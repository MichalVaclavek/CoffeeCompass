package cz.fungisoft.coffeecompass.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import cz.fungisoft.coffeecompass.service.VerificationTokenCreateAndSendEmailService;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent>
{
    @Autowired
    private VerificationTokenCreateAndSendEmailService verificationTokenSendEmailService;
    
    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }
 
    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        verificationTokenSendEmailService.setVerificationData(event.getUser(), event.getAppUrl(), event.getLocale());
        verificationTokenSendEmailService.createAndSendVerificationTokenEmail();
    }
    
}
