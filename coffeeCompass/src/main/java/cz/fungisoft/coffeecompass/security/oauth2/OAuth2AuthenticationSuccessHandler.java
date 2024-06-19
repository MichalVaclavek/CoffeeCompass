package cz.fungisoft.coffeecompass.security.oauth2;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.configuration.JwtAndOAuth2Properties;
import cz.fungisoft.coffeecompass.exceptions.BadAuthorizationRequestException;
import cz.fungisoft.coffeecompass.security.JwtTokenProviderService;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;


/**
 * Probably Not needed for Thymeleaf application, but it is used here to<br>
 * secure redirect URL after successfull social login.<br>
 * The token is added to the redirection URL to prove, that successfuly<br>
 * logedin user is accessing redirection URL (which maybe protected resource for logedin users only).<br>
 * The token is then verified in respective Controller.
 * <p>
 * Original description:<br>
 * On successful authentication, Spring security invokes the onAuthenticationSuccess() method of<br>
 * the OAuth2AuthenticationSuccessHandler configured in SecurityConfig.<br>
 * In this method, we perform some validations, create a JWT authentication token, and redirect the user<br>
 * to the redirect_uri specified by the client with the JWT token added in the query string.<br>
 * 
 * @author https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/
 * @author Michal Václavek
 */
@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProviderService jwtTokenProviderService;

    private final JwtAndOAuth2Properties ouat2Properties;

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;


    public OAuth2AuthenticationSuccessHandler(JwtTokenProviderService jwtTokenProviderService,
                                              JwtAndOAuth2Properties appProperties,
                                              HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
        this.jwtTokenProviderService = jwtTokenProviderService;
        this.ouat2Properties = appProperties;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 
     * @param request
     * @param response
     * @param authentication
     * @return
     */
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        
        Optional<String> redirectUri = Optional.of(ouat2Properties.getOauth2().getDefaultSuccessLoginRedirectURI());
        
        if (!isAuthorizedRedirectUri(redirectUri.get())) {
            BadAuthorizationRequestException ex = new BadAuthorizationRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
            ex.setLocalizedMessageCode("oauth2.unauthorizedRedirectURI.error.message");
            throw ex;
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        String token = jwtTokenProviderService.createToken(authentication);

        return UriComponentsBuilder.fromUriString(targetUrl)
                                   .queryParam("token", token)
                                   .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        
        URI clientRedirectUri = URI.create(uri);

        return ouat2Properties.getOauth2()
                              .getAuthorizedRedirectUris()
                              .stream()
                              .anyMatch(authorizedRedirectUri -> {
                                   // Only validate host and port. Let the clients use different paths if they want to
                                   URI authorizedURI = URI.create(authorizedRedirectUri);
                                   return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                                          && authorizedURI.getPort() == clientRedirectUri.getPort();
                              });
    }
    
}
