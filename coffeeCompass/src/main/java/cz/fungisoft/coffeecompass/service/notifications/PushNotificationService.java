package cz.fungisoft.coffeecompass.service.notifications;

import java.util.Map;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;

/**
 * Interface to define methods to sent instant push notification messages to Topic and/or token.
 * 
 * @author Michal Vaclavek
 *
 */public interface PushNotificationService {
    
    void sendPushNotificationWithData(Map<String, String> data, PushNotificationRequest request);
        
    void sendPushNotificationWithoutData(PushNotificationRequest request);
    
    void sendPushNotificationToToken(PushNotificationRequest request);
    
}
