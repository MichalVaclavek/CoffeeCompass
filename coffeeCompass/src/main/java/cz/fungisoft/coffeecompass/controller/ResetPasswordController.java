package cz.fungisoft.coffeecompass.controller;

import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.controller.models.EmailAddressModel;
import cz.fungisoft.coffeecompass.controller.models.NewPasswordInputModel;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.security.IAuthenticationFacade;
import cz.fungisoft.coffeecompass.service.TokenCreateAndSendEmailService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.service.ValidateTokenService;

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
public class ResetPasswordController
{
    private UserService userService;
    
    private TokenCreateAndSendEmailService passwordTokenService;
    
    private ValidateTokenService validateTokenService;
    
    private IAuthenticationFacade authenticationFacade;
    
    private CustomUserDetailsService userDetailsService;
    
    /**
     * Standard constructor.
     * 
     * @param userService
     * @param passwordTokenService
     * @param validateTokenService
     * @param authenticationFacade
     * @param userDetailsService
     */
    public ResetPasswordController(UserService userService,
                                   TokenCreateAndSendEmailService passwordTokenService,
                                   ValidateTokenService validateTokenService,
                                   IAuthenticationFacade authenticationFacade,
                                   CustomUserDetailsService userDetailsService) {
        super();
        this.userService = userService;
        this.passwordTokenService = passwordTokenService;
        this.validateTokenService = validateTokenService;
        this.authenticationFacade = authenticationFacade;
        this.userDetailsService = userDetailsService;
    }
    
    
    /**
     * Request to get Form for entering user's e-mail to send link allowing reseting passwd.
     * 
     * @return
     */
    @GetMapping("/forgotPassword")
    public String showForgotPasswordEmailForm(ModelMap model) {

        EmailAddressModel emailAddr = (EmailAddressModel) model.getOrDefault("emailAddr", new EmailAddressModel());
        model.addAttribute("emailAddr", emailAddr);
        
        return "forgotPassword";
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
                                      @Valid @ModelAttribute("emailAddr") EmailAddressModel userEmail,
                                      BindingResult bindingResult,
                                      RedirectAttributes attr) {
        
        ModelAndView mav = new ModelAndView();
        mav.setViewName("forgotPassword");
       
        if (bindingResult.hasErrors()) { // wrong format of e-mail address or empty
            return mav;
        } else { // try to find user based on valid e-mail address
            User user = userService.findByEmail(userEmail.getEmailAddr());
            if (user == null) {
                bindingResult.rejectValue("emailAddr", "resetPassword.email.unknown.message");
                return mav;
            } 
        }
        // User found by e-mail. Send passwd. reset link to the user.
        mav.setViewName("redirect:/login");
        String appUrl = "http://" + request.getServerName() +  ":" + request.getServerPort() +  request.getContextPath();
        passwordTokenService.setResetPasswordTokenData(userEmail.getEmailAddr(), appUrl, request.getLocale());
        passwordTokenService.createAndSendResetPasswordTokenEmail();
        attr.addFlashAttribute("resetPasswordEmailSent", true); 
        return mav;
    }
    
    /**
     * Serves click to reset password link sent to user by e-mail.
     * 
     * @param userId
     * @param token
     * @param locale
     * @param attr
     * @return
     */
    @GetMapping(value = "/user/changePassword")
    public ModelAndView processChangePasswordLink(@RequestParam(value = "userId", required = true) long userId,
                                                  @RequestParam(value = "token", required = true) String token,
                                                  Locale locale,
                                                  RedirectAttributes attr) {

        ModelAndView mav = new ModelAndView();
        // Validates token and if valid then log-in user with "CHANGE_PASSWORD_PRIVILEGE" needed for entering updatePassword.html page
        String validationResult = validateTokenService.validatePasswordResetToken(userId, token);
        
        if (!validationResult.isEmpty()) { // token not valid
            attr.addFlashAttribute("passwordResetTokenInvalid", validationResult);
            mav.setViewName("redirect:/login?lang=" + locale.getLanguage());
        } else { // valid token
            attr.addFlashAttribute("token", token); // needed to pass further, to be deleted after the new password is saved.
            mav.setViewName("redirect:/user/updatePassword?lang=" + locale.getLanguage());
        }
        
        return mav;
    }
    
    /**
     * Shows the page for entering new and confirm password.<br>
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
        mav.setViewName("updatePassword");
        
        // log-out temporary principal with "CHANGE_PASSWORD_PRIVILEGE"
        authenticationFacade.getContext().setAuthentication(null);
        
        return mav;
    }
    
    
    /**
     * Serves click on save/reset new password form on updatePassword page
     * 
     * @param locale
     * @param passwordDto
     * @return
     */
    @PostMapping(value = "/user/saveNewPassword/")
    public ModelAndView saveNewPassword(Locale locale,
                                        @RequestParam(value = "token", required = true) String token,
                                        @Valid @ModelAttribute("resetedPassword") NewPasswordInputModel resetedPassword,
                                        BindingResult bindingResult,
                                        RedirectAttributes attr) {
        
        ModelAndView mav = new ModelAndView();
        
        if (bindingResult.hasErrors()) { // wrong new password length or passwd. and confirmPasswd do not match
            mav.setViewName("updatePassword");
            mav.addObject("token", token);
            return mav;
        }
        
        User user = userService.getUserByPasswordResetToken(token);
        boolean saveResult = userService.changeUserPassword(user, resetedPassword.getNewPassword());
        
        if (saveResult) { 
            attr.addFlashAttribute("passwordResetSaveSuccess", true);
            // token can be deleted (was already used to save new passwd.)
            passwordTokenService.deletePasswordResetToken(token);
            
            // User verified by email, password reseted, User can be logged-in now
            Authentication auth = authenticationFacade.getContext().getAuthentication();
            if (auth == null || auth.getName().equalsIgnoreCase("anonymousUser")) {    
                Collection<SimpleGrantedAuthority>  nowAuthorities = (Collection<SimpleGrantedAuthority>) userDetailsService.getGrantedAuthorities(user);
                UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(user.getUserName(), resetedPassword.getNewPassword(), nowAuthorities);
                authenticationFacade.getContext().setAuthentication(newAuthentication);                 
            }
        } else {
            attr.addFlashAttribute("passwordResetSaveFailed", true);
        }
        
        mav.setViewName("redirect:/home?lang=" + locale.getLanguage());
        return mav;
    }
    
}
