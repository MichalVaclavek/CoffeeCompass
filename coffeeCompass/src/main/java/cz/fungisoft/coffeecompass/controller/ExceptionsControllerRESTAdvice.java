package cz.fungisoft.coffeecompass.controller;

import com.nimbusds.oauth2.sdk.ErrorResponse;
import cz.fungisoft.coffeecompass.controller.models.rest.CommonRestError;
import cz.fungisoft.coffeecompass.exceptions.rest.BadAuthorizationRESTRequestException;
import cz.fungisoft.coffeecompass.exceptions.rest.BadRESTRequestException;
import cz.fungisoft.coffeecompass.exceptions.rest.InvalidParameterValueException;
import cz.fungisoft.coffeecompass.exceptions.rest.RESTException;
import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralizovane zachycovani vyjimek a jejich zpracovani/odeslani pro REST rozhrani.
 */
@RestControllerAdvice
@Order(1)
public class ExceptionsControllerRESTAdvice extends ResponseEntityExceptionHandler {
    
    private static final Logger myLogger = LogManager.getLogger(ExceptionsControllerRESTAdvice.class);
    
    @Autowired
    private MessageSource messages;
    
    
    /** REST Exception handlers for errors during User handling **/
    
//    @ExceptionHandler({ UserNotFoundException.class })
//    public ResponseEntity<Object> handleRESTUserNotFound(UserNotFoundException ex, WebRequest request) {
//        logger.error("404 Status Code", ex);
//        GenericErrorResponse bodyOfResponse = new GenericErrorResponse(messages.getMessage("message.userNotFound.rest", null, request.getLocale()), "UserNotFound");
//         
//        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
//    }
    
    /** REST Exception handlers for error during User e-mail validation **/
    
    @ExceptionHandler({ MailAuthenticationException.class })
    public ResponseEntity<CommonRestError> handleMail(MailAuthenticationException ex, WebRequest request) {
        myLogger.error("40X Status Code", ex);
        CommonRestError bodyOfErrorResponse = new CommonRestError(messages.getMessage("message.email.config.error.rest",
                                                                                      null, request.getLocale()),
                                                                                      "MailError");
         
        return new ResponseEntity<>(bodyOfErrorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler({ BadRESTRequestException.class })
    public ResponseEntity<CommonRestError> handleBadRESTRequest(BadRESTRequestException ex, WebRequest request) {
        myLogger.error("400 Status Code", ex);
        CommonRestError bodyOfErrorResponse = new CommonRestError("/errors/bad_request", "Bad request",
                                                                  400, ex.getLocalizedMessage(),
                                                                  request.getDescription(false));
        return new ResponseEntity<>(bodyOfErrorResponse, HttpStatus.BAD_REQUEST);
    }
    
    
    @ExceptionHandler({ BadCredentialsException.class })
    public ResponseEntity<CommonRestError> handleBadCredentialsRESTRequest(BadCredentialsException ex, WebRequest request) {
        myLogger.error("401 Status Code", ex);
        CommonRestError bodyOfErrorResponse = new CommonRestError("/errors/authentication", "Wrong authentication",
                                                                  401, ex.getLocalizedMessage(),
                                                                  request.getDescription(false));
        return new ResponseEntity<>(bodyOfErrorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    
    @ExceptionHandler({ InvalidParameterValueException.class })
    public ResponseEntity<CommonRestError> handleInvalidParameterRESTRequest(InvalidParameterValueException ex, WebRequest request) {
        myLogger.error("400 Status Code", ex);
        CommonRestError bodyOfErrorResponse = new CommonRestError("/errors/input_parameters", "Wrong parameter",
                                                                  400, ex.getLocalizedErrorMessage(),
                                                                  request.getDescription(false));
        bodyOfErrorResponse.setErrorParameter(ex.getFieldName());
        bodyOfErrorResponse.setErrorParameterValue(ex.getFieldValue().toString());
        bodyOfErrorResponse.setErrorParametersMap(ex.getErrorsMap());
        return new ResponseEntity<>(bodyOfErrorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({ ResourceNotFoundException.class })
    public ResponseEntity<CommonRestError> handleResourceNotFoundRESTRequest(ResourceNotFoundException ex, WebRequest request) {
        myLogger.error("404 Status Code", ex);
        CommonRestError bodyOfErrorResponse = new CommonRestError("/errors/resources",
                                                                  "Resource not found: " + ex.getResourceName(),
                                                                  404, ex.getLocalizedErrorMessage(),
                                                                  request.getDescription(false));
        bodyOfErrorResponse.setErrorParameter(ex.getFieldName());
        bodyOfErrorResponse.setErrorParameterValue(ex.getFieldValue().toString());
        return new ResponseEntity<>(bodyOfErrorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler({ BadAuthorizationRESTRequestException.class })
    public ResponseEntity<CommonRestError> handleBadAuthorizationRESTRequest(BadAuthorizationRESTRequestException ex, WebRequest request) {
        myLogger.error("401 Status Code", ex);
        CommonRestError bodyOfErrorResponse = new CommonRestError("/errors/authentication", "Wrong authentication",
                                                                  401, ex.getLocalizedErrorMessage(),
                                                                  request.getDescription(false));
 
        return new ResponseEntity<>(bodyOfErrorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Method that check against {@code @Valid} Objects passed to controller endpoints
     *
     * @param ex
     */
//    @ExceptionHandler({MethodArgumentNotValidException.class})
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<CommonRestError> handleValidationException(MethodArgumentNotValidException ex) {
//
//        Map<String, Set<String>> errorsMap = ex.getBindingResult().getFieldErrors().stream().collect(
//                Collectors.groupingBy(FieldError::getField,
//                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toSet())
//                ));
//
//        CommonRestError bodyOfErrorResponse = new CommonRestError("/errors/inputValidation", "Wrong input values",
//                502, ex.getMessage(), "");
//        bodyOfErrorResponse.setErrorParametersMap(errorsMap);
//        return new ResponseEntity<>(bodyOfErrorResponse, HttpStatus.BAD_GATEWAY);
//    }


    /** Obecny REST exception handler **/
    
    @ExceptionHandler({ RESTException.class })
    public ResponseEntity<Object> handleRESTInternal(RESTException ex, WebRequest request) {
        myLogger.error("500 Status Code", ex);
        CommonRestError bodyOfResponse = new CommonRestError(messages.getMessage("message.error",
                                                                     null, request.getLocale()),
                                                                     "InternalError");
         
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}