package cz.fungisoft.coffeecompass.serviceimpl.user;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.security.IAuthenticationFacade;
import cz.fungisoft.coffeecompass.service.user.CustomRESTUserAuthenticationService;
import cz.fungisoft.coffeecompass.service.user.UserSecurityService;

/**
 * Implements custom interface for operations specific to security issues.
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
@Slf4j
public class UserSecurityServiceImpl implements UserSecurityService {

    private final IAuthenticationFacade authenticationFacade;
    
    private final CustomUserDetailsService userDetailsService;
    
    private final CustomRESTUserAuthenticationService restUserDetailsService;

    private final SecurityContextRepository securityContextRepository;
    
    public UserSecurityServiceImpl(IAuthenticationFacade authenticationFacade,
                                   CustomUserDetailsService userDetailsService,
                                   @Lazy
                                   @Qualifier("jwtTokenUserAuthenticationService")
                                   CustomRESTUserAuthenticationService restUserDetailsService,
                                   @Lazy
                                   SecurityContextRepository securityContextRepository
                                   ) {
        super();
        this.authenticationFacade = authenticationFacade;
        this.userDetailsService = userDetailsService;
        this.restUserDetailsService = restUserDetailsService;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public void authWithoutPassword(User user) {
        Authentication auth = authenticationFacade.getContext().getAuthentication();
        if (auth == null || auth.getName().equalsIgnoreCase("anonymousUser")) {    
            Collection<SimpleGrantedAuthority> nowAuthorities = (Collection<SimpleGrantedAuthority>) userDetailsService.getGrantedAuthorities(user);
            UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(user.getUserName(), null, nowAuthorities);
            authenticationFacade.getContext().setAuthentication(newAuthentication);                 
        }
    }
    
    @Override
    public void authWithPassword(User user, String password, HttpServletRequest request,
                                 HttpServletResponse response) {
        Authentication auth = authenticationFacade.getContext().getAuthentication();
        if (auth == null || auth.getName().equalsIgnoreCase("anonymousUser")) {    
            Collection<SimpleGrantedAuthority> nowAuthorities = (Collection<SimpleGrantedAuthority>) userDetailsService.getGrantedAuthorities(user);
            UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(user.getUserName(), password, nowAuthorities);
//            authenticationFacade.getContext().setAuthentication(newAuthentication);

            SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
                    .getContextHolderStrategy();

            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(newAuthentication);
            securityContextHolderStrategy.setContext(context);

            securityContextRepository.saveContext(context, request, response);
        }
    }
    
    /**
     * Used for authentication using REST token, within Authentication filter, where UserDetails
     * are fetched from jwt token send from client.
     */
    @Override
    public Authentication authWithToken(String token) {
        Authentication auth;
        
        Optional<UserDetails> userDetails = restUserDetailsService.findByToken(token);
        if (userDetails.isPresent()) {
            auth = new UsernamePasswordAuthenticationToken(userDetails.get().getUsername(), userDetails.get().getPassword(), userDetails.get().getAuthorities());
        } else {
            throw new BadCredentialsException("Bad authorization. User not found or expired token.");
        }
        
        authenticationFacade.getContext().setAuthentication(auth);
        return auth;
    }

    
    /**
     * Returns current logged-in username or<br>
     * empty String, if there is no authenticated/logged-in user.
     */
    @Override
    public String getCurrentLoggedInUserName() {
        Authentication authentication = authenticationFacade.getAuthentication();
        return  (authentication != null) ? authentication.getName() : "";
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
        Authentication auth = new UsernamePasswordAuthenticationToken(userName, passwd, Collections.singletonList(new SimpleGrantedAuthority(role)));
        authenticationFacade.getContext().setAuthentication(auth);
    }

    @Override
    public void logout(UserDetails user) {
        if (user.equals(authenticationFacade.getAuthentication().getDetails())) {
            logout();
        }
    }

    @Override
    public void logout(String userName) {
        if (userName.equalsIgnoreCase((authenticationFacade.getAuthentication().getName()))) {
            logout();
        }
    }
}