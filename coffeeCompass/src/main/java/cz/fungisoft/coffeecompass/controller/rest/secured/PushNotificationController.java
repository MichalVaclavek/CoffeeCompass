package cz.fungisoft.coffeecompass.controller.rest.secured;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;
import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationResponse;
import cz.fungisoft.coffeecompass.service.PushNotificationService;
import io.swagger.annotations.Api;

@Api // Swagger
@RestController
@RequestMapping("/rest/secured/firebase")
public class PushNotificationController {
    
    private PushNotificationService pushNotificationService;
    
    public PushNotificationController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }
    
    @PostMapping("/notification/topic")
    public ResponseEntity<PushNotificationResponse> sendNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationWithoutData(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }
    
    @PostMapping("/notification/token")
    public ResponseEntity<PushNotificationResponse> sendTokenNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationToToken(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }
    
    @PostMapping("/notification/data")
    public ResponseEntity<PushNotificationResponse> sendDataNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotification(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }   
}
