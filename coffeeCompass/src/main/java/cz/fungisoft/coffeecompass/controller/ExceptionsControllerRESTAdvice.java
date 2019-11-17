package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.controller.models.GenericResponse;
import cz.fungisoft.coffeecompass.exception.BadRequestException;
import cz.fungisoft.coffeecompass.exception.RESTException;
import cz.fungisoft.coffeecompass.exception.UserNotFoundException;

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
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Centralizovane zachycovani vyjimek a jejich zpracovani/odeslani pro REST rozhrani.
 */
@ControllerAdvice
public class ExceptionsControllerRESTAdvice extends ResponseEntityExceptionHandler
{
    private static final Logger logger = LogManager.getLogger(ExceptionsControllerRESTAdvice.class);
    
    @Autowired
    private MessageSource messages;
    
    
    /** REST Exception handlers for errors during User handling **/
    
    @ExceptionHandler({ UserNotFoundException.class })
    public ResponseEntity<Object> handleRESTUserNotFound(UserNotFoundException ex, WebRequest request) {
        logger.error("404 Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(messages.getMessage("message.userNotFound.rest", null, request.getLocale()), "UserNotFound");
         
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
    
    /** REST Exception handlers for error during User e-mail validation **/
    
    @ExceptionHandler({ MailAuthenticationException.class })
    public ResponseEntity<Object> handleMail(MailAuthenticationException ex, WebRequest request) {
        logger.error("40X Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(messages.getMessage("message.email.config.error.rest", null, request.getLocale()), "MailError");
         
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }
    
    @ExceptionHandler({ BadRequestException.class })
    public ResponseEntity<Object> handleBadRESTRequest(RuntimeException ex, WebRequest request) {
        logger.error("40X Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(messages.getMessage("message.error.bad.request",
                                                             null,
                                                             request.getLocale()),
                                                             "Bad request");
         
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
 
    /** Obecny REST exception handler **/
    
    @ExceptionHandler({ RESTException.class })
    public ResponseEntity<Object> handleRESTInternal(RuntimeException ex, WebRequest request) {
        logger.error("500 Status Code", ex);
        GenericResponse bodyOfResponse = new GenericResponse(messages.getMessage("message.error",
                                                             null,
                                                             request.getLocale()),
                                                             "InternalError");
         
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
    
    
}