package cz.fungisoft.coffeecompass.controller.models.rest;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * A DTO object for accepting REST register request from mobile
 * client app. It is simlified version of {@link cz.fungisoft.coffeecompass.dto.UserDTO}.
 * but deviceID attribute is added to distinquish what device
 * the user is registering or loging from.
 * 
 * @author Michal Vaclavek
 *
 */
@Data
public class SignUpAndLoginRESTDto {
    
    @NotBlank
    private String userName;

    @Email
    private String email;

    @NotBlank
    private String password;
    
    @NotBlank
    private String deviceID;

}