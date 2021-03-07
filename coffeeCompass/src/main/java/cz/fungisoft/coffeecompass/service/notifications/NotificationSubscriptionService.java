package cz.fungisoft.coffeecompass.service.notifications;

import java.util.concurrent.ExecutionException;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationSubscriptionRequest;

/**
 * Service to define methods for subscription and unsubscription of the notification messages
 * about new CoffeeSites created in the given town (or generally any other Topic defined in the application features).
 * 
 * @author Michal Vaclavek
 *
 */
public interface NotificationSubscriptionService {
    
    
    /**
     * Subscription for push notifications for list of Topics for one user/token
     * 
     * @param request
     * @throws InterruptedException
     * @throws ExecutionException
     */
    void subscribeToTopic(PushNotificationSubscriptionRequest request) throws InterruptedException, ExecutionException;

    /**
     * Undo Subscription for push notifications for list of Topics for one user/token
     * 
     * @param request
     * @throws InterruptedException
     * @throws ExecutionException
     */
    void unsubscribeFromTopic(PushNotificationSubscriptionRequest request) throws InterruptedException, ExecutionException;
    
}
