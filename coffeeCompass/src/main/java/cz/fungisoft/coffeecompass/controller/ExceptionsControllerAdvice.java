package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.exception.EntityNotFoundException;
import cz.fungisoft.coffeecompass.exception.StorageFileException;
import cz.fungisoft.coffeecompass.pojo.Message;

import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Centralizovane zachycovani vyjimek a prirazovani textu a View vsem druhum vyjimek.
 */
//@RestControllerAdvice
@ControllerAdvice
public class ExceptionsControllerAdvice extends ResponseEntityExceptionHandler
{
    private static final Logger logger = LogManager.getLogger(ExceptionsControllerAdvice.class);
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntNotFound(EntityNotFoundException e) {
        logger.error("Chyba Entity Not Found {}", e.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject("errorMessage", e.getMessage());
        model.setViewName("error/404");
        return model;
    }

    /**
     * Probably not used yet. 
     * @param e
     * @return
     */
    /*
    @ExceptionHandler(ConstraintViolationException.class)  
    public ResponseEntity<Message> handleConstraintViolation(ConstraintViolationException e) {
        logger.error("Chyba Constraint Violation {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(e.getMessage()));
    }
    
    @ExceptionHandler(StorageFileException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileException exc) {
        return ResponseEntity.notFound().build();
    }
    */
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView MaxUploadSizeExceeded(MaxUploadSizeExceededException exc) {
        
        logger.error("MaxUploadSizeExceeded {}", exc.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject("errorMessage", exc.getMessage());
        model.getModel().put("fileTooLargeError", "File too large!");
        model.setViewName("error");
//        model.setViewName("/showSite/");
        return model;
    }
    
    /**
     * Osetreni ostatnich vyjimky nedefinovanych v me aplikaci 
     */
    @ExceptionHandler
    public ModelAndView handleOtherExceptions(Exception e) {
        
        logger.error("Chyba {}", e.getMessage());
        
        ModelAndView model = new ModelAndView();
        model.addObject("errorMessage", e.getMessage());
        model.setViewName("error");
        return model;
    }
    
}