package cz.fungisoft.coffeecompass.security.oauth2.user;

import java.util.Map;

/**
 * Every OAuth2 provider returns a different JSON response when we fetch the authenticated user’s details.<br>
 * Spring security parses the response in the form of a generic map of key-value pairs.<br>
   This class is used as a base class to get the required details of the user from the generic map of key-value pairs.<br>
   <br>
   There has to be extension of the class implementing detailes for every social login provider used in app.
   
 * @author https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/
 * @author Michal Václavek
 *
 */
public abstract class OAuth2UserInfo
{
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();

    public abstract String getName();
    
    public abstract String getFirstName();
    
    public abstract String getLastName();

    public abstract String getEmail();
    
    public abstract boolean isEmailConfirmed();

    public abstract String getImageUrl();
    
}
