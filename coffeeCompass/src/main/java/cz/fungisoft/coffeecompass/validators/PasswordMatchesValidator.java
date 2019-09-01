package cz.fungisoft.coffeecompass.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import cz.fungisoft.coffeecompass.controller.models.NewPasswordInputModel;
import cz.fungisoft.coffeecompass.dto.UserDataDTO;

/**
 * Validates if password and confirmationPassword matches.<br>
 * 
 * Can be used only for {@link UserDataDTO} and {@link NewPasswordInputModel} classes.
 * 
 * @author Michal Vaclavek
 *
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object>
{

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {       
    }
    
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {   
        
        boolean result = true;
        String errorMessageKey = "";
        
        if (obj instanceof UserDataDTO) {
            UserDataDTO user = (UserDataDTO) obj;
            result = user.getPassword().equals(user.getConfirmPassword());
        }
        if (obj instanceof NewPasswordInputModel) {
            NewPasswordInputModel changePasswd = (NewPasswordInputModel) obj;
            result = changePasswd.getNewPassword().equals(changePasswd.getConfirmPassword());
        }
        
        if (!result) {
            errorMessageKey = "{error.user.password.notmatching}";
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessageKey) // retrieve message from Validation Messages source defined in CoffeeCompassConfiguration
               .addConstraintViolation();
        }
        
        return result ;
    }   

}
