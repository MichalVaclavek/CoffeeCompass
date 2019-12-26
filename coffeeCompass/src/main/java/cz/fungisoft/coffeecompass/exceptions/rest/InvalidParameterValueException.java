package cz.fungisoft.coffeecompass.exceptions.rest;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

/**
 * Exception class to send error message to client in case of POST, PUT requests,
 * when the parametr value is not valid, cannot be used etc.
 * 
 * @author Michal V.
 *
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidParameterValueException extends RESTException {
    
    private static final long serialVersionUID = -3507077783107847764L;
    
    // Name of resource, like User, CoffeeSite etc
    private String resourceName;
    // name of resource field which is invalid
    private String fieldName;
    // invalid value of the field
    private Object fieldValue;
    

    public InvalidParameterValueException(String resourceName, String fieldName, Object fieldValue, String localizedErrorMessage) {
        super(String.format("Invalid value '%s' of the parameter '%s' for resource '%s'.", fieldValue, fieldName, resourceName));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.localizedErrorMessage = localizedErrorMessage;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
    
}