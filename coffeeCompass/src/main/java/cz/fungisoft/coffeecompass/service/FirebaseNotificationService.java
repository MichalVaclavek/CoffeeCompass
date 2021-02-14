package cz.fungisoft.coffeecompass.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;

public interface FirebaseNotificationService {
    
    void sendMessageWithData(Map<String, String> data, PushNotificationRequest request) throws InterruptedException, ExecutionException;
    
    void sendMessageWithoutData(PushNotificationRequest request) throws InterruptedException, ExecutionException;
    
    void sendMessageToToken(PushNotificationRequest request) throws InterruptedException, ExecutionException;

    void subscribeToTopic(List<String> registrationTokens, String topic) throws Exception;

    void unsubscribeFromTopic(List<String> registrationTokens, String topic) throws Exception;
    
}
