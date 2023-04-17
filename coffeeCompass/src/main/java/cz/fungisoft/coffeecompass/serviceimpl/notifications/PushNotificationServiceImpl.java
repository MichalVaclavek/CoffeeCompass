package cz.fungisoft.coffeecompass.serviceimpl.notifications;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;
import cz.fungisoft.coffeecompass.service.notifications.FirebaseNotificationService;
import cz.fungisoft.coffeecompass.service.notifications.PushNotificationService;
import lombok.extern.log4j.Log4j2;

/**
 * Not used in current implementation as instant sending of push notifications
 * is not needed.<br>
 */
@Log4j2
@Service
public class PushNotificationServiceImpl implements PushNotificationService {
    
    private final FirebaseNotificationService fcmService;
    
    private static final String PUSH_NOTIFICATION_ERROR = "Push notification message sent error: {}";
    
    
    public PushNotificationServiceImpl(FirebaseNotificationService fcmService) {
        this.fcmService = fcmService;
    }
    
    @Override
    public void sendPushNotificationWithData(Map<String, String> data, PushNotificationRequest request) {
        try {
            fcmService.sendMessageWithDataToTopic(data, request);
        } catch (InterruptedException e) {
            log.error(PUSH_NOTIFICATION_ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            log.error(PUSH_NOTIFICATION_ERROR, e.getMessage());
        }
    }
    
    /**
     * Used to send push notifications to Topic via Firebase. Tokens/equipments to receive
     * such notifications subscribed to this Topic earlier using fcmService.subscribeToTopic(registrationTokens, topic);
     * This method is invoked by Service which observes creating/deleting of CoffeeSites 
     */
    @Override
    public void sendPushNotificationWithoutData(PushNotificationRequest request) {
        try {
            fcmService.sendMessageWithoutDataToTopic(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(PUSH_NOTIFICATION_ERROR, e.getMessage());
        } catch (ExecutionException | TimeoutException e) {
            log.error(PUSH_NOTIFICATION_ERROR, e.getMessage());
        }
    }
    
    @Override
    public void sendPushNotificationToToken(PushNotificationRequest request) {
        try {
            fcmService.sendMessageToToken(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(PUSH_NOTIFICATION_ERROR, e.getMessage());
        } catch (ExecutionException | TimeoutException e) {
            log.error(PUSH_NOTIFICATION_ERROR, e.getMessage());
        }
    }
}
