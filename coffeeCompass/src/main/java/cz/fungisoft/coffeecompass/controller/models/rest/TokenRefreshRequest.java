package cz.fungisoft.coffeecompass.controller.models.rest;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class TokenRefreshRequest {

    @NotBlank
    private String refreshToken;

    private String deviceID;
}
