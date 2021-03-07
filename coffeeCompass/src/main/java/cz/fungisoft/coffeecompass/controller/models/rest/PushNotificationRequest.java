package cz.fungisoft.coffeecompass.controller.models.rest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class to hold request for sending notifications to given Topic and/or Token.
 * Title and message of the notification can be requested.
 * 
 * @author Michal V.
 *
 */
@Setter
@Getter
@NoArgsConstructor
public class PushNotificationRequest {
    
    private String title;
    private String message;
    private String topic;
    private String token;  
}
