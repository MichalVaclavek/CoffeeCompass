package cz.fungisoft.coffeecompass.controller.models;

import javax.validation.constraints.Size;

import cz.fungisoft.coffeecompass.validators.PasswordMatches;

/**
 * Class model used for entering new user's password in case old one
 * is forgoten and required to reset by e-mail link.
 * 
 * @author Michal Vaclavek
 *
 */
@PasswordMatches
public class NewPasswordInputModel
{
    @Size(min = 4)
    private String newPassword;
    
    @Size(min = 4)
    private String confirmPassword;
    

    public NewPasswordInputModel() {
        super();
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
