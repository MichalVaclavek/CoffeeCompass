package cz.fungisoft.users.controllers;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class to hold an Error response description in case of REST.<br>
 * Based on RFC7807 and https://www.baeldung.com/rest-api-error-handling-best-practices
 * <p>
 * This schema is composed of five parts:<br>

 *   type — A URI identifier that categorizes the error<br>
 *   title — A brief, human-readable message about the error<br>
 *   status — The HTTP response code (optional)<br>
 *   detail — A human-readable explanation of the error<br>
 *   instance — A URI that identifies the specific occurrence of the error<br>
 *   Instead of using our custom error response body, we can convert our body to:<br>
 * <p>
 *   Example:
 *   
 *    {
 *       "type": "/errors/incorrect-user-pass",<br>
 *       "title": "Incorrect username or password.",<br>
 *       "status": 403,<br>
 *       "detail": "Authentication failed due to incorrect username or password.",<br>
 *       "instance": "/login/log/abc123"<br>
 *   }
 *   
 *   Doplneno o parametry: errorParameter, errorParameterValue
 *   pro pripad chyb pri validaci vstupnich parametru POST a PUT requestu
 * 
 * @author Michal Vaclavek
 *
 */
@Data
public class CommonRestError {
    
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    
    private String errorParameter = "";
    private String errorParameterValue = "";
    
    /**
     * Used for list of input fields validation errors
     */
    private Map<String, Set<String>> errorParametersMap;
    
    /**
     * Default constructor with default field values
     */
    public CommonRestError() {

        this.type = "/errors/general";
        this.title = "General error";
        this.status = 400;
        this.detail = "No detail info available.";
        this.instance = "/";
    }
 
    public CommonRestError(String message) {
        super();
        this.title = message;
    }
 
    public CommonRestError(String message, String error) {
        super();
        this.title = message;
        this.detail = error;
    }

    public CommonRestError(String type, String title, Integer status, String detail, String instance) {
        super();
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }
    
    /**
     * Mapping from default Spring error attributes to my Rest error attributes.<br>
     * Used in case an Exception is thrown by Spring and processed by standard<br>
     * Spring exception handling mechanism, if there are no ControllerAdvices defined for the Exception.<br>
     * 
     * @param defaultErrorAttributes
     * @return
     */
    public static CommonRestError fromDefaultAttributeMap(Map<String, Object> defaultErrorAttributes) {
        // original attribute values are documented in org.springframework.boot.web.servlet.error.DefaultErrorAttributes
        return new CommonRestError((String) defaultErrorAttributes.getOrDefault("exception", "Spring error"),
                                   (String) defaultErrorAttributes.getOrDefault("error", "no reason available"),
                                   ((Integer) defaultErrorAttributes.get("status")),
                                   (String) defaultErrorAttributes.getOrDefault("message", "no detail message available"),
                                   (String) defaultErrorAttributes.getOrDefault("path", "no instance available"));
    }

    // Utility method to return a map of serialized root attributes
    public Map<String, Object> toAttributeMap() {
        
        Map<String, Object> attributesMap = new HashMap<>();
        
        attributesMap.put("instance", instance);
        attributesMap.put("detail", detail);
        attributesMap.put("status", String.valueOf(status));
        attributesMap.put("title", title);
        attributesMap.put("type", type);
        
        return attributesMap;
    }
}
