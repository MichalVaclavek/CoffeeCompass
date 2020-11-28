package cz.fungisoft.coffeecompass.testutils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.security.UserPrincipal;

/**
 * Creates test Authentication object with usage of {@link WithMockCustomAdminUser}
 * 
 * @author Michal Vaclavek
 *
 */
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomAdminUser> {
    
    // ADMIN profile
    private  UserProfile userProfADMIN;
    private  Set<UserProfile> userProfilesADMIN;
    
    /**
     * Can be returned as authentication object for Mocked UserSecurityService instancies
     * and respective methods, like userSecurityService.authWithToken();
     */
    private static Authentication testAuthentication;
    
    public static Authentication getTestAuthentication() {
        return testAuthentication;
    }


    /**
     * Creates authentication object in test Security Spring context.
     * Functional interface {@link WithMockCustomAdminUser} default values are
     * used as input.
     */
    @Override
    public SecurityContext createSecurityContext(WithMockCustomAdminUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        userProfADMIN = new UserProfile();
        userProfADMIN.setType(customUser.roles()[0]);
        
        userProfilesADMIN = new HashSet<UserProfile>();
        userProfilesADMIN.add(userProfADMIN);
        
        User admin = new User();
        // ADMIN user profile returned, when requesting UserDetails
        admin.setUserName(customUser.userName());
        admin.setPassword(customUser.password());
        admin.setEmail(customUser.email());
        admin.setId(1L);
        admin.setCreatedOn(new Timestamp(new Date().getTime()));
        admin.setUserProfiles(userProfilesADMIN);
        
        UserDetails principal = UserPrincipal.create(admin);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(principal.getUsername(), principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(auth);
        testAuthentication = auth;
        return context;
    }
}
