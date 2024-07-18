package cz.fungisoft.users.listeners;


@Component
public class UserRegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private TokenCreateAndSendEmailService verificationTokenSendEmailService;
    
    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }
 
    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        //verificationTokenSendEmailService.setUserVerificationData(event.getUser(), event.getLocale());
        verificationTokenSendEmailService.createAndSendVerificationTokenEmail(event.getUser(), event.getLocale());
    }
}
