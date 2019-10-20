package cz.fungisoft.coffeecompass.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * For building JWT token in case of REST api for authenticated user.
 * Not needed for Thymeleaf?
 * 
 * @author Michal
 *
 */
//@PropertySource("classpath:application-${spring.profiles.active}.properties")
@Profile("default")
@Configuration
@ConfigurationProperties(prefix = "app")
public class OAuth2Properties
{
    private final Auth auth = new Auth();
    private final OAuth2 oauth2 = new OAuth2();

    public static class Auth {
        
        private String tokenSecret;
        
        private long tokenExpirationMsec;

        public String getTokenSecret() {
            return tokenSecret;
        }

        public void setTokenSecret(String tokenSecret) {
            this.tokenSecret = tokenSecret;
        }

        public long getTokenExpirationMsec() {
            return tokenExpirationMsec;
        }

        public void setTokenExpirationMsec(long tokenExpirationMsec) {
            this.tokenExpirationMsec = tokenExpirationMsec;
        }
    }

    public static final class OAuth2 {
        
        private String defaultSuccessLoginRedirectURI;
        
        public void setDefaultSuccessLoginRedirectURI(String defaultSuccessLoginRedirectURI) {
            this.defaultSuccessLoginRedirectURI = defaultSuccessLoginRedirectURI;
        }

        public String getDefaultSuccessLoginRedirectURI() {
            return defaultSuccessLoginRedirectURI;
        }
        
        private String authorizationRequestBaseUri;
        
        public void setAuthorizationRequestBaseUri(String authorizationRequestBaseUri) {
            this.authorizationRequestBaseUri = authorizationRequestBaseUri;
        }

        public String getAuthorizationRequestBaseUri() {
            return authorizationRequestBaseUri;
        }

        private List<String> authorizedRedirectUris = new ArrayList<>();

        public List<String> getAuthorizedRedirectUris() {
            return authorizedRedirectUris;
        }

        public OAuth2 authorizedRedirectUris(List<String> authorizedRedirectUris) {
            this.authorizedRedirectUris = authorizedRedirectUris;
            return this;
        }
        
        private String defaultErrorLoginRedirectURI;

        public void setDefaultErrorLoginRedirectURI(String defaultErrorLoginRedirectURI) {
            this.defaultErrorLoginRedirectURI = defaultErrorLoginRedirectURI;
        }
        
        public String getDefaultErrorLoginRedirectURI() {
            return defaultErrorLoginRedirectURI;
        }
    }

    public Auth getAuth() {
        return auth;
    }

    public OAuth2 getOauth2() {
        return oauth2;
    }
    
}
