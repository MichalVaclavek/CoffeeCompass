package cz.fungisoft.coffeecompass.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER}) 
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
@Documented
public @interface ValidEmail {

    String message() default "{error.user.email.wrong}";
    Class<?>[] groups() default {}; 
    Class<? extends Payload>[] payload() default {};
    
    /**
     * @return true, if the e-mail address can be null or empty.
     */
    boolean canbeempty() default false;
}
