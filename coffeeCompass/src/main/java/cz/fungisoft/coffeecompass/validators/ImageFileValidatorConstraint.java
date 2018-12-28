package cz.fungisoft.coffeecompass.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Overeni, ze byl vybran jpg, jpeg nebo png soubor k uploadu.
 * 
 * @author Michal Václavek
 */
@Documented
@Constraint(validatedBy = ImageFileValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
public @interface ImageFileValidatorConstraint {
    String message() default "Only jpg, jpeg or png files allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
