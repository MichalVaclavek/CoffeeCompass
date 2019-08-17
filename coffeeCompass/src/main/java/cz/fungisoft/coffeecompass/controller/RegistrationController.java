package cz.fungisoft.coffeecompass.controller;

import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.controller.models.GenericResponse;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserVerificationToken;
import cz.fungisoft.coffeecompass.listeners.OnRegistrationCompleteEvent;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.service.VerificationTokenCreateAndSendEmailService;

/**
 * Controller to process click on User verification link.
 * 
 * @author Michal Vaclavek
 *
 */
@Controller
public class RegistrationController
{
    private UserService userService;
    
    private VerificationTokenCreateAndSendEmailService verificationTokenService;
    
    private MessageSource messagesSource;
    
    
    public RegistrationController(UserService userService, VerificationTokenCreateAndSendEmailService verificationTokenService,
                                  MessageSource messagesSource) {
        super();
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.messagesSource = messagesSource;
    }

    /**
     * Handles click to verification e-mail link 
     * 
     * @param locale
     * @param model
     * @param token
     * @return
     */
    @GetMapping(value = "/user/registrationConfirm")
    public String confirmRegistration(Locale locale, Model model, @RequestParam("token") String token, RedirectAttributes attr) {
        
        UserVerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        
//        String message = messagesSource.getMessage("register.failure.invalidtoken.message", null, locale);
        
        // Error creatin token?
        if (verificationToken == null) {
//            model.addAttribute("message", message);
//            model.addAttribute("expired", false);
//            attr.addFlashAttribute("message", message);
            attr.addFlashAttribute("tokenInvalid", true);
            
            return (userService.getCurrentLoggedInUser() != null) ? "redirect:/home"
                                                                  : "redirect:/registrationTokenFailure";
        }

        // Token expired ?
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
//            message = messagesSource.getMessage("register.failure.expiredtoken.message", null, locale);
//            model.addAttribute("message", message);
//            model.addAttribute("expired", true);
//            model.addAttribute("token", token);
//            attr.addFlashAttribute("message", message);
            attr.addFlashAttribute("tokenExpired", true);
            attr.addFlashAttribute("token", token);
            return "redirect:/registrationTokenFailure";
        }
        
        // Token valid ?
        User user = verificationToken.getUser();
        userService.saveVerifiedRegisteredUser(user, token);
//        model.addAttribute("message", messagesSource.getMessage("message.accountVerified", null, locale));
//        model.addAttribute("emailVerified", true);
//        model.addAttribute("userName", user.getUserName());
        
        attr.addFlashAttribute("userName", user.getUserName());
        attr.addFlashAttribute("emailVerified", true);
        
        // If already logged-in user confirmed, then go to home page
        // If user not logged-in yet, go to log-in page
        return (userService.getCurrentLoggedInUser().getId() == user.getId()) ? "redirect:/home"
                                                                              : "redirect:/login/?lang=" + locale.getLanguage();

    }
    
    @GetMapping(value = "/registrationTokenFailure")
    public String showRegistrationTokenFailurePage() {
        return "registrationTokenFailure";
    }
    
    /**
     * User requests another verification link, in case the previous one expired.
     * REST variant, not working yet, problems in a view "registrationTokenFailure"
     * 
     //TODO DO NOT DELETE, NEEDS TO BE REPAIRED 
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
        
        UserVerificationToken verificationToken = verificationTokenService.getVerificationToken(existingToken);
        User user = verificationToken.getUser();
        
        String newToken = "";
        
        try {
            String appUrl = "http://" + request.getServerName() +  ":" + request.getServerPort() +  request.getContextPath();
            verificationTokenService.setVerificationData(userService.getCurrentLoggedInUser(), appUrl, request.getLocale());
            newToken = verificationTokenService.reSendVerificationTokenEmail(existingToken);
            
            attr.addFlashAttribute("userName", user.getUserName());
            attr.addFlashAttribute("verificationEmailSent", true);
            attr.addFlashAttribute("userCreateSuccess", true);
            
            if (userService.getCurrentLoggedInUser().getId() == user.getId()) {
                mav.setViewName("redirect:/home");
            } 
//            else {
//                mav.setViewName("redirect:/login/?lang=" + request.getLocale().getLanguage());
//            }
        } catch (Exception me) {
            
            attr.addFlashAttribute("emailError", true);
            attr.addFlashAttribute("token", newToken);
            
            mav.setViewName("redirect:/registrationTokenFailure"); // problems to send confirmation e-mail
        }
        
        return mav;
    }
    
}
