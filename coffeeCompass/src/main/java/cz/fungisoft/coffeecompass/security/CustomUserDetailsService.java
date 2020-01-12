package cz.fungisoft.coffeecompass.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.service.UserService;

/**
 * Custom implementation of Spring's UserDetailsService. Provides {@code UserDetails}
 * for Spring security framwork.<br>
 * Allows login using both username and email.
 * 
 * @author Michal Vaclavek
 *
 */
@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService
{
    static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
     
    private UserService userService;
    
    /**
     * Anotace @Lazy pridana proto, ze se udajne tvorila Circular dependency pri vytvareni UserService, CustomUserDetailsService
     * a SecurityConfiguration, kdyz vsechny tyto tridy pouzivali Constructor injection.
     * 
     * @param userService
     */
    public CustomUserDetailsService(@Lazy UserService userService) {
        super();
        this.userService = userService;
    }

    /**
     * @param userNameEmail - user name or e-mail of the User who's profile is to be provided to Spring security framework.
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String userNameEmail) throws UsernameNotFoundException {
        
        Optional<User> user = userService.findByUserName(userNameEmail);
        if (!user.isPresent()) { // not found by user name
            user = userService.findByEmail(userNameEmail); // try to find by email
            if (!user.isPresent()) {
                logger.info("Username/email {} not found.", userNameEmail);
                throw new UsernameNotFoundException("Username/email not found: " + userNameEmail) ;
            }
        } 
        
        return UserPrincipal.create(user.get());
    }
    
    /**
     * Used in case we use e-mail as userid
     * 
     * @param email
     * @return
     * @throws UsernameNotFoundException
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
            
        try {
            Optional<User> user = userService.findByEmail(email);
            if (!user.isPresent()) {
                logger.info("User with e-mail {} not found.", email);
                throw new UsernameNotFoundException("No user found with e-mail: " + email);
            }
               
            return UserPrincipal.create(user.get());
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Transactional
    public UserDetails loadUserById(Long id) {
        
        Optional<User> user = userService.findById(id);
        if (!user.isPresent()) {
            logger.info("User with id {} not found.", id);
            throw new UsernameNotFoundException("There is no user with this id: " + id);
        }

        return UserPrincipal.create(user.get());
    }
    
    /**
     * Provides list of User's {@code GrantedAuthority} as requested by Spring Security.
     * 
     * @param user - a User who's {@code GrantedAuthority} are requested.
     * @return
     */
    public List<? extends GrantedAuthority> getGrantedAuthorities(User user) {
        
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
         
        for (UserProfile userProfile : user.getUserProfiles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userProfile.getType()));
        }
        
        logger.info("authorities : {}", authorities);
        return authorities;
    }
    
}
     
