package cz.fungisoft.users.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Data object to be sent after successful REST register or login request to client.
 * 
 * @author Michal Vaclavek
 *
 */
@Data
public class AuthRESTResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private String refreshToken;
    
    @JsonIgnore
    private long jwtExpiryDateFromEpoch;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    private LocalDateTime expiryDate;

    public AuthRESTResponse(String accessToken, long jwtExpiryDate, String refreshToken) {
        this.accessToken = accessToken;
        this.jwtExpiryDateFromEpoch = jwtExpiryDate;
        this.refreshToken = refreshToken;
        expiryDate = Instant.ofEpochMilli(jwtExpiryDate * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
