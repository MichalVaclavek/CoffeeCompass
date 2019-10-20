package cz.fungisoft.coffeecompass.service;

import cz.fungisoft.coffeecompass.entity.User;

/**
 * An interface to gather functions related to user's authentication like<br>
 * log-in, log-out, updates of the Spring user's Authentication data object etc.
 * 
 * @author Michal Vaclavek
 *
 */
public interface UserSecurityService
{
    public void authWithoutPassword(User user);

    public void authWithPassword(User user, String password);
    
    public void authWithUserNameAndRole(String userName, String role);
    
    public void authWithUserNameAndPasswordAndRole(String userName, String passwd, String role);
    
    public String getCurrentLoggedInUserName();
    
    public void logout();

    public void updateCurrentAuthentication(User entity, String newUserName, String newPasswd);
}
