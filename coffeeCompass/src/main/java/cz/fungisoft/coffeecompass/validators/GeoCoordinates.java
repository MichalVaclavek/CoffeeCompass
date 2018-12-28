package cz.fungisoft.coffeecompass.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validator pro zemepisne souradnice. Bude umoznovat vlozit zem. souradnice ve formatu
 * Double cisla a nebo jako st°min'sec''
 * 
 * @author Michal Vaclavek
 *
 */
@Documented
@Constraint(validatedBy = GeoCoordinatesValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
public @interface GeoCoordinates {
    String message() default "Not a valid geo coordinates format.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
