package cz.fungisoft.coffeecompass.controller.models.rest;

import java.util.Date;

import lombok.Data;

@Data
public class AuthRESTResponse
{
    private String accessToken;
    private String tokenType = "Bearer";
    private Date expiryDate;

    public AuthRESTResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
