package cz.fungisoft.coffeecompass.testutils;

import java.time.LocalDateTime;
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

        // ADMIN profile
        UserProfile userProfADMIN = new UserProfile();
        userProfADMIN.setType(customUser.roles()[0]);

        Set<UserProfile> userProfilesADMIN = new HashSet<>();
        userProfilesADMIN.add(userProfADMIN);
        
        User admin = new User();
        // ADMIN user profile returned, when requesting UserDetails
        admin.setUserName(customUser.userName());
        admin.setPassword(customUser.password());
        admin.setEmail(customUser.email());
        admin.setLongId(1L);
        admin.setCreatedOn(LocalDateTime.now());
        admin.setUserProfiles(userProfilesADMIN);
        
        UserDetails principal = UserPrincipal.create(admin);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(principal.getUsername(), principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(auth);
        testAuthentication = auth;
        return context;
    }
}
