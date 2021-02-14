/**
 * 
 */
package cz.fungisoft.coffeecompass.service;

/**
 * Interface to define service methods to configure/initialize of the Firebase framwork connection
 * 
 * Taken from: https://medium.com/@singh.pankajmca/fcm-integration-with-spring-boot-to-send-push-notification-from-server-side-1091cfd2cacf 
 * 
 * @author Michal
 *
 */
public interface FirebaseInitializer {

    void initialize();
    
}
