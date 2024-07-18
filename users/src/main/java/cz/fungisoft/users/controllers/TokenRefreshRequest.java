package cz.fungisoft.users.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRefreshRequest {

    @NotBlank
    private String refreshToken;

    private String deviceID;
}
