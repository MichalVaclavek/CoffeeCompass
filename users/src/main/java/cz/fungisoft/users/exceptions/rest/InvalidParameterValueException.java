package cz.fungisoft.users.exceptions.rest;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exception class to send error message to client in case of POST, PUT requests,
 * when the parametr value is not valid, cannot be used etc.
 * 
 * @author Michal V.
 *
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidParameterValueException extends RESTException {
    
    private static final long serialVersionUID = -3507077783107847764L;
    
    // Name of resource, like User, CoffeeSite etc
    private String resourceName;
    // name of resource field which is invalid
    private String fieldName;
    // invalid value of the field
    private Object fieldValue;
    
    private List<FieldError> fieldErrors;
    
    private Map<String, Set<String>> errorsMap;
   

    public InvalidParameterValueException(String resourceName, String fieldName, Object fieldValue, String localizedErrorMessage) {
        super(String.format("Invalid value '%s' of the parameter '%s' for resource '%s'.", fieldValue, fieldName, resourceName));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.localizedErrorMessage = localizedErrorMessage;
    }
    
    public InvalidParameterValueException(String resourceName, List<FieldError> fieldErrors) {
        super("Invalid input fields value.");
        
        if (!fieldErrors.isEmpty()) {
            this.localizedErrorMessage = fieldErrors.get(0).getDefaultMessage();
    
            this.fieldErrors = fieldErrors;
            this.errorsMap =  fieldErrors.stream().collect(
                    Collectors.groupingBy(FieldError::getField,
                            Collectors.mapping(FieldError::getDefaultMessage, Collectors.toSet())
                    )
            );
        }
        
        this.resourceName = resourceName;
    }
    

    public Map<String, Set<String>> getErrorsMap() {
        return errorsMap;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        if (fieldName == null && this.fieldErrors != null && this.fieldErrors.size() > 0) {
            fieldName = this.fieldErrors.get(0).getField();
        }
        return fieldName;
    }

    public Object getFieldValue() {
        if (fieldValue == null && this.fieldErrors != null && this.fieldErrors.size() > 0) {
            fieldValue = this.fieldErrors.get(0).getRejectedValue();
        }
        return fieldValue;
    }
}