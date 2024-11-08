package cz.fungisoft.coffeecompass.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;

/**
 * A class providing UserDetails objects for Spring security framework by mapping from User.
 * <p>
 * Original description:<br>
 * The UserPrincipal class represents an authenticated Spring Security principal. It contains the details of the authenticated user.
 *  
 * @author https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/
 * @author Michal Václavek
 */
public class UserPrincipal implements OAuth2User, UserDetails {

    private static final long serialVersionUID = 2750856799274399097L;
    
    private String id;
    private String email;
    private String password;
    private String userName;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;
    
    private boolean accountNonLocked;
    
    private boolean enabled;

    /**
     * Base constructor
     * 
     * @param id
     * @param userName
     * @param email
     * @param password
     * @param authorities
     * @param accountNonLocked
     * @param enabled
     */
    public UserPrincipal(String id, String userName, String email, String password,
                         Collection<? extends GrantedAuthority> authorities, boolean accountNonLocked, boolean enabled) {
        this.id = id;
        this.email = email;
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
        this.accountNonLocked = accountNonLocked;
        this.enabled = enabled;
    }


    public static UserPrincipal create(User user) {

        return new UserPrincipal(
                user.getLongId().toString(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                getGrantedAuthorities(user),
                !user.isBanned(),
                true
        );
    }

   
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }
    
    private static List<? extends GrantedAuthority> getGrantedAuthorities(User user) {
        
        List<GrantedAuthority> authorities = new ArrayList<>();
         
        for (UserProfile userProfile : user.getUserProfiles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userProfile.getType()));
        }
        
        return authorities;
    }
    

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return id;
    }
}
