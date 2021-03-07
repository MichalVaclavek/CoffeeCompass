package cz.fungisoft.coffeecompass.serviceimpl.notifications;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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
import cz.fungisoft.coffeecompass.service.notifications.FirebaseNotificationService;
import lombok.extern.log4j.Log4j2;

/**
 * Class/Service to immediate sending of messages/notifications over Firebase to given Topics for 
 * given Firebase/device tokens.
 * <p>
 * There are few variants/methods how to send such notification, either to Topic, to Tokens,
 * with or without additional data. Not all are used in current implementation.
 * <p>
 * Also contains methods for notification messages subscription and unsusbscription.
 * 
 * @author Michal V.
 *
 */
@Log4j2
@Service
public class FirebaseServiceImpl implements FirebaseNotificationService {
    
    @Value("${app.firebase.notificationmessage.timetolive.mins:720}") // 720 default
    private long pushMessageTtlMins;// push message notification time to live 
    
    /**
     * Method to send notification message with additional data to Token
     * 
     * @param data - map with key and value data to be sent to recepient
     * @param request - request containing Topic, token, message and title
     */
    @Override
    public void sendMessageWithDataToToken(Map<String, String> data, PushNotificationRequest request) throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithDataToToken(data, request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
        log.info("Push notification message sent with data to tokens. Topic: {}, {}, msg: {}", request.getTopic(), response, jsonOutput);
    }
    
    /**
     * Method to send notification message with additional data to Topic
     * 
     * @param data - map with key and value data to be sent to recepient
     * @param request - request containing Topic, token, message and title
     */
    @Override
    public void sendMessageWithDataToTopic(Map<String, String> data, PushNotificationRequest request) throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithDataToTopic(data, request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
        log.info("Push notification message sent with data. Topic: {}, {}, msg: {}", request.getTopic(), response, jsonOutput);
    }
    
    @Override
    public void sendMessageWithoutDataToTopic(PushNotificationRequest request) throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithoutDataToTopic(request);
        String response = sendAndGetResponse(message);
        log.info("Push notification message sent without data.. Topic: {}, {}", request.getTopic(), response);
    }
    
    @Override
    public void sendMessageToToken(PushNotificationRequest request) throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithoutDataToToken(request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
        log.info("Push notification message sent to token. Device token: {}, {}, msg: {}", request.getToken(), response, jsonOutput);
    }
    
    /**
     * Subscribe the devices, corresponding to the registration tokens, to the topic.
     */
    @Override
    public void subscribeToTopic(List<String> registrationTokens, String topic) throws InterruptedException, ExecutionException {
        TopicManagementResponse response = FirebaseMessaging.getInstance()
                                                            .subscribeToTopicAsync(registrationTokens, topic)
                                                            .get();
        // See the TopicManagementResponse reference documentation  for the contents of response.
        log.info("Number of tokens registration to topic '{}': {}. Response: {}", topic, response.getSuccessCount(), response);
    }
    
    /**
     * Unsubscribe the devices corresponding to the registration tokens from the topic.
     */
    @Override
    public void unsubscribeFromTopic(List<String> registrationTokens, String topic) throws InterruptedException, ExecutionException {
        TopicManagementResponse response = FirebaseMessaging.getInstance()
                                                            .unsubscribeFromTopicAsync(registrationTokens, topic)
                                                            .get();
        // See the TopicManagementResponse reference documentation for the contents of response.
        log.info("Number of Tokens unregistrated from topic '{}': {}. Response: ", topic, response.getSuccessCount(), response);
    }
    
    /* *** Supporting methods preparing Firebase Message *** */
    
    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }
    
    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(pushMessageTtlMins).toMillis()).setCollapseKey(topic) // time to live for message to be delivered
                .setPriority(AndroidConfig.Priority.NORMAL)
                .setNotification(AndroidNotification.builder().setTag(topic).build()).build();
    }
    
    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }
    
    private Message getPreconfiguredMessageWithoutDataToToken(PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).setToken(request.getToken())
                .build();
    }
    
    private Message getPreconfiguredMessageWithoutDataToTopic(PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).setTopic(request.getTopic())
                .build();
    }
    
    private Message getPreconfiguredMessageWithDataToToken(Map<String, String> data, PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).putAllData(data).setToken(request.getToken())
                .build();
    }
    
    private Message getPreconfiguredMessageWithDataToTopic(Map<String, String> data, PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).putAllData(data).setTopic(request.getTopic())
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
