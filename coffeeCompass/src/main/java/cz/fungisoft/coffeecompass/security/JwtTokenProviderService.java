package cz.fungisoft.coffeecompass.security;

import io.jsonwebtoken.*;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import cz.fungisoft.coffeecompass.configuration.JwtAndOAuth2Properties;
import cz.fungisoft.coffeecompass.service.tokens.TokenService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.function.Supplier;

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
//    private static final GzipCompressionCodec COMPRESSION_CODEC = new GzipCompressionCodec();

    private final String issuer;
    private final int expirationSec;
//    private final int clockSkewSec;
    private final SecretKey secretKey;
//    private final PublicKey publicKey;
    

    private final JwtAndOAuth2Properties jwtPoperties;

    public JwtTokenProviderService(JwtAndOAuth2Properties appProperties) {
        this.jwtPoperties = appProperties;
        
        this.issuer = requireNonNull(jwtPoperties.getJwtAuth().getIssuer());
        this.expirationSec = jwtPoperties.getJwtAuth().getTokenExpirationSec();
//        this.clockSkewSec = jwtPoperties.getJwtAuth().getClockSkewSec();
        byte[] keyBytes = Decoders.BASE64.decode(jwtPoperties.getJwtAuth().getTokenSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        byte[] publicKeyBytes = Decoders.BASE64.decode(jwtPoperties.getJwtAuth().getPublicKey());
//        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
//        this.publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
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
                .subject(userPrincipal.getId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiryDate)
                .signWith(this.secretKey)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts
                .parser()
//                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        
        try {
            Jwts
                    .parser()
//                    .verifyWith(publicKey)
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
        Date expiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 24);
        if (expiresInSec > 0) {
            expiryDate = convertToDate(now.plusSeconds(jwtPoperties.getJwtAuth().getTokenExpirationSec()));
        }

        final Claims claims = Jwts.claims()
                                  .issuer(issuer)
                                  .issuedAt(convertToDate(now))
                                  .expiration(expiryDate)
                                  .build();

        claims.putAll(attributes);

        return Jwts.builder()
                   .claims(claims)
                   .signWith(secretKey)
//                   .compressWith(COMPRESSION_CODEC)
                   .compact();
    }

    @Override
    public Map<String, String> verify(final String token) {
        final JwtParser parser = Jwts
                .parser()
                .requireIssuer(issuer)
//                .verifyWith(publicKey)
                .clock(this)
                .build();

        return parseClaims(() -> parser.parseSignedClaims(token).getPayload());
    }

    @Override
    public Map<String, String> untrusted(final String token) {
        final JwtParser parser = Jwts
                .parser()
                .requireIssuer(issuer)
//                .verifyWith(publicKey)
                .clock(this)
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
