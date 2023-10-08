package cz.fungisoft.coffeecompass.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.compression.GzipCompressionCodec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

import cz.fungisoft.coffeecompass.configuration.JwtAndOAuth2Properties;
import cz.fungisoft.coffeecompass.service.tokens.TokenService;

import java.util.Date;
import java.util.Map;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.impl.TextCodec.BASE64;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

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
@Service("jwtTokenProviderService")
public class JwtTokenProviderService implements Clock, TokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderService.class);
    
    private static final String DOT = ".";
    private static final GzipCompressionCodec COMPRESSION_CODEC = new GzipCompressionCodec();

    private final String issuer;
    private final int expirationSec;
    private final int clockSkewSec;
    private final String secretKey;
    

    private final JwtAndOAuth2Properties jwtPoperties;

    public JwtTokenProviderService(JwtAndOAuth2Properties appProperties) {
        this.jwtPoperties = appProperties;
        
        this.issuer = requireNonNull(jwtPoperties.getJwtAuth().getIssuer());
        this.expirationSec = jwtPoperties.getJwtAuth().getTokenExpirationSec();
        this.clockSkewSec = jwtPoperties.getJwtAuth().getClockSkewSec();
        this.secretKey = BASE64.encode(requireNonNull(jwtPoperties.getJwtAuth().getTokenSecret()));
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
        Date expiryDate = new Date(now.getTime() + jwtPoperties.getJwtAuth().getTokenExpirationSec());

        return Jwts.builder()
                   .setSubject(Long.toString(userPrincipal.getId()))
                   .setIssuedAt(new Date())
                   .setExpiration(expiryDate)
                   .signWith(SignatureAlgorithm.HS512, jwtPoperties.getJwtAuth().getTokenSecret())
                   .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                            .setSigningKey(jwtPoperties.getJwtAuth().getTokenSecret())
                            .parseClaimsJws(token)
                            .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        
        try {
            Jwts.parser().setSigningKey(jwtPoperties.getJwtAuth().getTokenSecret()).parseClaimsJws(authToken);
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
    
    /** REST mobile app. **/

    @Override
    public String permanent(final Map<String, String> attributes) {
        return newToken(attributes, 0);
    }

    @Override
    public String expiring(final Map<String, String> attributes) {
        return newToken(attributes, expirationSec);
    }

    private String newToken(final Map<String, String> attributes, final int expiresInSec) {
        LocalDateTime now = LocalDateTime.now();
        final Claims claims = Jwts.claims()
                                  .setIssuer(issuer)
                                  .setIssuedAt(convertToDate(now));

        if (expiresInSec > 0) {
            Date expiryDate = convertToDate(now.plusSeconds(jwtPoperties.getJwtAuth().getTokenExpirationSec()));
            claims.setExpiration(expiryDate);
        }
        claims.putAll(attributes);

        return Jwts.builder()
                   .setClaims(claims)
                   .signWith(HS256, secretKey)
                   .compressWith(COMPRESSION_CODEC)
                   .compact();
    }

    @Override
    public Map<String, String> verify(final String token) {
        final JwtParser parser = Jwts.parser()
                                     .requireIssuer(issuer)
                                     .setClock(this)
                                     .setAllowedClockSkewSeconds(clockSkewSec)
                                     .setSigningKey(secretKey);
        return parseClaims(() -> parser.parseClaimsJws(token).getBody());
    }

    @Override
    public Map<String, String> untrusted(final String token) {
          
        final JwtParser parser = Jwts.parser()
                                     .requireIssuer(issuer)
                                     .setClock(this)
                                     .setAllowedClockSkewSeconds(clockSkewSec);

        // See: https://github.com/jwtk/jjwt/issues/135
        final String withoutSignature = substringBeforeLast(token, DOT) + DOT;
        return parseClaims(() -> parser.parseClaimsJwt(withoutSignature).getBody());
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
            return ImmutableMap.of();
        }
    }

    @Override
    public Date now() {
        return convertToDate(LocalDateTime.now());
    }
      
    /**
     * Pomocna metoda n prevod novych LocalDateTime instanci na stary Date, ktery
     * je vyzadovan JWT knihovnou
     * @param dateToConvert
     * @return
     */
    private Date convertToDate(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
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
