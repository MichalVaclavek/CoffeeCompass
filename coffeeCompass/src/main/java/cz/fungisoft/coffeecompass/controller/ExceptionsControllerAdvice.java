package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.exception.EntityNotFoundException;
import cz.fungisoft.coffeecompass.exception.StorageFileException;
import cz.fungisoft.coffeecompass.pojo.Message;
import cz.fungisoft.coffeecompass.serviceimpl.SendMeEmailServiceImpl;

import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(ExceptionsControllerAdvice.class);
    
    @ExceptionHandler
    public ResponseEntity<Message> handleEntNotFound(EntityNotFoundException e) {
        // Co se ma poslat v html body v pripade vyjimky. HttpStatus.NOT_FOUND generuje response/status kod 404
        logger.error("Chyba Entity Not Found {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(e.getMessage()));
    }

    
    @ExceptionHandler  
    public ResponseEntity<Message> handleConstraintViolation(ConstraintViolationException e) {
        logger.error("Chyba Constraint Violation {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(e.getMessage()));
    }
    
    /**
     * Osetreni dalsiho typu vyjimky v definovanych v me aplikaci 
     *
     */
    @ExceptionHandler
    public ResponseEntity<Message> handleOtherExceptions(Exception e) {
        // Co se ma poslat v html body v pripade vyjimky. HttpStatus.NOT_FOUND generuje response/status kod 404
        logger.error("Chyba {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(new Message(e.getMessage()));
    }
    
    @ExceptionHandler(StorageFileException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileException exc) {
        return ResponseEntity.notFound().build();
    }
    
    
}