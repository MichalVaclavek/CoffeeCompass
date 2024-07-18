package cz.fungisoft.users.security.oauth2;

import cz.fungisoft.coffeecompass.configuration.JwtAndOAuth2Properties;
import cz.fungisoft.coffeecompass.exceptions.OAuth2AuthenticationProcessingException;
import cz.fungisoft.coffeecompass.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static cz.fungisoft.coffeecompass.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

/**
 * In case of any error during OAuth2 authentication, Spring Security invokes the onAuthenticationFailure()<br>
 * method of the OAuth2AuthenticationFailureHandler, that was configured in SecurityConfig.<br>
 * It sends the user to the frontend client with an error message added to the query string.<br>

 * @author https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/
 * @author Michal Vaclavek
 */
@Component
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    
    private final JwtAndOAuth2Properties ouat2Properties;

    public OAuth2AuthenticationFailureHandler(HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository,
                                              JwtAndOAuth2Properties ouat2Properties) {
        super();
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
        this.ouat2Properties = ouat2Properties;
    }

    /**
     * Serves OAuth2 authentication failure case.
     * Mainly redirects to predefined URL with error message code included.
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
      
        log.error("Chyba: {}", exception.getMessage());
        
        String redirectErrorUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
              .map(Cookie::getValue)
              .orElse((ouat2Properties.getOauth2().getDefaultErrorLoginRedirectURI())); 
          
        
        if (exception instanceof OAuth2AuthenticationProcessingException oAuth2Exception) {
            redirectErrorUri = UriComponentsBuilder.fromUriString(redirectErrorUri)
                                                   .queryParam("oAuth2ErrorMessageCode", oAuth2Exception.getLocalizedMessageCode())
                                                   .queryParam("oAuth2ErrorMessageParameter", oAuth2Exception.getProviderName())
                                                   .build().toUriString();
        } else {
            redirectErrorUri = UriComponentsBuilder.fromUriString(redirectErrorUri)
                                                   .queryParam("oAuth2ErrorMessage", exception.getLocalizedMessage())
                                                   .build().toUriString();
        }

        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
        
        getRedirectStrategy().sendRedirect(request, response, redirectErrorUri);
  }
}
