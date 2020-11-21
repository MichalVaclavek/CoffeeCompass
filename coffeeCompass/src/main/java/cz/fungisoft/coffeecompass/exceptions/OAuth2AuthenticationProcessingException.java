package cz.fungisoft.coffeecompass.exceptions;

import org.springframework.security.core.AuthenticationException;

public class OAuth2AuthenticationProcessingException extends AuthenticationException
{
    /**
     * 
     */
    private static final long serialVersionUID = -9177256050734451139L;
    
    private String providerName;
    
    private String localizedMessageCode;
    

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getLocalizedMessageCode() {
        return localizedMessageCode;
    }

    public void setLocalizedMessageCode(String messageCode) {
        this.localizedMessageCode = messageCode;
    }

    public OAuth2AuthenticationProcessingException(String msg, Throwable t) {
        super(msg, t);
    }

    public OAuth2AuthenticationProcessingException(String msg) {
        super(msg);
    }
}
