/**
 * 
 */
package cz.fungisoft.coffeecompass.exceptions;

import java.util.Map;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

import cz.fungisoft.coffeecompass.controller.models.rest.CommonRestError;

/**
 * Class to map from Spring default error attributes to this application's<br>
 * REST error attributes of the class {@link cz.fungisoft.coffeecompass.controller.models.rest.CommonRestError}<br>
 * Based on https://thepracticaldeveloper.com/2019/09/09/custom-error-handling-rest-controllers-spring-boot/#Basics_ControllerAdvice_and_ExceptionHandler_in_Spring
 * 
 * @author Michal Vaclavek
 *
 */
public class GeneralErrorAttributes extends DefaultErrorAttributes
{
 
    public GeneralErrorAttributes() {
        super();
    }
 
    @Override
    public Map<String, Object> getErrorAttributes(final WebRequest webRequest, final boolean includeStackTrace) {
        final Map<String, Object> defaultErrorAttributes = super.getErrorAttributes(webRequest, false);
        final CommonRestError restError = CommonRestError.fromDefaultAttributeMap(defaultErrorAttributes);
        return restError.toAttributeMap();
    }
}
