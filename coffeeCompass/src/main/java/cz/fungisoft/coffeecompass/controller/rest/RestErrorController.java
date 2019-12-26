package cz.fungisoft.coffeecompass.controller.rest;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.ExceptionsControllerAdvice;
import cz.fungisoft.coffeecompass.controller.ExceptionsControllerRESTAdvice;

/**
 * Controller pro obsluhu obecnych chyb. Pouzije se namisto "Whiteable Error Page".<br>
 * Aplikace tedy bude vzdy vracet REST JSON response v pripade, ze dojde k nejake<br>
 * vyjimce, kterou neosetruji {@link ExceptionsControllerAdvice} nebo<br>.
 * {@link ExceptionsControllerRESTAdvice}
 * 
 * @author Michal Vaclavek
 *
 */
@RestController
@RequestMapping({RestErrorController.REST_ERROR_PATH})
public class RestErrorController extends AbstractErrorController
{
    
    static final String REST_ERROR_PATH = "/error";
    
    public RestErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = this.getErrorAttributes(request, false);
        HttpStatus status = this.getStatus(request);
        return new ResponseEntity<>(body, status);
    }
 
    @Override
    public String getErrorPath() {
        return REST_ERROR_PATH;
    }

}
