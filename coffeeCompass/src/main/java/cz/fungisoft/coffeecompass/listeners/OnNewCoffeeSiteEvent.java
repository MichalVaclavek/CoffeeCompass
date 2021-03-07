package cz.fungisoft.coffeecompass.listeners;

import org.springframework.context.ApplicationEvent;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import lombok.Getter;
import lombok.Setter;

/**
 * Event to be fired when new CoffeeSite was added to DB.<br>
 * Used to detect such event and send notifications to those
 * subscribed for them in Firebase.
 * 
 * @author Michal V.
 *
 */
@Setter
@Getter
public class OnNewCoffeeSiteEvent extends ApplicationEvent
{
    
    private static final long serialVersionUID = 8367834543095239220L;
    
    private CoffeeSite coffeeSite;
 
    public OnNewCoffeeSiteEvent(CoffeeSite coffeeSite) {
        super(coffeeSite);
         
        this.coffeeSite = coffeeSite;
    }
}
