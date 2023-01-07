package cz.fungisoft.coffeecompass.controller.models.rest;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * A DTO object for accepting REST register or login request from mobile
 * client app.<br>
 * It is simplified version of {@link cz.fungisoft.coffeecompass.dto.UserDTO}.
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
    
    public void setUserName(String userName) {
        this.userName = userName.trim();
    }

    @Email
    private String email;
    
    public void setEmail(String email) {
        this.email = email.trim();
    }

    @NotBlank
    private String password;
    
    private String deviceID;
}