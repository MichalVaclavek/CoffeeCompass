package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.exception.EntityNotFoundException;
import cz.fungisoft.coffeecompass.pojo.Message;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Centralizovane zachycovani vyjimek a prirazovani textu vsem druhum vyjimek
 */
//@RestControllerAdvice
@ControllerAdvice
public class ExceptionsControllerAdvice extends ResponseEntityExceptionHandler
{
    @ExceptionHandler
    public ResponseEntity<Message> handleEntNotFound(EntityNotFoundException e) {
        // Co se ma poslat v html body v pripade vyjimky. HttpStatus.NOT_FOUND generuje response/status kod 404
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(e.getMessage()));
    }

    
    @ExceptionHandler  
    public ResponseEntity<Message> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(e.getMessage()));
    }
    
    /**
     * Osetreni dalsiho typu vyjimky v definovanych v me aplikaci 
     *
     */
    @ExceptionHandler
    public ResponseEntity<Message> handleOtherExceptions(Exception e) {
        // Co se ma poslat v html body v pripade vyjimky. HttpStatus.NOT_FOUND generuje response/status kod 404
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(new Message(e.getMessage()));
    }
    
}