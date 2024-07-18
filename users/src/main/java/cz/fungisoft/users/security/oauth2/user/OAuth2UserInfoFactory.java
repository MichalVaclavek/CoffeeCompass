package cz.fungisoft.users.security.oauth2.user;


import java.util.Map;

/**
 * Factory class to get concrete implementation of the OAuth2UserInfo class according provider.
 * 
 * @author https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/
 *
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        
        if (registrationId.equalsIgnoreCase(AuthProviders.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProviders.FACEBOOK.toString())) {
            return new FacebookOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
