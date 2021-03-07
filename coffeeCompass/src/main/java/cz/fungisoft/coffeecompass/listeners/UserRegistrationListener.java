package cz.fungisoft.coffeecompass.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import cz.fungisoft.coffeecompass.service.tokens.TokenCreateAndSendEmailService;

@Component
public class UserRegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent>
{
    @Autowired
    private TokenCreateAndSendEmailService verificationTokenSendEmailService;
    
    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }
 
    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        verificationTokenSendEmailService.setUserVerificationData(event.getUser(), event.getLocale());
        verificationTokenSendEmailService.createAndSendVerificationTokenEmail();
    }
}
