package cz.fungisoft.coffeecompass.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import cz.fungisoft.coffeecompass.dto.UserDataDTO;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object>
{

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {       
    }
    
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){   
        UserDataDTO user = (UserDataDTO) obj;
        return user.getPassword().equals(user.getConfirmPassword());    
    }   

}
