package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;
import cz.fungisoft.coffeecompass.service.FirebaseNotificationService;

import lombok.extern.log4j.Log4j2;

/**
 * Class to perform sending messages / notifications over Firebase to topics and firebase tokens.
 * 
 * @author Michal V.
 *
 */
@Log4j2
@Service
public class FirebaseServiceImpl implements FirebaseNotificationService {
    
    
    @Override
    public void sendMessageWithData(Map<String, String> data, PushNotificationRequest request) throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithData(data, request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
        log.info("Sent message with data. Topic: " + request.getTopic() + ", " + response+ " msg " + jsonOutput);
    }
    
    @Override
    public void sendMessageWithoutData(PushNotificationRequest request)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithoutData(request);
        String response = sendAndGetResponse(message);
        log.info("Sent message without data. Topic: " + request.getTopic() + ", " + response);
    }
    
    @Override
    public void sendMessageToToken(PushNotificationRequest request)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageToToken(request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
        log.info("Sent message to token. Device token: " + request.getToken() + ", " + response+ " msg " + jsonOutput);
    }
    
    /**
     * Subscribe the devices corresponding to the registration tokens to the topic.
     */
    @Override
    public void subscribeToTopic(List<String> registrationTokens, String topic) throws Exception {
       
        TopicManagementResponse response = FirebaseMessaging.getInstance()
                                                            .subscribeToTopicAsync(registrationTokens, topic)
                                                            .get();
        // See the TopicManagementResponse reference documentation  for the contents of response.
        log.info("Token registration to topic: " + response.getSuccessCount() + ", " + response);
    }
    
    /**
     * Unsubscribe the devices corresponding to the registration tokens from the topic.
     */
    @Override
    public void unsubscribeFromTopic(List<String> registrationTokens, String topic) throws Exception {
        
        TopicManagementResponse response = FirebaseMessaging.getInstance()
                                                            .unsubscribeFromTopicAsync(registrationTokens, topic)
                                                            .get();
        // See the TopicManagementResponse reference documentation for the contents of response.
        log.info("Token unregistration from topic: " + response.getSuccessCount() + ", " + response);
    }
    
    
    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }
    
    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.NORMAL)
                .setNotification(AndroidNotification.builder().setTag(topic).build()).build(); // colorAkcent
    }
    
    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }
    
    private Message getPreconfiguredMessageToToken(PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).setToken(request.getToken())
                .build();
    }
    
    private Message getPreconfiguredMessageWithoutData(PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).setTopic(request.getTopic())
                .build();
    }
    
    private Message getPreconfiguredMessageWithData(Map<String, String> data, PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).putAllData(data).setToken(request.getToken())
                .build();
    }
    
    private Message.Builder getPreconfiguredMessageBuilder(PushNotificationRequest request) {
        AndroidConfig androidConfig = getAndroidConfig(request.getTopic());
        ApnsConfig apnsConfig = getApnsConfig(request.getTopic());
        return Message.builder()
                .setApnsConfig(apnsConfig).setAndroidConfig(androidConfig).setNotification(
                        new Notification(request.getTitle(), request.getMessage()));
    }

}
