/**
 * 
 */
package cz.fungisoft.users.exceptions;

import cz.fungisoft.coffeecompass.controller.models.rest.CommonRestError;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * Class to map from Spring default error attributes to this application's<br>
 * REST error attributes of the class {@link cz.fungisoft.coffeecompass.controller.models.rest.CommonRestError}<br>
 * Based on https://thepracticaldeveloper.com/2019/09/09/custom-error-handling-rest-controllers-spring-boot/#Basics_ControllerAdvice_and_ExceptionHandler_in_Spring
 * 
 * @author Michal Vaclavek
 *
 */
public class GeneralErrorAttributes extends DefaultErrorAttributes {
 
    public GeneralErrorAttributes() {
        super();
    }
 
    @Override
    public Map<String, Object> getErrorAttributes(final WebRequest webRequest, ErrorAttributeOptions options) {
        final Map<String, Object> defaultErrorAttributes = super.getErrorAttributes(webRequest, options);
        final CommonRestError restError = CommonRestError.fromDefaultAttributeMap(defaultErrorAttributes);
        return restError.toAttributeMap();
    }
}
