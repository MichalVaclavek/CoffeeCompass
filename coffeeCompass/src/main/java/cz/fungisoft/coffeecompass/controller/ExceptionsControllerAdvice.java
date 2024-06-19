package cz.fungisoft.coffeecompass.controller;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.Locale;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import cz.fungisoft.coffeecompass.exceptions.BadAuthorizationRequestException;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.exceptions.OAuth2AuthenticationProcessingException;
import cz.fungisoft.coffeecompass.exceptions.UserNotFoundException;

/**
 * Centralizovane zachycovani vyjimek a prirazovani textu a View vsem druhum vyjimek
 * pro pripad pouziti Thymeleaf procesingu.
 */
@ControllerAdvice
@Order(2)
public class ExceptionsControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger myLogger = LogManager.getLogger(ExceptionsControllerAdvice.class);
    
    private static final String ERROR_MODEL_KEY = "error";
    private static final String STATUS_MODEL_KEY = "status";
    private static final String ERROR_MESSAGE_MODEL_KEY = "errorMessage";
    
    private static final String ERROR_VIEW_NAME = "error";
    private static final String ERROR_404_VIEW_NAME = "error/404";
    private static final String LOGIN_VIEW_NAME = "login";
    
    @Autowired
    private MessageSource messages;
    
    
    @ExceptionHandler({EntityNotFoundException.class})
    public ModelAndView handleEntNotFound(HttpServletResponse res, EntityNotFoundException e) {
        myLogger.error("Chyba: Entity Not Found {}", e.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject(ERROR_MODEL_KEY, "Requested Entity not found!");
        model.addObject(STATUS_MODEL_KEY, "Try another page/item.");
        model.addObject(ERROR_MESSAGE_MODEL_KEY, e.getMessage());
        model.setViewName(ERROR_404_VIEW_NAME);
        return model;
    }
    
    @ExceptionHandler({ UserNotFoundException.class })
    public ModelAndView handleRESTUserNotFound(UserNotFoundException ex) {
        
        myLogger.error("404 - Chyba: user Not Found {}", ex.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject(ERROR_MODEL_KEY, "Requested User not found!");
        model.addObject(STATUS_MODEL_KEY, "Try another page/item.");
        model.addObject(ERROR_MESSAGE_MODEL_KEY, ex.getMessage());
        model.setViewName(ERROR_404_VIEW_NAME);
        return model;
    }

    /**
     * Probably not needed.
     * 
     * @param e
     * @return
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ModelAndView handleConstraintViolation(ConstraintViolationException e) {
        myLogger.error("Chyba: Constraint Violation {}", e.getMessage());

        ModelAndView model = new ModelAndView();
        model.addObject(ERROR_MESSAGE_MODEL_KEY, e.getMessage());
        model.setViewName(ERROR_VIEW_NAME);
        return model;
    }
    
    
    /**
     * Probably not needed currently as the validation of the uploaded file size is performed<br>
     * (by {@link cz.fungisoft.coffeecompass.validators.ImageFileValidator})<br>
     * before Exception could be raised.
     * 
     * @param exc
     * @return
     */
    @ExceptionHandler(value = {MultipartException.class, MaxUploadSizeExceededException.class})
    public ModelAndView maxUploadSizeExceeded(MultipartException exc) {
        
        myLogger.error("MaxUploadSizeExceeded {}", exc.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject(ERROR_MESSAGE_MODEL_KEY, exc.getMessage());
        model.getModel().put("fileTooLargeError", "File too large!");
        model.setViewName(ERROR_VIEW_NAME);
        return model;
    }
    
    /**
     * Probably not needed here as it is handled within
     *  {@link cz.fungisoft.coffeecompass.security.oauth2.OAuth2AuthenticationFailureHandler}
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler({ OAuth2AuthenticationProcessingException.class })
    public ModelAndView handleOAuth2AuthenticationProcessingException(OAuth2AuthenticationProcessingException ex) {
        
        myLogger.error("Chyba: {}", ex.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject(ERROR_MODEL_KEY, "Sorry user, error! Chyba pri 'social network' autentikaci.");
        model.addObject("oAuth2ErrorMessage", ex.getMessage());
        model.setViewName(LOGIN_VIEW_NAME);
        
        return model;
    }
    
    @ExceptionHandler({ BadAuthorizationRequestException.class })
    public ModelAndView handleBadAuthorizationRequestException(BadAuthorizationRequestException ex, Locale locale) {
        
        myLogger.error("Chyba: {}", ex.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject(ERROR_MODEL_KEY, "Sorry user, error! Problem pri autorizaci uzivatele.");
        model.addObject(ERROR_MESSAGE_MODEL_KEY, messages.getMessage(ex.getLocalizedMessageCode(), null, locale));
        model.setViewName(ERROR_VIEW_NAME);
        return model;
    }
    
    
    /**
     * Osetreni ostatnich vyjimky nedefinovanych v me aplikaci.
     */
    @ExceptionHandler
    public ModelAndView handleOtherExceptions(Exception e) {

        myLogger.error("Chyba: {}", e.getMessage());

        ModelAndView model = new ModelAndView();
        model.addObject(ERROR_MODEL_KEY, "Sorry user, error!");
        model.addObject(ERROR_MESSAGE_MODEL_KEY, e.getMessage());
        model.setViewName(ERROR_VIEW_NAME);
        return model;
    }
}