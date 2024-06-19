package cz.fungisoft.coffeecompass.validators;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.exceptions.rest.BadRESTRequestException;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * Validates the size of input list of CoffeeSites when POSTing for saving/updating
 */
public class ListInputSizeValidatorREST implements ConstraintValidator<ListInputSize, List<CoffeeSiteDTO>> {

    private int maxListSize;
    private int minListSize;

    private String defaultMessageTemplate;

    @Override
    public void initialize(ListInputSize constraintAnnotation) {
        this.defaultMessageTemplate = constraintAnnotation.message();
        this.maxListSize = constraintAnnotation.max();
        this.minListSize = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(List<CoffeeSiteDTO> values, ConstraintValidatorContext context) {

        boolean result = values.size() <= maxListSize && values.size() >= minListSize;

        if (!result) {
            // Overrides standard MethodArgumentNotValidException, which is processed by SpringMVC and returned within error.html page,
            // to InvalidParameterValueException or BadRESTRequestException.
            // This Overriding ensures, that ExceptionsControllerRESTAdvice will be invoked and REST error message will be returned
            // If InvalidParameterValueException used, then it is not used properly, as the InvalidParameterValueException
            // is intended to be used when requesting not available resources not for validating Controller input
            //TODO - read error message template from messages.properties
            throw new BadRESTRequestException(String.format(defaultMessageTemplate, maxListSize, minListSize));
        }

        return result;
    }
}
