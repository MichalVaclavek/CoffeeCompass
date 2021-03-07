package cz.fungisoft.coffeecompass.controller.models.rest;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class to hold subscription request for receiving push notifications to given Topics and subTopic.
 * (In our app., Topic is usually something like: new_coffeeSites, deletedSites, ..
 * and subTopic is the name of town)
 * 
 * @author Michal V.
 *
 */
@Setter
@Getter
@NoArgsConstructor
public class PushNotificationSubscriptionRequest {
    
    @NotBlank(message="Cannot be empty")
    private String topic; // for example: 'new_coffeeSites'
    
    @Size(min=1, max=5, message="Max. 5 subTopics/towns, minimum 1 subTopic/town")
    //@Size(min=1, max=5, message="{Size.pushNotificationSubscriptionRequest.subTopics}")
    private List<String> subTopics; // for example: ['Brno','Praha','Tišnov'] or ['all_towns']
    
    @NotBlank(message="Cannot be empty")
    private String token;  // device token assigned by Firebase to the client
}
