package cz.fungisoft.coffeecompass.listeners;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.geocoding.GeoCodingService;
import cz.fungisoft.coffeecompass.service.notifications.FirebaseNotificationService;
import cz.fungisoft.coffeecompass.service.notifications.TopicsForPushNotificationsService;
import lombok.extern.log4j.Log4j2;

/**
 * Listens for new activated CoffeeSites events and sends push notifications via Firebase<br>
 * to those tokens, which has requested new CoffeeSites notifications in the town of the CoffeeSite.
 * <p>
 * @author Michal Vaclavek
 *
 */
@Log4j2
@Component
public class NewCoffeeSiteEventListener implements ApplicationListener<OnNewCoffeeSiteEvent> {
    
    private final FirebaseNotificationService notificationService;
    
    private final TopicsForPushNotificationsService firebaseTopicService;
    
    private final MessageSource messages;
    
    private final GeoCodingService geoCodingService;
    
    
    public NewCoffeeSiteEventListener(FirebaseNotificationService notificationService,
                                      TopicsForPushNotificationsService firebaseTopicService,
                                      MessageSource messages, GeoCodingService geoCodingService) {
        super();
        this.notificationService = notificationService;
        this.firebaseTopicService = firebaseTopicService;
        this.messages = messages;
        this.geoCodingService = geoCodingService;
    }

    @Override
    public void onApplicationEvent(OnNewCoffeeSiteEvent event) {
        this.sendNotifications(event);
    }
 
    /**
     * Main processing OnNewCoffeeSiteEvent event method.
     * 
     * @param event
     */
    private void sendNotifications(OnNewCoffeeSiteEvent event) {
        // checks if this new CoffeeSite activation was already notified
        Optional.ofNullable(event.getCoffeeSite())
                .ifPresent(newCoffeeSite -> {
                    if (!newCoffeeSite.isNewSitePushNotificationSent()) {
                        String town = geoCodingService.getTownName(newCoffeeSite.getZemSirka(), newCoffeeSite.getZemDelka());
                        town = town.isEmpty() ? newCoffeeSite.getMesto() : town;
                        if (!town.isEmpty()) {
                            sendNotificationsToSubscribedTokens(newCoffeeSite, town);
                        }
                    }
                });
    }
    

    /**
     * Sends push notification message to Topic, with additional data made of town and coffeeSiteId
     * 
     * @param topic
     * @param town
     * @param coffeeSiteId
     */
    private synchronized void sendNotifications(String topic, String town, long coffeeSiteId) {
        // 2. Prepare data 
        Map<String, String> data = getCoffeeSiteNotificationData(coffeeSiteId, town, topic);
        // 3. Prepare request
        PushNotificationRequest request = prepareNotificationRequest(town, topic);
        try { // 4. sent notifications to all tokens susbcribed for the Topic
            notificationService.sendMessageWithDataToTopic(data, request);
        } catch (InterruptedException e) {
            log.debug("Error sending notification message for CoffeeSite id: {} in {} town.", coffeeSiteId, town);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
           log.debug("Error sending notification message for CoffeeSite id: {} in {} town.", coffeeSiteId, town);
        }
    }
    
    
    /**
     * Prepares PushNotificationRequest 
     * 
     * @param town
     * @param topic
     * @return
     */
    private PushNotificationRequest prepareNotificationRequest(String town, String topic) {
        PushNotificationRequest request = new PushNotificationRequest();
        request.setTitle(messages.getMessage("push.notification.title.new_coffeesite", null, new Locale("cs")));
        request.setMessage(town);
        request.setTopic(topic);
        return request;
    }
    
    
    /**
     * Prepares notification data
     * 
     * @param coffeeSiteId - coffeeSite id
     * @param town
     * @param topic
     * @return
     */
    private Map<String, String> getCoffeeSiteNotificationData(long coffeeSiteId, String town, String topic) {
        Map<String, String> pushData = new HashMap<>();
        pushData.put("topic", topic);
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestUri();
        UriComponentsBuilder extBuilder = builder.replacePath("/rest/site/" + coffeeSiteId);
        String csURL = extBuilder.build().toUriString();
        pushData.put("coffeeSiteURL", csURL);
        pushData.put("town", town);
        return pushData;
    }
    
    
    /**
     * Sends push notifications about new CoffeeSite created in the town to all Tokens
     * susbcribed to receive such notification Topic.
     * 
     * @param newCoffeeSite - coffeeSite, created in the town, about which we want to notify those subscribed 
     * @param town - town, where the CoffeeSite were created
     */
    private void sendNotificationsToSubscribedTokens(final CoffeeSite newCoffeeSite, final String town) {

        final String mainTopic = messages.getMessage("push.notification.main.topic.new_coffeesite", null, new Locale("cs"));
        final String allTownsSubTopic = messages.getMessage("push.notification.subTopic.all_towns", null, new Locale("cs"));
        
        // The topic subscribed to Firebase is composed of mainTopic and Id of the town's Topic from DB
        firebaseTopicService.getOneTopicSubtopic(mainTopic, town) 
            .ifPresent(firebaseTopic -> {
                // are there tokens subscribed for this specific town topic
                if (!firebaseTopicService.getTokensSubscribed(firebaseTopic.getId()).isEmpty()) { 
                    String topic = mainTopic + "_" + firebaseTopic.getId();
                    sendNotifications(topic, town, newCoffeeSite.getId());
                }
        });
        // there can be also 'all_towns' subscriptions
        firebaseTopicService.getOneTopicSubtopic(mainTopic, allTownsSubTopic)
                .ifPresent(firebaseTopic -> { 
                    if (!firebaseTopicService.getTokensSubscribed(firebaseTopic.getId()).isEmpty()) { 
                        String topic = mainTopic + "_" + firebaseTopic.getId();
                        sendNotifications(topic, town, newCoffeeSite.getId());
                    }
                });
        
        newCoffeeSite.setNewSitePushNotificationSent(true);
    }
}
