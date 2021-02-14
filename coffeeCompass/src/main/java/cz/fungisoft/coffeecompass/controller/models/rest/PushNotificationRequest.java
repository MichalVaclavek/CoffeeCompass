package cz.fungisoft.coffeecompass.controller.models.rest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class to hold request from client/mobile app. to receive notifications
 * about given topic
 * 
 * @author Michal
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
