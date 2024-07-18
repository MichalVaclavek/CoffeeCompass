package cz.fungisoft.users.listeners;

import java.util.Locale;

import cz.fungisoft.users.entity.User;
import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private static final long serialVersionUID = -4098439041309044675L;
    
    private Locale locale;
    private User user;
 
    public OnRegistrationCompleteEvent(User user, Locale locale) {
        super(user);
         
        this.user = user;
        this.locale = locale;
    }
}
