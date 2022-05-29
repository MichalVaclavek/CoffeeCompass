package cz.fungisoft.coffeecompass.controller.models.rest;

import lombok.Data;

/**
 * Data object to be sent after successful REST register or login request to client.
 * 
 * @author Michal Vaclavek
 *
 */
@Data
public class TokenRefreshResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    public TokenRefreshResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
