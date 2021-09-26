package cz.fungisoft.coffeecompass.controller.rest;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationResponse;
import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationSubscriptionRequest;
import cz.fungisoft.coffeecompass.exceptions.rest.InvalidParameterValueException;
import cz.fungisoft.coffeecompass.service.notifications.NotificationSubscriptionService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

/**
 * REST Controller to handle requests related to Push notifications of Firebase.
 * This is non-secured version of the controller, so can be used without login-in user.
 *  
 * @author Michal Vaclavek
 */
@Api // Swagger
@RestController
@RequestMapping("/rest/firebase")
public class PushNotificationController {
    
    private static final Logger log = LoggerFactory.getLogger(PushNotificationController.class);
    
    private final NotificationSubscriptionService notificationSubscriptionService;
    
    public PushNotificationController(NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }
    
    /**
     * Handles user's subscription request for sending push notification about new CoffeeSite in respective towns.<br>
     * Input is validated according PushNotificationSubscriptionRequest annotanions (max. number of subTopics/towns and so on).
     * <p>
     * JSON body examples of the request:
     * 
     * 1) all towns:
     * 
     * {
     *   "topic":"new_coffeeSite",
     *   "subTopics": [
     *                 "all_towns"
     *                ],
     *   "token":"long-token-string"
     *   }
     * 
     * 2) selected, up to 5, towns:
     * {
     *   "topic":"new_coffeeSite",
     *   "subTopics": [
     *                 "Brno",
     *                 "Tišnov",
     *                 "Nymburk"                                
     *                ],
     *   "token":"long-token-string"
     *  }
     * 
     * @param request
     * @return
     */
    @PostMapping("/notification/subscribe")
    public ResponseEntity<PushNotificationResponse> subscribeNotification(@RequestBody @Valid PushNotificationSubscriptionRequest request, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidParameterValueException("PushNotificationSubscriptionRequest", bindingResult.getFieldErrors());
        }
        try {
            notificationSubscriptionService.subscribeToTopic(request);
        } catch (InterruptedException ex) {
            log.error("Notification subscription failed. Exception: {}", ex.getMessage());
            Thread.currentThread().interrupt();
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Notification subscription failed."), HttpStatus.SERVICE_UNAVAILABLE);
        } catch ( ExecutionException e ) {
            log.error("Notification subscription failed. Exception: {}", e.getMessage());
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Notification subscription failed."), HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Subscription accepted."), HttpStatus.OK);
    } 
    
    /**
     * * Handles user's request to unsubscribe of sending push notification about new CoffeeSite in respective towns.
     * 
     * @param request
     * @return
     */
    @PostMapping("/notification/unsubscribe")
    public ResponseEntity<PushNotificationResponse> unSubscribeNotification(@RequestBody PushNotificationSubscriptionRequest request, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidParameterValueException("PushNotificationSubscriptionRequest", bindingResult.getFieldErrors());
        }
        try {
            notificationSubscriptionService.unsubscribeFromTopics(request);
        } catch (InterruptedException ex) {
            log.error("Notification unsubscription failed. Exception: {}", ex.getMessage());
            Thread.currentThread().interrupt();
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Notification unsubscription failed."), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (ExecutionException e) {
            log.error("Notification unsubscription failed. Exception: {}", e.getMessage());
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Notification unsubscription failed."), HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Subscription cancel accepted."), HttpStatus.OK);
    }

    /**
     * Handles user's request to unsubscribe of sending push notification about new CoffeeSite in ALL towns.
     * <p>
     * Example URL: https://localhost:8443/rest/firebase/notification/unsubscribeAll
     *
     * Example body:
     * {
     * 	"topic":"",
     * 	"subTopics": [],
     * 	"token":"c_w6FcZdT0WlDNKcGJ_BiM:APA91bGPPsD05OXPNF4G44_aUX96skkSBV9lK_4eaZtJcCn_4KOkbJsGCNvp_g1QQDWmt7vTMb73L8kZu97-RKRFrIqr-f6aSWdP2Q06WHa1tCOcVPIglu93YxH7RNS_BhXnmU2VCSbk"
     * }
     *
     *
     * @param request
     * @return
     */
    @PostMapping("/notification/unsubscribeAll")
    public ResponseEntity<PushNotificationResponse> unSubscribeAllNotifications(@RequestBody PushNotificationSubscriptionRequest request, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidParameterValueException("PushNotificationSubscriptionRequest", bindingResult.getFieldErrors());
        }
        try {
            notificationSubscriptionService.unsubscribeFromAllTopics(request.getToken());
        } catch (InterruptedException ex) {
            log.error("Notification unsubscription failed. Exception: {}", ex.getMessage());
            Thread.currentThread().interrupt();
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Notification unsubscription failed."), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (ExecutionException e) {
            log.error("Notification unsubscription failed. Exception: {}", e.getMessage());
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Notification unsubscription failed."), HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Subscription cancel accepted."), HttpStatus.OK);
    }
}
