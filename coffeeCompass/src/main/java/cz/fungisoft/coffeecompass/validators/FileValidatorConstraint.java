package cz.fungisoft.coffeecompass.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Overeni, ze byl vybran soubor k uploadu.
 * 
 * @author Michal
 *
 */
@Documented
@Constraint(validatedBy = FileValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
public @interface FileValidatorConstraint {
    String message() default "Please, select valid file to upload.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
