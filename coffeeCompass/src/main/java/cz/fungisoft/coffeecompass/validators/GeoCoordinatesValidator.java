package cz.fungisoft.coffeecompass.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Will be used to validate View input for CoffeeSite geo coordinates.<br>
 * Can be inserted in two forms: Double number as decimal degrees or
 * as XY°AB'CD''
 *   
 * @author Michal Václavek
 *
 */
public class GeoCoordinatesValidator implements ConstraintValidator<GeoCoordinates, String>
{
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // TODO Auto-generated method stub
        return false;
    }
}
