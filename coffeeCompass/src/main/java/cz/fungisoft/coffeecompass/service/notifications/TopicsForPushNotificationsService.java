package cz.fungisoft.coffeecompass.service.notifications;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import cz.fungisoft.coffeecompass.entity.DeviceFirebaseToken;
import cz.fungisoft.coffeecompass.entity.FirebaseTopic;
import cz.fungisoft.coffeecompass.entity.User;


/**
 * Service to save/delete and get Topics for push notifications
 * about new/deleted CoffeeSites
 * 
 * @author Michal V.
 *
 */
public interface TopicsForPushNotificationsService {
    
    FirebaseTopic saveTopicAndSubtopic(String topic, String subTopic);
    
    Optional<FirebaseTopic> getOneTopicSubtopic(String mainTopic, String allTownsSubTopic);
    
    List<DeviceFirebaseToken> getTokensSubscribed(String mainTopic, String subTopic);
    
    Set<DeviceFirebaseToken> getTokensSubscribed(int topicId);
    
    /**
     * Retrieval of the FirebaseTopics assigned to all tokens of one user
     * 
     * @param user
     * @return
     */
    List<FirebaseTopic> getTopicsForUser(User user);
    
    /**
     * Retrieval of the FirebaseTopics as one String (mainTopic_subTopic) assigned to all tokens of one user
     * 
     * @param user
     * @return
     */
    List<String> getTopicsOneStringForUser(User user);
    
    /**
     * Retrieval of the FirebaseTopic's IDs assigned to all tokens of one user
     * 
     * @param user
     * @return
     */
    List<Integer> getTopicIdsForUser(User user);
    
    /**
     * Retrieval of the FirebaseTopics assigned to onr token
     * 
     * @param token
     * @return
     */
    List<FirebaseTopic> getTopicsForToken(String token);
    
    /**
     * Retrieval of the FirebaseTopics as one String (mainTopic_subTopic) assigned to one token
     * 
     * @param token
     * @return
     */
    List<String> getTopicsOneStringForToken(String token);
    
    /**
     * Retrieval of the FirebaseTopic's IDs assigned to one token
     * 
     * @param token
     * @return
     */
    List<Integer> getTopicIdsForToken(String token);
    
    
    void deleteTopicsOfUser(User user);
    
    void deleteAllTopicsOfToken(String tokenString);

    void deleteSelectedTopicsOfToken(String tokenString, List<String> topics);

    void deleteTopicAndSubtopic(String topic, String subTopic);
}
