package cz.fungisoft.coffeecompass.controller.models;

import javax.validation.constraints.NotEmpty;

import cz.fungisoft.coffeecompass.validators.ValidEmail;

/**
 * Model used for entering and validating form with only e-mail address input field.
 * 
 * @author Michal
 *
 */
public class EmailAddressModel {

    @NotEmpty
    @ValidEmail
    private String emailAddr = "";
    
    public EmailAddressModel() {
    }
    
    public EmailAddressModel(@NotEmpty String emailAddr) {
        super();
        this.emailAddr = emailAddr;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String email) {
        this.emailAddr = email;
    }
}
