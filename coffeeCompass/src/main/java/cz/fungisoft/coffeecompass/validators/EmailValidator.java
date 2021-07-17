package cz.fungisoft.coffeecompass.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * Validator for e-mail address.
 * <p>
 * If the attribute 'canbeempty' is set to true, then emty email String is valid. (used for UserDataDTO obejct,<br>
 * where the e-mail is not mandatory for registered user).
 * 
 * @author Michal Vaclavek
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

   private Pattern pattern;
   private Matcher matcher;
   
   private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";
   
   private boolean canbeempty;
   
   @Override
   public void initialize(ValidEmail constraintAnnotation) {
       this.canbeempty = constraintAnnotation.canbeempty();
   }
   
   @Override
   public boolean isValid(String email, ConstraintValidatorContext cxt) {
       
       boolean result = true;
       String errorMessageKey = "";
       
       if (canbeempty && (email == null || email.isEmpty()) ) {
           return true;
       }
       
       if (email == null || !validateEmail(email)) {
           errorMessageKey = "{error.user.email.wrong}";
           result = false;
       }
       
       if (!result) {
           cxt.disableDefaultConstraintViolation();
           cxt.buildConstraintViolationWithTemplate(errorMessageKey) // retrieve message from Validation Messages source defined in CoffeeCompassConfiguration
              .addConstraintViolation();
       }
       return result;
   } 
   
   private boolean validateEmail(String email) {
       pattern = Pattern.compile(EMAIL_PATTERN);
       matcher = pattern.matcher(email);
       return matcher.matches();
   }
}