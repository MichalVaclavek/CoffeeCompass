package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;
import cz.fungisoft.coffeecompass.service.FirebaseNotificationService;
import cz.fungisoft.coffeecompass.service.PushNotificationService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class PushNotificationServiceImpl implements PushNotificationService {
    
    private FirebaseNotificationService fcmService;
    
    public PushNotificationServiceImpl(FirebaseNotificationService fcmService) {
        this.fcmService = fcmService;
    }
    
    @Override
    public void sendPushNotification(PushNotificationRequest request) {
        try {
            fcmService.sendMessageWithData(getSamplePayloadData(), request);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    
    @Override
    public void sendPushNotificationWithoutData(PushNotificationRequest request) {
        try {
            fcmService.sendMessageWithoutData(request);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    
    @Override
    public void sendPushNotificationToToken(PushNotificationRequest request) {
        try {
            fcmService.sendMessageToToken(request);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    
    
    private Map<String, String> getSamplePayloadData() {
        Map<String, String> pushData = new HashMap<>();
        pushData.put("messageId", "msgid");
        pushData.put("text", "txt");
        pushData.put("user", "goro");
        return pushData;
    }
}
