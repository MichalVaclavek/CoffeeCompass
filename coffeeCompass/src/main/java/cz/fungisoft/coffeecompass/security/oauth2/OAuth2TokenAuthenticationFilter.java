package cz.fungisoft.coffeecompass.security.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class is used to read JWT authentication token from the request, verify it,<br>
 * and set Spring Security’s SecurityContext if the token is valid.<br>
 * 
 * ???? Usage ???? needed for Thymeleaf? or only for REST?
 * 
 * @author https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/
 *
 */
public class OAuth2TokenAuthenticationFilter extends OncePerRequestFilter
{
    private static final Logger logger = LoggerFactory.getLogger(OAuth2TokenAuthenticationFilter.class);
    
    private OAuth2TokenProvider oAuth2TokenProvider;

    private CustomUserDetailsService customUserDetailsService;

    
    @Autowired
    public OAuth2TokenAuthenticationFilter(OAuth2TokenProvider oAuth2TokenProvider, CustomUserDetailsService customUserDetailsService) {
        super();
        this.oAuth2TokenProvider = oAuth2TokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && oAuth2TokenProvider.validateToken(jwt)) {
                Long userId = oAuth2TokenProvider.getUserIdFromToken(jwt);

                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

}