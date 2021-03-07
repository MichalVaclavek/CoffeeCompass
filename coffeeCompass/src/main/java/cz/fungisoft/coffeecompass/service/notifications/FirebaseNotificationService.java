package cz.fungisoft.coffeecompass.service.notifications;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.security.access.prepost.PreAuthorize;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;

/**
 * Interface to define methods for sending push notification messages or for (un)subscribe of Topic
 * via Firebase.<br>
 * 
 * @author Michal Vaclavek
 *
 */
public interface FirebaseNotificationService {
    
    @PreAuthorize("isAuthenticated()")
    void sendMessageWithDataToToken(Map<String, String> data, PushNotificationRequest request) throws InterruptedException, ExecutionException;
    
    @PreAuthorize("isAuthenticated()")
    void sendMessageWithDataToTopic(Map<String, String> data, PushNotificationRequest request) throws InterruptedException, ExecutionException;
    
    @PreAuthorize("isAuthenticated()")
    void sendMessageWithoutDataToTopic(PushNotificationRequest request) throws InterruptedException, ExecutionException;
    
    @PreAuthorize("isAuthenticated()")
    void sendMessageToToken(PushNotificationRequest request) throws InterruptedException, ExecutionException;

    
    void subscribeToTopic(List<String> registrationTokens, String topic) throws InterruptedException, ExecutionException;

    void unsubscribeFromTopic(List<String> registrationTokens, String topic) throws InterruptedException, ExecutionException;
    
}
