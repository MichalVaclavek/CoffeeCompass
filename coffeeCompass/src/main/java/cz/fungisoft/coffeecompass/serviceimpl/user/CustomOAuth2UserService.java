package cz.fungisoft.coffeecompass.serviceimpl.user;

import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.controller.models.AuthProviders;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exceptions.OAuth2AuthenticationProcessingException;
import cz.fungisoft.coffeecompass.security.UserPrincipal;
import cz.fungisoft.coffeecompass.security.oauth2.user.OAuth2UserInfo;
import cz.fungisoft.coffeecompass.security.oauth2.user.OAuth2UserInfoFactory;
import cz.fungisoft.coffeecompass.service.user.UserService;

/**
 * The CustomOAuth2UserService extends Spring Security’s DefaultOAuth2UserService and implements its loadUser() method.<br>
 * This method is called after an access token is obtained from the OAuth2 provider.<br>
 * In this method, we first fetch the user’s details from the OAuth2 provider. If a user with the same email<br>
 * already exists in our database then we update his details, otherwise, we register a new user.<br>
 * 
 * @author https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/
 * @author Michal Vaclavek
 *
 */
@Service
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    
    public CustomOAuth2UserService(@Lazy UserService userService) {
        super();
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }
    
    /**
     * Creates new user or updates current user, which is successfully logged-in within 'social' OAuth2 provider.
     * 
     * @param oAuth2UserRequest
     * @param oAuth2User
     * @return
     */
    public OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if (oAuth2UserInfo.getEmail().isEmpty()) {
            OAuth2AuthenticationProcessingException ex = new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider " + oAuth2UserRequest.getClientRegistration().getRegistrationId());
            ex.setLocalizedMessageCode("oauth2.emailnotreturned.message");
            ex.setProviderName(oAuth2UserRequest.getClientRegistration().getRegistrationId());
            throw ex;
        }

        Optional<User> userOptional = this.userService.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getAuthProvider().equals(AuthProviders.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                OAuth2AuthenticationProcessingException ex = new OAuth2AuthenticationProcessingException("OAuth2 authorization error with provider: " + oAuth2UserRequest.getClientRegistration().getRegistrationId());
                ex.setLocalizedMessageCode("oauth2.signup.anotherprovider.message");
                ex.setProviderName(user.getAuthProvider().toString());
                throw ex;
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return (user != null) ? UserPrincipal.create(user, oAuth2User.getAttributes()) : oAuth2User;
    }

    public User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        return userService.saveOAuth2User(oAuth2UserRequest.getClientRegistration(), oAuth2UserInfo);
    }

    public User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        return userService.updateOAuth2User(existingUser, oAuth2UserInfo);
    }
}
