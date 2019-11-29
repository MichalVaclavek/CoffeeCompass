package cz.fungisoft.coffeecompass.controller.models.rest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class AuthRESTResponse
{
    private String accessToken;
    private String tokenType = "Bearer";
    @JsonIgnore
    private long jwtExpiryDateFromEpoch;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    private LocalDateTime expiryDate;

    public AuthRESTResponse(String accessToken, long jwtExpiryDate) {
        this.accessToken = accessToken;
        this.jwtExpiryDateFromEpoch = jwtExpiryDate;
        expiryDate = Instant.ofEpochMilli(jwtExpiryDate * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
