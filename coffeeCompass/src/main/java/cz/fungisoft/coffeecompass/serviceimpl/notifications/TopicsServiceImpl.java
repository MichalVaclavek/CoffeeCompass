package cz.fungisoft.coffeecompass.serviceimpl.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.DeviceFirebaseToken;
import cz.fungisoft.coffeecompass.entity.FirebaseTopic;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.repository.FirebaseDeviceTokenRepository;
import cz.fungisoft.coffeecompass.repository.FirebaseTopicRepository;
import cz.fungisoft.coffeecompass.service.notifications.TopicsForPushNotificationsService;
import lombok.extern.log4j.Log4j2;

/**
 * Saves/deletes Topics for push notifications. Can retrieve Topics according token and/or user's tokens.
 * 
 * @author Michal Vaclavek
 *
 */
@Service
@Transactional
@Log4j2
public class TopicsServiceImpl implements TopicsForPushNotificationsService {

    private final FirebaseTopicRepository firebaseTopicRepository;
    
    private final FirebaseDeviceTokenRepository firebaseDeviceTokenRepository;
    
    private final MessageSource messages;
    
    /**
     * 
     * @param firebaseTopicRepository
     * @param firebaseDeviceTokenRepository
     */
    public TopicsServiceImpl(FirebaseTopicRepository firebaseTopicRepository,
                             FirebaseDeviceTokenRepository firebaseDeviceTokenRepository,
                             MessageSource messages) {
        super();
        this.firebaseTopicRepository = firebaseTopicRepository;
        this.firebaseDeviceTokenRepository = firebaseDeviceTokenRepository;
        this.messages = messages;
    }
    
    /**
     * To create and save special 'new_coffeeSite_all_towns' topic
     */
    @PostConstruct
    public void initialize() {
        String mainTopic = messages.getMessage("push.notification.main.topic.new_coffeesite", null, new Locale("cs"));
        String allTownsSubTopic = messages.getMessage("push.notification.subTopic.all_towns", null, new Locale("cs"));
        // Is this special topic_subTopic already in DB? If not save
        Optional<FirebaseTopic> allTownsTopic =  getOneTopicSubtopic(mainTopic, allTownsSubTopic);
        if (!allTownsTopic.isPresent()) {
            saveTopicAndSubtopic(mainTopic, allTownsSubTopic);
        }
    }
    
    @Override
    public Optional<FirebaseTopic> getOneTopicSubtopic(String mainTopic, String allTownsSubTopic) {
        return firebaseTopicRepository.getOneTopicSubtopic(mainTopic, allTownsSubTopic);
    }

    @Override
    public FirebaseTopic saveTopicAndSubtopic(String topic, String subTopic) {
        // check if the topic is already saved or not
        Optional<FirebaseTopic> fbTopic = firebaseTopicRepository.getOneTopicSubtopic(topic, subTopic);
        
        if (!fbTopic.isPresent()) {
            FirebaseTopic firebaseTopic = new FirebaseTopic(topic, subTopic);
            firebaseTopic.setId(0); // new DB entry
            log.info("New topic/subTopic saved: {}_{}", topic, subTopic);
            return firebaseTopicRepository.save(firebaseTopic);
        } else {
            return fbTopic.get();
        }
    }
    
    /**
     * Returns all Tokens subscribed for the mainTopic, subTopic combination
     */
    @Override
    public List<DeviceFirebaseToken> getTokensSubscribed(String mainTopic, String subTopic) {
        List<DeviceFirebaseToken> result = new ArrayList<>();
        Optional<FirebaseTopic> topic = firebaseTopicRepository.getOneTopicSubtopic(mainTopic, subTopic);
        if (topic.isPresent()) {
            result = new ArrayList<>(topic.get().getTokens());
        }
        return result;
    }
    
    /**
     * Returns all Tokens subscribed for the mainTopic, subTopic combination
     */
    @Override
    public List<DeviceFirebaseToken> getTokensSubscribed(int topicId) {
        List<DeviceFirebaseToken> result = new ArrayList<>();
        try {
            FirebaseTopic topic = firebaseTopicRepository.getOne(topicId);
            result = new ArrayList<>(topic.getTokens());
        } catch (Exception ex) {
            log.error("DeviceFirebaseToken not found in DB for id {}", topicId);
        }
        return result;
    }
    
    /* ****** Retrieving User's tokens Topics ************* */

    @Override
    public List<FirebaseTopic> getTopicsForUser(User user) {
        List<DeviceFirebaseToken> userTokens = firebaseDeviceTokenRepository.getAllTokensForUser(user.getId());
        return userTokens.stream().flatMap(token -> token.getTopics().stream()).collect(Collectors.toList());
    }
    
    @Override
    public List<String> getTopicsOneStringForUser(User user) {
        List<DeviceFirebaseToken> userTokens = firebaseDeviceTokenRepository.getAllTokensForUser(user.getId());
        return userTokens.stream()
                         .flatMap(token -> token.getTopics().stream())
                         .map(firebaseTopic -> firebaseTopic.getMainTopic() + "_" + firebaseTopic.getSubTopic())
                         .collect(Collectors.toList());
    }
    
    @Override
    public List<Integer> getTopicIdsForUser(User user) {
        List<DeviceFirebaseToken> userTokens = firebaseDeviceTokenRepository.getAllTokensForUser(user.getId());
        return userTokens.stream()
                         .flatMap(token -> token.getTopics().stream())
                         .map(FirebaseTopic::getId)
                         .collect(Collectors.toList());
    }

    /* ****** Retrieving One Token Topics ************* */
    
    @Override
    public List<FirebaseTopic> getTopicsForToken(String tokenString) {
        List<FirebaseTopic> result = new ArrayList<>();
        firebaseDeviceTokenRepository.getOneToken(tokenString)
                                     .ifPresent(token -> result.addAll(token.getTopics()));
        return result;
    }

    @Override
    public List<String> getTopicsOneStringForToken(String tokenString) {
        List<String> result = new ArrayList<>();
        firebaseDeviceTokenRepository.getOneToken(tokenString)
                                     .ifPresent(token -> result.addAll(token.getTopics()
                                                                            .stream()
                                                                            .map(firebaseTopic -> firebaseTopic.getMainTopic() + "_" + firebaseTopic.getSubTopic())
                                                                            .collect(Collectors.toList())
                                                                      )
                                     );
       return result;
    }
    

    @Override
    public List<Integer> getTopicIdsForToken(String tokenString) {
        List<Integer> result = new ArrayList<>();
        firebaseDeviceTokenRepository.getOneToken(tokenString)
                                     .ifPresent(token -> result.addAll(token.getTopics()
                                                                            .stream()
                                                                            .map(FirebaseTopic::getId)
                                                                            .collect(Collectors.toList())
                                                                      )
                                     );
       return result;
    }
    
    

    @Override
    public void deleteTopicsOfUser(User user) {
        List<DeviceFirebaseToken> userTokens = firebaseDeviceTokenRepository.getAllTokensForUser(user.getId());
        userTokens.forEach(token -> token.getTopics().clear());
        log.info("All subscribed Topics for user deleted. User: {}", user.getUserName());
    }

    /**
     * Deletes ALL Topics/subTopics related to the token.
     *
     * @param tokenString
     */
    @Override
    public void deleteAllTopicsOfToken(String tokenString) {
        firebaseDeviceTokenRepository.getOneToken(tokenString)
                                     .ifPresent(token -> token.getTopics().clear());
        log.info("All subscribed Topics for the Token deleted. Token: {}", tokenString);
    }

    /**
     * Deletes selected Topics/subTopics related to the token.
     *
     * @param tokenString
     */
    @Override
    public void deleteSelectedTopicsOfToken(String tokenString, List<String> topics) {
        firebaseDeviceTokenRepository.getOneToken(tokenString)
                .ifPresent(token -> token.getTopics().removeIf(topic -> topics.stream().anyMatch(topic::equals)));

        log.info("Selected Topics for the Token deleted. Token: {}", tokenString);
    }

    @Override
    public void deleteTopicAndSubtopic(String topic, String subTopic) {
        firebaseTopicRepository.deleteTopic(topic, subTopic);
        log.info("Main Topic {} and subTopic {} deleted", topic, subTopic);
    }
}
