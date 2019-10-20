package cz.fungisoft.coffeecompass.security.oauth2;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.configuration.OAuth2Properties;
import cz.fungisoft.coffeecompass.security.UserPrincipal;

import java.util.Date;

/**
 * This class contains code to generate and verify Json Web Tokens.
 * <p>
 * Probably Not needed for Thymeleaf application, but it is used here to<br>
 * secure redirect url after successfull social login. The token is added<br>
 * to the redirection uri to prove that successfuly logedin user is accessing<br>
 * redirected url (which maybe protected resource for logedin users only).
 * 
 * @author https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/
 * 
 */
@Service
public class OAuth2TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2TokenProvider.class);

    private OAuth2Properties oauth2Properties;

    public OAuth2TokenProvider(OAuth2Properties appProperties) {
        this.oauth2Properties = appProperties;
    }

    /**
     * Creates token from userID, current date and token expiry date.
     * 
     * @param authentication - Spring security object holding current user which is being authenticating?
     * @return created token.
     */
    public String createToken(Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + oauth2Properties.getAuth().getTokenExpirationMsec());

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, oauth2Properties.getAuth().getTokenSecret())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(oauth2Properties.getAuth().getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(oauth2Properties.getAuth().getTokenSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
    
}
