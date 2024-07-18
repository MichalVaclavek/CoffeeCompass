package cz.fungisoft.test.image2.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Pro overeni, ze byl vybran jpg, jpeg nebo png soubor k uploadu<br>
 * a ze velikost uploadovaneho souboru nepresahuje definovanou hodnotu.
 * 
 * @author Michal Václavek
 */
@Documented
@Constraint(validatedBy = cz.fungisoft.test.image2.validators.ImageFileValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
public @interface ImageFileValidatorConstraint {

    String message() default "Only jpg, jpeg or png files allowed.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
