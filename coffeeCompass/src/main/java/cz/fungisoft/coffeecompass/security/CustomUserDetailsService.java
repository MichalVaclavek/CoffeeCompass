package cz.fungisoft.coffeecompass.security;

import java.util.ArrayList;
import java.util.List;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

 
@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService
{
    static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
     
    private UserService userService;
    
    /**
     * Anotace @Lazy pridana proto, ze se udajne tvorila Circular dependency pri vytvareni UserService, CustomUserDetailsService
     * a SecurityConfiguration, kdyz vsechny tyto tridy pouzivali Constructor injection
     * 
     * @param userService
     */
    @Autowired
    public CustomUserDetailsService(/*@Qualifier("userService")*/ @Lazy UserService userService) {
        super();
        this.userService = userService;
    }

    // Je nutne zde uvadet @Transactional pokud nejsme v Repository vrstve? Pokud by bylo
    // vice operaci tak asi ano
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        
        User user = userService.findByUserName(userName);
        logger.info("User : {}", userName);
        if (user == null)
        {
            logger.info("User not found");
            throw new UsernameNotFoundException("Username not found: " + userName) ;
        }
        return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(), 
                 true, true, true, true, getGrantedAuthorities(user));
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
            
//              boolean enabled = true;
              boolean accountNonExpired = true;
              boolean credentialsNonExpired = true;
              boolean accountNonLocked = true;
              
              try
              {
                  User user = userService.findByEmail(email);
                  if (user == null) {
                      throw new UsernameNotFoundException("No user found with username: " + email);
                  }
                   
                  return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), 
                    user.getPassword().toLowerCase(), 
                    user.isRegisterEmailConfirmed(), 
                    accountNonExpired, 
                    credentialsNonExpired, 
                    accountNonLocked, 
                    getGrantedAuthorities(user));
              } catch (Exception e) {
                  throw new RuntimeException(e);
              }
    }
     
    public List<? extends GrantedAuthority> getGrantedAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
         
        for (UserProfile userProfile : user.getUserProfiles()) {
            logger.info("UserProfile : {}", userProfile);
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userProfile.getType()));
        }
        
        logger.info("authorities : {}", authorities);
        return authorities;
    }
    
}
     
