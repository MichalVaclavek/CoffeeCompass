package cz.fungisoft.coffeecompass.controller;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.controller.models.EmailAddressModel;
import cz.fungisoft.coffeecompass.controller.models.NewPasswordInputModel;
import cz.fungisoft.coffeecompass.entity.PasswordResetToken;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.service.tokens.TokenCreateAndSendEmailService;
import cz.fungisoft.coffeecompass.service.tokens.ValidateTokenService;
import cz.fungisoft.coffeecompass.service.user.UserSecurityService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to handle URLs for forgot password procedure.<br>
 * Handles entering of the e-mail address of the user where the reset password link should be sent to.<br>
 * Handles click to the reset password link (sent by e-mail to user).<br>
 * Handles entering new password and it's confirnation.<br>
 * After successful entering of the new password the user is automaticly loged-in.<br> 
 * 
 * @author Michal Vaclavek
 *
 */
@Controller
@Slf4j
public class ResetPasswordController {

    private static final String FORGOT_PASSWD_VIEW = "forgotPassword";
    private static final String UPDATE_PASSWD_VIEW = "updatePassword";
    
    private static final String EMAIL_ADDR_MODEL_KEY = "emailAddr";
    private static final String TOKEN_ATTRIB_KEY = "token";
    
    
    private final UserService userService;
    
    private final TokenCreateAndSendEmailService passwordTokenService;
    
    private final ValidateTokenService validateTokenService;
    
    private final UserSecurityService userSecurityService;
    
    private final MessageSource messages;
    
    /**
     * Standard constructor.
     * 
     * @param userService
     * @param passwordTokenService
     * @param validateTokenService
     */
    public ResetPasswordController(UserService userService,
                                   TokenCreateAndSendEmailService passwordTokenService,
                                   ValidateTokenService validateTokenService,
                                   UserSecurityService userSecurityService,
                                   MessageSource messages) {
        super();
        this.userService = userService;
        this.passwordTokenService = passwordTokenService;
        this.validateTokenService = validateTokenService;
        this.userSecurityService = userSecurityService;
        this.messages = messages;
    }
    
    
    /**
     * Request to get Form for entering user's e-mail to send link allowing reseting passwd.
     * 
     * @return
     */
    @GetMapping("/forgotPassword")
    public String showForgotPasswordEmailForm(ModelMap model) {

        EmailAddressModel emailAddr = (EmailAddressModel) model.getOrDefault(EMAIL_ADDR_MODEL_KEY, new EmailAddressModel());
        model.addAttribute(EMAIL_ADDR_MODEL_KEY, emailAddr);
        
        return FORGOT_PASSWD_VIEW;
    }

    // ------------------- Forgot password ---------------------------------- //

    /**
     * Processes submit of the page/form with user's e-mail, where the passwd. reset link shoul be sent to.
     * 
     * @param request
     * @param userEmail
     * @param bindingResult
     * @param attr
     * @return
     */
    @PostMapping(value = "/user/resetPassword")
    public ModelAndView resetPassword(HttpServletRequest request,
                                      @Valid @ModelAttribute(EMAIL_ADDR_MODEL_KEY) EmailAddressModel userEmail,
                                      BindingResult bindingResult,
                                      RedirectAttributes attr) {
        
        ModelAndView mav = new ModelAndView();
        mav.setViewName(FORGOT_PASSWD_VIEW);
       
        if (bindingResult.hasErrors()) { // wrong format of e-mail address or empty
            return mav;
        }
        
        // try to find user based on valid e-mail address
        Optional<User> user = userService.findByEmail(userEmail.getEmailAddr());
        if (!user.isPresent()) {
            bindingResult.rejectValue(EMAIL_ADDR_MODEL_KEY, "resetPassword.email.unknown.message");
            return mav;
        } 
        
        // User found by e-mail. Send password reset link to the user.
        mav.setViewName("redirect:/login");
        passwordTokenService.setResetPasswordTokenData(userEmail.getEmailAddr(), request.getLocale());
        passwordTokenService.createAndSendResetPasswordTokenEmail();
        attr.addFlashAttribute("resetPasswordEmailSent", true); 
        return mav;
    }
    
    /**
     * Serves click to reset password link sent to user's e-mail address.
     * 
     * @param userId
     * @param token
     * @param locale
     * @param attr
     * @return
     */
    @GetMapping(value = "/user/changePassword")
    public ModelAndView processChangePasswordLink(@RequestParam(value = "userId", required = true) String userId,
                                                  @RequestParam(value = TOKEN_ATTRIB_KEY, required = true) String token,
                                                  Locale locale,
                                                  RedirectAttributes attr) {

        ModelAndView mav = new ModelAndView();
        // Validates token 
        String validationResult = validateTokenService.validatePasswordResetToken(UUID.fromString(userId), token);
        
        if (validationResult.isEmpty()) { // token is valid
            // create temporary active principal with ROLE CHANGE_PASSWORD_PRIVILEGE
            PasswordResetToken passToken = passwordTokenService.getPasswordResetToken(token);
            User user = passToken.getUser();
            userSecurityService.authWithUserNameAndRole(user.getUserName(), "CHANGE_PASSWORD_PRIVILEGE");
            
            attr.addFlashAttribute(TOKEN_ATTRIB_KEY, token); // needed to pass further, to be deleted after the new password is saved.
            mav.setViewName("redirect:/user/updatePassword?lang=" + locale.getLanguage());
        } else { // invalid token
            attr.addFlashAttribute("passwordResetTokenInvalid", validationResult);
            mav.setViewName("redirect:/login?lang=" + locale.getLanguage());
        }
        
        return mav;
    }
    
    /**
     * Shows the page for entering new and confirm password.
     * <p>
     * Called after successfull verification of password reset token in processChangePasswordLink method.<br>
     * Can be called only with "CHANGE_PASSWORD_PRIVILEGE", see SecurityConfiguration.<br>
     * 
     * @return
     */
    @GetMapping(value = "/user/updatePassword")
    public ModelAndView showChangePasswordPage(ModelMap modelMap) {  
        ModelAndView mav = new ModelAndView();
        mav.addAllObjects(modelMap);
        NewPasswordInputModel resetedPassword = new NewPasswordInputModel(); 
        mav.addObject("resetedPassword", resetedPassword);
        mav.setViewName(UPDATE_PASSWD_VIEW);
        
        // log-out temporary principal with "CHANGE_PASSWORD_PRIVILEGE" role.
        userSecurityService.logout();
        
        return mav;
    }
    
    
    /**
     * Serves click on save/reset new password form on updatePassword page
     * 
     * @param locale
     * @param resetedPassword
     * @return
     */
    @PostMapping(value = "/user/saveNewPassword/")
    public ModelAndView saveNewPassword(Locale locale,
                                        @RequestParam(value = TOKEN_ATTRIB_KEY, required = true) String token,
                                        @Valid @ModelAttribute("resetedPassword") NewPasswordInputModel resetedPassword,
                                        BindingResult bindingResult,
                                        RedirectAttributes attr,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        
        ModelAndView mav = new ModelAndView();
        
        if (bindingResult.hasErrors()) { // wrong new password length or passwd. and confirmPasswd do not match
            mav.setViewName(UPDATE_PASSWD_VIEW);
            mav.addObject(TOKEN_ATTRIB_KEY, token);
            return mav;
        }
        
        User user = userService.getUserByPasswordResetToken(token);
        boolean saveResult = userService.changeUserPassword(user, resetedPassword.getNewPassword());
        
        if (saveResult) { 
            attr.addFlashAttribute("passwordResetSaveSuccess", true);
            // token can be deleted (was already used to save new passwd.)
            passwordTokenService.deletePasswordResetToken(token);
            
            // User verified by email, password reseted, User can be logged-in now
            userSecurityService.authWithPassword(user, resetedPassword.getNewPassword(), request, response);
        } else {
            attr.addFlashAttribute("passwordResetSaveFailed", true);
        }
        
        mav.setViewName("redirect:/home?lang=" + locale.getLanguage());
        return mav;
    }
    
    /**
     * Handles exception during sending e-mail with reset password link.
     * 
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler({MailException.class})  
    public ModelAndView handleSendMailException(MailException e, WebRequest request) {
        log.error("MailException: {}", e.getMessage());
        
        String errorMessage = messages.getMessage("resetPassword.sendemail.error.message", null, request.getLocale());
        ModelAndView model = new ModelAndView();
        model.addObject("errorMessage", errorMessage);
        model.setViewName(FORGOT_PASSWD_VIEW);
        return model;
    }
}
