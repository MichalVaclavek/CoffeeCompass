package cz.fungisoft.coffeecompass.controller;

import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserEmailVerificationToken;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.service.ValidateTokenService;
import cz.fungisoft.coffeecompass.service.TokenCreateAndSendEmailService;
import cz.fungisoft.coffeecompass.service.UserSecurityService;

/**
 * Controller to process click on User registration e-mail verification link.
 * 
 * @author Michal Vaclavek
 */
@Controller
public class RegistrationController
{
    private UserService userService;
    
    private TokenCreateAndSendEmailService userVerificationTokenService;
    
    private ValidateTokenService validateTokenService;
    
    private UserSecurityService userSecurityService;
    
    /**
     * 
     * @param userService
     * @param verificationTokenService
     * @param messagesSource
     */
    public RegistrationController(UserService userService,
                                  TokenCreateAndSendEmailService verificationTokenService,
                                  ValidateTokenService validateTokenService,
                                  UserSecurityService userSecurityService) {
        super();
        this.userService = userService;
        this.userVerificationTokenService = verificationTokenService;
        this.validateTokenService = validateTokenService;
        this.userSecurityService = userSecurityService;
    }

    /**
     * Handles click to verification e-mail link.
     * 
     * Logout first if any user is already logged-in. It can happen, when one user is loged-in in browser
     * and second one performs new registration via REST.
     * 
     * @param locale
     * @param model
     * @param token
     * @return
     */
    @GetMapping(value = "/user/registrationConfirm")
    public String confirmRegistration(Locale locale, Model model, @RequestParam("token") String token, RedirectAttributes attr) {
        
        String tokenValidationResult = validateTokenService.validateUserRegistrationToken(token);
        
        // Invalid token
        if ("invalidToken".equals(tokenValidationResult)) {
            attr.addFlashAttribute("tokenInvalid", true);
            return (userService.getCurrentLoggedInUser().isPresent()) ? "redirect:/home"
                                                                      : "redirect:/registrationTokenFailure";
        }

        // Token expired ?
        if ("expiredToken".equals(tokenValidationResult)) {
            attr.addFlashAttribute("tokenExpired", true);
            attr.addFlashAttribute("token", token);
            return "redirect:/registrationTokenFailure";
        }
        
        // Valid token
        if (tokenValidationResult.isEmpty()) {

            UserEmailVerificationToken verificationToken = userVerificationTokenService.getUserVerificationToken(token);

            User newUser = verificationToken.getUser();
            userService.saveVerifiedRegisteredUser(newUser, token);
            
            // Deletes already used confirmation token from DB. This makes it invalid.
            userVerificationTokenService.deleteRegistrationToken(token);
            
            attr.addFlashAttribute("userName", newUser.getUserName());
            attr.addFlashAttribute("emailVerified", true);
            
            Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
            // If email confirmed by already logged-in user, then go to home page
            // Default go to login page, after e-mail confirm success, but first log-out user if it is different from new user, whio confirmed email
            if (loggedInUser.isPresent()) {
                if (loggedInUser.get().getId() == newUser.getId()) {
                   return "redirect:/home";
                } else { // new user is different from current logged-in user - logout current user
                    userSecurityService.logout();
                }
            }
        }
        
        return "redirect:/login/?lang=" + locale.getLanguage(); 
    }
    
    @GetMapping(value = "/registrationTokenFailure")
    public String showRegistrationTokenFailurePage() {
        return "registrationTokenFailure";
    }
    
    /**
     * User requests another verification link, in case the previous one expired.
     * REST variant, not working yet, problems in a view "registrationTokenFailure"
     * 
     //TODO DO NOT DELETE, NEEDS TO BE REPAIRED as REST reponse
     * 
     * @param request
     * @param existingToken
     * @return
     */
//    @GetMapping(value = "/user/resendRegistrationToken")
//    @ResponseBody
//    public GenericResponse resendRegistrationToken(HttpServletRequest request,
//                                                   @RequestParam("token") String existingToken) {
//      
//        String appUrl = "http://" + request.getServerName() +  ":" + request.getServerPort() +  request.getContextPath();
//        verificationTokenService.setVerificationData(userService.getCurrentLoggedInUser(), appUrl, request.getLocale());
//        
//        //TODO - 
//        verificationTokenService.reSendVerificationTokenEmail(existingToken);
//        return new GenericResponse(messagesSource.getMessage("message.resendToken", null, request.getLocale()));
//    }
    
    /**
     * User requests another verification link, in case the previous one expired.
     * 
     * @param request
     * @param existingToken
     * @return
     */
    @GetMapping(value = "/user/resendRegistrationToken")
    @ResponseBody
    public ModelAndView resendRegistrationToken(HttpServletRequest request,
                                                @RequestParam("token") String existingToken,
                                                RedirectAttributes attr) {
        
        // Standard new page (login) after new verification e-mail sent - used in case user is not logged-in yet
        ModelAndView mav = new ModelAndView("redirect:/login/?lang=" + request.getLocale().getLanguage());
        
        UserEmailVerificationToken verificationToken = userVerificationTokenService.getUserVerificationToken(existingToken);
        User user = verificationToken.getUser();
        
        String newToken = "";
        
        try {
            userVerificationTokenService.setUserVerificationData(user, request.getLocale());
            newToken = userVerificationTokenService.reSendUserVerificationTokenEmail(existingToken);
            
            attr.addFlashAttribute("userName", user.getUserName());
            attr.addFlashAttribute("verificationEmailSent", true);
            attr.addFlashAttribute("userCreateSuccess", true);
            
            Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
            
            if (loggedInUser.isPresent() && loggedInUser.get().getId() != null
                && loggedInUser.get().getId() == user.getId()) {
                mav.setViewName("redirect:/home");
            } 
        } catch (Exception me) {
            attr.addFlashAttribute("emailError", true);
            attr.addFlashAttribute("token", newToken); // passed to be used for generation of a new token
            mav.setViewName("redirect:/registrationTokenFailure"); // problems to resend confirmation e-mail
        }
        
        return mav;
    }
    
}