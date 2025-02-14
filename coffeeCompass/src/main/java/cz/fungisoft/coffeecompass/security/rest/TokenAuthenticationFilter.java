package cz.fungisoft.coffeecompass.security.rest;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import cz.fungisoft.coffeecompass.service.user.UserSecurityService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

/**
 * The TokenAuthenticationFilter is responsible of extracting the authentication token from the request headers.<br>
 * It takes the Authorization header value and attempts to extract the token from it.<br>
 * Authentication is then delegated to the AuthenticationManager. The filter is only enabled for a given set of urls.
 * <p>
 * see: <a href="https://octoperf.com/blog/2018/03/08/securing-rest-api-spring-security/">...</a>
 *
 * @author Michal Vaclavek
 *
 */
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String BEARER = "Bearer";
    
    private final UserSecurityService userSecurityService;
    

    public TokenAuthenticationFilter(final RequestMatcher requiresAuth, UserSecurityService userSecurityService) {
       super(requiresAuth);
       this.userSecurityService = userSecurityService;
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response) {
        
       String param = Optional.ofNullable(request.getHeader(AUTHORIZATION))
                              .orElse(request.getParameter("t"));

       String token = Optional.ofNullable(param)
                              .map(value -> value.startsWith(BEARER) ? value.replace(BEARER, "") : value)
                              .map(String::trim)
                              .orElseThrow(() -> new BadCredentialsException("Missing Authentication Token"));

       Authentication auth;
       
       try {
           auth = userSecurityService.authWithToken(token);
       } catch (Exception ex) {
           logger.error("Could not set user authentication in security context. " + ex.getMessage(), ex);
           throw ex;
       }
       return auth;
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final FilterChain chain,
                                            final Authentication authResult) throws IOException, ServletException {
        
       super.successfulAuthentication(request, response, chain, authResult);
       chain.doFilter(request, response);
    }
}
