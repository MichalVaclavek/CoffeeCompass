package cz.fungisoft.coffeecompass.serviceimpl.notifications;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import cz.fungisoft.coffeecompass.service.notifications.FirebaseInitializer;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FirebaseInitializerImpl implements FirebaseInitializer {

    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;
    
    
    @PostConstruct // called on application start up
    @Override
    public void initialize() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())).build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase has been initialized.");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
