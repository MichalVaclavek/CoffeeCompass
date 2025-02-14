package cz.fungisoft.coffeecompass.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import cz.fungisoft.coffeecompass.configuration.JwtAndOAuth2Properties;
import cz.fungisoft.coffeecompass.service.tokens.TokenService;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;


/**
 * This class contains code to generate and verify Json Web Tokens.
 * <p>
 * Probably Not needed for Thymeleaf application, but it is used here to<br>
 * secure redirect url after successfull social login. The token is added<br>
 * to the redirection uri to prove that successfuly logedin user is accessing<br>
 * redirected url (which maybe protected resource for logedin users only).
 * Upraveno 15.2. 2025 pro novou verzi knihovny Jjwt, podle <a href="https://www.appsdeveloperblog.com/add-and-validate-custom-claims-in-jwt/">...</a>
 *
 * @author <a href="https://www.callicoder.com/spring-boot-security-oauth2-social-login-part-2/">...</a>
 */
@Service("jwtTokenProviderService")
public class JwtTokenProviderService implements TokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderService.class);

    private static final String DOT = ".";

    private final String issuer;
    private final int expirationSec;
    private final SecretKey secretKey;

    private final JwtAndOAuth2Properties jwtPoperties;

    public JwtTokenProviderService(JwtAndOAuth2Properties appProperties) {
        this.jwtPoperties = appProperties;

        this.issuer = requireNonNull(jwtPoperties.getJwtAuth().getIssuer());
        this.expirationSec = jwtPoperties.getJwtAuth().getTokenExpirationSec();
        byte[] keyBytes = Decoders.BASE64.decode(jwtPoperties.getJwtAuth().getTokenSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Creates token from userID, current date and token expiry date.
     *
     * @param authentication - Spring security object holding current user which is being authenticating?
     * @return created token.
     */
    public String createToken(Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(jwtPoperties.getJwtAuth().getTokenExpirationSec()); // expires in 1 hour
        Date expiryDate = Date.from(expiration);

        return Jwts.builder()
                .subject(userPrincipal.getId())
                .issuedAt(Date.from(now))
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {

        try {
            Jwts
              .parser()
              .verifyWith(secretKey)
              .build()
              .parseSignedClaims(authToken);
            return true;
        } catch (InvalidClaimException ex) {
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

    /**
     * REST mobile app.
     **/
    @Override
    public String permanent(final Map<String, String> attributes) {
        return newToken(attributes, 0);
    }

    @Override
    public String expiring(final Map<String, String> attributes) {
        return newToken(attributes, expirationSec);
    }

    private String newToken(final Map<String, String> attributes, final int expiresInSec) {
        Instant now = Instant.now();
        Date expiryDate = Date.from(now.plusSeconds(6000L)); // 1 hour as default
        if (expiresInSec > 0) {
            Instant expiration = now.plusSeconds(jwtPoperties.getJwtAuth().getTokenExpirationSec());
            expiryDate = Date.from(expiration);
        }

        final Claims claims = Jwts.claims()
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(expiryDate)
                .add(attributes)
                .build();

        return Jwts.builder()
                .claims(claims)
                .signWith(secretKey)
                .compressWith(Jwts.ZIP.DEF)
                .compact();
    }

    @Override
    public Map<String, String> verify(final String token) {
        final JwtParser parser = Jwts
                .parser()
                .requireIssuer(issuer)
                .verifyWith(secretKey)
                .build();

        return parseClaims(() -> parser.parseSignedClaims(token).getPayload());
    }

    @Override
    public Map<String, String> untrusted(final String token) {
        final JwtParser parser = Jwts
                .parser()
                .requireIssuer(issuer)
                .verifyWith(secretKey)
                .build();

        // See: https://github.com/jwtk/jjwt/issues/135
        final String withoutSignature = substringBeforeLast(token, DOT) + DOT;
        return parseClaims(() -> parser.parseUnsecuredClaims(withoutSignature).getPayload());
    }


    private static Map<String, String> parseClaims(final Supplier<Claims> toClaims) {
        try {
            final Claims claims = toClaims.get();
            final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            for (final Map.Entry<String, Object> e : claims.entrySet()) {
                builder.put(e.getKey(), String.valueOf(e.getValue()));
            }
            return builder.build();
        } catch (final IllegalArgumentException | JwtException e) {
            return Map.of();
        }
    }

    /**
     * Pomocna metoda nahrazujici metodu z Apache common knihovny.
     *
     * @param str
     * @param separator
     * @return
     */
    private String substringBeforeLast(String str, String separator) {
        int sepPos = str.lastIndexOf(separator);
        if (sepPos == -1) {
            return "";
        }
        return str.substring(0, sepPos);
    }
}
