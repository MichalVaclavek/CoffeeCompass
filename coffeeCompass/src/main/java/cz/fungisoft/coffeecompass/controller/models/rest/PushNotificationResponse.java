package cz.fungisoft.coffeecompass.controller.models.rest;

/**
 * This class should represent response to PushNotificationRequest<br>
 * or PushNotificationSubscriptionRequest sent to Firebase previously.
 * 
 * @author Michal V.
 *
 */
public class PushNotificationResponse {
    
    private int status;
    private String message;
    
    public PushNotificationResponse() {
    }
    
    public PushNotificationResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
