package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.concurrent.ExecutionException;

import javax.validation.Valid;

import cz.fungisoft.coffeecompass.controller.rest.PushNotificationController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationRequest;
import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationResponse;
import cz.fungisoft.coffeecompass.controller.models.rest.PushNotificationSubscriptionRequest;
import cz.fungisoft.coffeecompass.exceptions.rest.InvalidParameterValueException;
import cz.fungisoft.coffeecompass.service.notifications.NotificationSubscriptionService;
import cz.fungisoft.coffeecompass.service.notifications.PushNotificationService;

/**
 * REST Controller to handle requests related to Push notifications of Firebase.
 *  
 * @author Michal Vaclavek
 *
 */
@Tag(name = "PushNotificationsSecured", description = "New coffee site's Push notifications subscription")
@RestController
@RequestMapping("/rest/secured/firebase")
public class PushNotificationSecuredController {
    
    private static final Logger log = LoggerFactory.getLogger(PushNotificationSecuredController.class);
    
    private final PushNotificationService pushNotificationService;
    
    private final NotificationSubscriptionService notificationSubscriptionService;
    
    public PushNotificationSecuredController(PushNotificationService pushNotificationService, NotificationSubscriptionService notificationSubscriptionService) {
        this.pushNotificationService = pushNotificationService;
        this.notificationSubscriptionService = notificationSubscriptionService;
    }
    
    /**
     * Handles reques to immediate send push message to given Topic without data body.
     * Used for testing purposes only.
     * 
     * @param request
     * @return
     */
    @PostMapping("/notification/topic")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PushNotificationResponse> sendNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationWithoutData(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }
    
    /**
     * Handles request to immediate send push message to given Token.
     * Used for testing purposes only.
     * 
     * @param request
     * @return
     */
    @PostMapping("/notification/token")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PushNotificationResponse> sendTokenNotification(@RequestBody PushNotificationRequest request) {
        pushNotificationService.sendPushNotificationToToken(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
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
     * 2) selected, upt to 5, towns:
     * {
     *   "topic":"new_coffeeSite",
     *   "subTopics": [
     *                 "Brno",
     *                 "Tišnov",
     *                 "Nymburk"                                
     *                ],
     *   "token":"long-token-string"
     *
     *   }
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
     * Handles user's request to unsubscribe of sending push notification about new CoffeeSite in respective towns.
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
     * Handles user's request to unsubscribe of sending push notification about new CoffeeSite in all towns.
     * There is same method body, same @RequestBody as in the public {@link PushNotificationController#unSubscribeAllNotifications(PushNotificationSubscriptionRequest, BindingResult)}
     * version, but with Authentication header included in the POST request.
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
