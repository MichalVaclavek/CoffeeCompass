package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.security.IAuthenticationFacade;
import cz.fungisoft.coffeecompass.service.UserSecurityService;

/**
 * Implements custome interface for operations related to security issues.
 * <p>
 * Creates new authentications objects in case new user is registered or current user is
 * updated.
 * <p>
 * Returns current logged-in user, can perform logout of the user.
 * 
 * @author Michal Vaclavek
 *
 */
@Service("userSecurityService")
public class UserSecurityServiceImpl implements UserSecurityService
{
    private IAuthenticationFacade authenticationFacade;
    
    private CustomUserDetailsService userDetailsService;
    
    public UserSecurityServiceImpl(IAuthenticationFacade authenticationFacade, CustomUserDetailsService userDetailsService) {
        super();
        this.authenticationFacade = authenticationFacade;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void authWithoutPassword(User user) {
        
        Authentication auth = authenticationFacade.getContext().getAuthentication();
        if (auth == null || auth.getName().equalsIgnoreCase("anonymousUser")) {    
            Collection<SimpleGrantedAuthority>  nowAuthorities = (Collection<SimpleGrantedAuthority>) userDetailsService.getGrantedAuthorities(user);
            UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(user.getUserName(), null, nowAuthorities);
            authenticationFacade.getContext().setAuthentication(newAuthentication);                 
        }
    }
    
    @Override
    public void authWithPassword(User user, String password) {
        
        Authentication auth = authenticationFacade.getContext().getAuthentication();
        if (auth == null || auth.getName().equalsIgnoreCase("anonymousUser")) {    
            Collection<SimpleGrantedAuthority>  nowAuthorities = (Collection<SimpleGrantedAuthority>) userDetailsService.getGrantedAuthorities(user);
            UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(user.getUserName(), password, nowAuthorities);
            authenticationFacade.getContext().setAuthentication(newAuthentication);                 
        }
    }

    
    /**
     * Returns current logged-in username or<br>
     * empty String, if there is no authenticated/logged-in user.
     */
    @Override
    public String getCurrentLoggedInUserName() {
        Authentication authentication = authenticationFacade.getAuthentication();
        return  (authentication != null) ? authentication.getName()
                                         : "";
    }
    
    /**
     * Updates Spring authentication/security context/object in case a user's profile has been updated. 
     */
    @Override
    public void updateCurrentAuthentication(User entity, String newUserName, String newPasswd) {
        Authentication authentication = authenticationFacade.getAuthentication();
        
        if (authentication != null) {
            Collection<SimpleGrantedAuthority>  nowAuthorities = (Collection<SimpleGrantedAuthority>) userDetailsService.getGrantedAuthorities(entity);
            UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(newUserName, newPasswd, nowAuthorities);
            authenticationFacade.getContext().setAuthentication(newAuthentication);                 
        }
    }

    @Override
    public void logout() {
        authenticationFacade.getContext().setAuthentication(null);
    }

    @Override
    public void authWithUserNameAndRole(String userName, String role) {
        authWithUserNameAndPasswordAndRole(userName, null, role);
        
    }

    @Override
    public void authWithUserNameAndPasswordAndRole(String userName, String passwd, String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken(userName, passwd, Arrays.asList(new SimpleGrantedAuthority(role)));
        authenticationFacade.getContext().setAuthentication(auth);
    }

}
