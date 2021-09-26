/**
 * 
 */
package cz.fungisoft.coffeecompass.service.notifications;

/**
 * Interface to define service methods to configure/initialize of the Firebase fraemwork connection
 * 
 * Taken from: https://medium.com/@singh.pankajmca/fcm-integration-with-spring-boot-to-send-push-notification-from-server-side-1091cfd2cacf 
 * 
 * @author Michal V.
 *
 */
public interface FirebaseInitializer {

    void initialize();
}
