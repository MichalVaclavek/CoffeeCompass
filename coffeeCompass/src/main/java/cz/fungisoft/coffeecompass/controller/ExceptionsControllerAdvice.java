package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.controller.models.GenericResponse;
import cz.fungisoft.coffeecompass.exception.EntityNotFoundException;
import cz.fungisoft.coffeecompass.exception.UserNotFoundException;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Centralizovane zachycovani vyjimek a prirazovani textu a View vsem druhum vyjimek.
 */
@ControllerAdvice
public class ExceptionsControllerAdvice extends ResponseEntityExceptionHandler
{
    private static final Logger logger = LogManager.getLogger(ExceptionsControllerAdvice.class);
    
    @Autowired
    private MessageSource messages;
    
    
    @ExceptionHandler({EntityNotFoundException.class})
    public ModelAndView handleEntNotFound(HttpServletResponse res, EntityNotFoundException e) {
        logger.error("Chyba Entity Not Found {}", e.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject("error", "Requested Entity not found!");
        model.addObject("status", "Try another page/item.");
        model.addObject("errorMessage", e.getMessage());
        model.setViewName("error/404");
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
        logger.error("Chyba Constraint Violation {}", e.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject("errorMessage", e.getMessage());
        model.setViewName("error");
        return model;
    }
    
    /*
    @ExceptionHandler(StorageFileException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileException exc) {
        return ResponseEntity.notFound().build();
    }
    */
    
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
        
        logger.error("MaxUploadSizeExceeded {}", exc.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject("errorMessage", exc.getMessage());
        model.getModel().put("fileTooLargeError", "File too large!");
        model.setViewName("error");
        return model;
    }
    
    /**
     * Osetreni ostatnich vyjimky nedefinovanych v me aplikaci 
     //TODO pozor, stejnou obsluhu asi dela i metoda nize  handleInternal(RuntimeException ex, WebRequest request) 
     */
    @ExceptionHandler
    public ModelAndView handleOtherExceptions(Exception e) {
        
        logger.error("Chyba {}", e.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject("error", "Sorry user, error!");
        model.addObject("errorMessage", e.getMessage());
        model.setViewName("error");
        return model;
    }
    
    @ExceptionHandler({ UserNotFoundException.class })
    public ResponseEntity<Object> handleUserNotFound(RuntimeException ex, WebRequest request) {
        logger.error("404 Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(messages.getMessage("message.userNotFound.rest", null, request.getLocale()), "UserNotFound");
         
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
 
    /** REST Exception handlers for error during User e-mail validation **/
    
    @ExceptionHandler({ MailAuthenticationException.class })
    public ResponseEntity<Object> handleMail(RuntimeException ex, WebRequest request) {
        logger.error("500 Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(messages.getMessage("message.email.config.error.rest", null, request.getLocale()), "MailError");
         
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }
 
    /** Obecny REST exception handler **/
    
    /*
    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleInternal(RuntimeException ex, WebRequest request) {
        logger.error("500 Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(messages.getMessage("message.error",
                                                             null,
                                                             request.getLocale()),
                                                             "InternalError");
         
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
    */
    
}