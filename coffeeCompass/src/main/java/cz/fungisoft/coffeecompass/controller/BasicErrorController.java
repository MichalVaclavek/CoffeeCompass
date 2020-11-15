package cz.fungisoft.coffeecompass.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller pro obsluhu obecnych chyb. Pouzije se namisto "Whiteable Error Page"
 * Probabaly not needed as new RestErrorController is handling such error requests.
 * 
 * @author Michal Vaclavek
 *
 */
//@Controller
public class BasicErrorController implements ErrorController
{
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
         
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            }
        }

        return "error";
    }
 
    @Override
    public String getErrorPath() {
        return "/error";
    }

}
