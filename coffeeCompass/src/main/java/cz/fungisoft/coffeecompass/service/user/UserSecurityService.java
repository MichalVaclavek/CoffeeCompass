package cz.fungisoft.coffeecompass.service.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import cz.fungisoft.coffeecompass.entity.User;

/**
 * An interface to gather functions related to user's authentication like<br>
 * log-in, log-out, updates of the Spring user's Authentication data object etc.
 * 
 * @author Michal Vaclavek
 *
 */
public interface UserSecurityService {

    void authWithoutPassword(User user);
    
    /**
     * Authenticates user by its username and password, which is obtained from
     * token.
     * 
     * @param token assigned to user on login or register request.
     * @return
     */
    Authentication authWithToken(String token);

    void authWithPassword(User user, String password);
    
    void authWithUserNameAndRole(String userName, String role);
    
    void authWithUserNameAndPasswordAndRole(String userName, String passwd, String role);
    
    String getCurrentLoggedInUserName();
    
    void logout();
    
    void logout(UserDetails user);
    
    void logout(String userName);

    void updateCurrentAuthentication(User entity, String newUserName, String newPasswd);
}
