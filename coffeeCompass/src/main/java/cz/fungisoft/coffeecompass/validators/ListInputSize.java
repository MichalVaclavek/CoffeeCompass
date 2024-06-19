package cz.fungisoft.coffeecompass.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation used to mark REST controllers methods parameter to be validated for number of items in a list.
 * So applicable only to List<?>
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ListInputSizeValidatorREST.class)
public @interface ListInputSize {

    int max() default 20; // maximum number of items in a list
    int min() default 1; // minimum number of items in a list

    String message() default "List of elements to be created/updated must have %d items at most and %d at least.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
