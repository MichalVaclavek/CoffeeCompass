package cz.fungisoft.coffeecompass.serviceimpl.notifications;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationSubscriptionRequest;
import cz.fungisoft.coffeecompass.entity.DeviceFirebaseToken;
import cz.fungisoft.coffeecompass.entity.FirebaseTopic;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.service.geocoding.GeoCodingService;
import cz.fungisoft.coffeecompass.service.notifications.FirebaseDeviceTokenService;
import cz.fungisoft.coffeecompass.service.notifications.FirebaseNotificationService;
import cz.fungisoft.coffeecompass.service.notifications.NotificationSubscriptionService;
import cz.fungisoft.coffeecompass.service.notifications.TopicsForPushNotificationsService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import lombok.extern.log4j.Log4j2;

/**
 * Service to crate subscriptions for new CoffeeSites creation (or CoffeeSites deletition ...)
 * Currently subscribes or unsubscribes for new CoffeeSites creation in given towns.
 * <p>
 * The Topic id used passed to FirebaseNotificationService is the Id of the Topic saved in DB.<br>
 * Topic is created from mainTopic and subTopic, where mainTopic is usually something like 'new_soffeeSite',<br>
 * 'removed_coffeeSite' plus name of town where the CoffeeSite is to be activated or deleted.<br>
 * The only special Topic is the 'new_soffeeSite_all_towns'.
 * MainTopics are saved in messages_xy.properties files.<br>
 * 
 * @author Michal Vaclavek
 *
 */
@Log4j2
@Transactional
@Service
public class NotificationSubscriptionServiceImpl implements NotificationSubscriptionService {
    
    private final FirebaseNotificationService fcmService;
    
    private final FirebaseDeviceTokenService firebaseDeviceTokenService;
    
    private final TopicsForPushNotificationsService topicsService;
    
    private final UserService userService;
    
    private final GeoCodingService geoCodingService;

    private static String ALL_TOWNS_SUBTOPIC;
    
    public NotificationSubscriptionServiceImpl(FirebaseNotificationService fcmService,
                                               FirebaseDeviceTokenService firebaseDeviceTokenService,
                                               TopicsForPushNotificationsService topicsService,
                                               UserService userService, GeoCodingService geoCodingService,
                                               MessageSource messages) {
        super();
        this.fcmService = fcmService;
        this.firebaseDeviceTokenService = firebaseDeviceTokenService;
        this.topicsService = topicsService;
        this.userService = userService;
        this.geoCodingService = geoCodingService;
        ALL_TOWNS_SUBTOPIC = messages.getMessage("push.notification.subTopic.all_towns", null, new Locale("cs"));
    }

    /**
     * Create subscriptions for the Token and Topic/subTopics requested.<br>
     * Before the new subscription is performed, all current subscriptions of the Token are unsubscribed and deleted from DB.<br>
     * Requsted Topics are saved, but before that, validated using GeocodingAPI, to ensure only valid town names are used as a Topic.<br>
     */
    @Override
    public void subscribeToTopic(PushNotificationSubscriptionRequest request) throws InterruptedException, ExecutionException {
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
        final String tokenString = request.getToken();
        
        // 1. Unsubscribe ALL topics currently subscribed for the token
        List<String> unregistrationTokens = new ArrayList<>();
        unregistrationTokens.add(tokenString);
        List<FirebaseTopic> topicsToUnsubscribe = topicsService.getTopicsForToken(tokenString);
        for (FirebaseTopic topic : topicsToUnsubscribe) {
            fcmService.unsubscribeFromTopic(unregistrationTokens, topic.getMainTopic() + "_" + topic.getId());
        }
        // Delete all subscribed topics for the token
        topicsService.deleteAllTopicsOfToken(tokenString);
        
        // 2. save new subscritpions to DB
        // get already saved or new DeviceFirebaseToken object
        DeviceFirebaseToken token = firebaseDeviceTokenService.getOneByTokenString(tokenString)
                                                              .orElseGet(() -> {
                                                                  DeviceFirebaseToken newToken = new DeviceFirebaseToken(tokenString);
                                                                  newToken.setId(0);
                                                                  return newToken;
                                                              });
        // update token with user if logged-in   
        loggedInUser.ifPresent(token::setUser);
           
        // 2.a. saves Topic and subTopics
        Set<FirebaseTopic> newTopics = new HashSet<>();
        if (request.getSubTopics().stream().anyMatch(ALL_TOWNS_SUBTOPIC::equals)) { // if there is 'all_towns' subTopic in the list, ignore all others
            newTopics.add(topicsService.saveTopicAndSubtopic(request.getTopic(), ALL_TOWNS_SUBTOPIC));
        } else {
            request.getSubTopics().stream()
                                  .filter(subTopic -> geoCodingService.validateTownName(subTopic))
                                  .forEach(subTopic -> newTopics.add(topicsService.saveTopicAndSubtopic(request.getTopic(), subTopic)));
        }
        
        // 2.b. update Token with subscribed Topics
        token.setTopics(newTopics);
        
        // 2.c finally save token with topics
        firebaseDeviceTokenService.saveToken(token);
        
        // 3. get Topics for the Token of request from DB
        List<FirebaseTopic> topicsToSubscribe = topicsService.getTopicsForToken(tokenString);
        
        // 4. get tokens of the user or the only token if user is not logged in
        // In current implementation, we use only Topics of a one Token, not of all tokens of the user
        List<String> registrationTokens = new ArrayList<>();
        registrationTokens.add(request.getToken());
        
        // 5. Subscribe the Topics for the token in Firebase
        // Topic consists of: mainTopic String and Id of the mainTopic/subTopic entry 
        for (FirebaseTopic topic : topicsToSubscribe) {
            fcmService.subscribeToTopic(registrationTokens, topic.getMainTopic() + "_" + topic.getId());
        }
    }

    /**
     * To unsusbcribe selected Topics/subtopics of the Token
     */
    @Override
    public void unsubscribeFromTopics(PushNotificationSubscriptionRequest request) throws InterruptedException, ExecutionException {
        List<String> unregistrationTokens = new ArrayList<>();
        unregistrationTokens.add(request.getToken());
        // unsubscribe Topics/subTopics requested for this Token
        List<String> topicSubTopicsToDelete = new ArrayList<>();
        for (String subTopic : request.getSubTopics()) {
            String topicSubTopic = request.getTopic() + "_" + subTopic;
            topicSubTopicsToDelete.add(topicSubTopic);
            fcmService.unsubscribeFromTopic(unregistrationTokens, topicSubTopic);
        }
        // Delete selected subscribed topics for the token
        topicsService.deleteSelectedTopicsOfToken(request.getToken(), topicSubTopicsToDelete);
    }

    @Override
    public void unsubscribeFromAllTopics(String token) throws InterruptedException, ExecutionException {
        // 1. Unsubscribe ALL topics currently subscribed for the token
        List<String> unregistrationTokens = new ArrayList<>();
        unregistrationTokens.add(token);
        List<FirebaseTopic> topicsToUnsubscribe = topicsService.getTopicsForToken(token);
        for (FirebaseTopic topic : topicsToUnsubscribe) {
            fcmService.unsubscribeFromTopic(unregistrationTokens, topic.getMainTopic() + "_" + topic.getId());
        }
        // Delete all subscribed topics for the token
        topicsService.deleteAllTopicsOfToken(token);
    }
}
