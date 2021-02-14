package cz.fungisoft.coffeecompass.service;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;

public interface PushNotificationService {
    
    void sendPushNotification(PushNotificationRequest request);
        
    void sendPushNotificationWithoutData(PushNotificationRequest request);
    
    void sendPushNotificationToToken(PushNotificationRequest request);
    
}
