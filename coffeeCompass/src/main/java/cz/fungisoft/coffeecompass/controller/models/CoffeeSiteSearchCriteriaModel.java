package cz.fungisoft.coffeecompass.controller.models;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import cz.fungisoft.coffeecompass.entity.CoffeeSort.CoffeeSortEnum;
import lombok.Data;

/**
 * Object pro View model pro Search operaci pri hledani CoffeeSite podle:<br>
 * latitude<br>
 * longitude<br>
 * range (meters)<br>
 * 
 *{@link CoffeeSiteStatus}<br>
 *{@link CoffeeSort}<br>
 * 
 * @author Michal Václavek
 */
@Data
public class CoffeeSiteSearchCriteriaModel
{
    @NotNull
    @DecimalMax(value="180.0")
    @DecimalMin(value="-180.0")
    private Double lat1 = 49.8250401; // defaultni hodnoty, stred CR
    
    @NotNull
    @DecimalMax(value="180.0")
    @DecimalMin(value="-180.0")
    private Double lon1 = 15.4190817; // defaultni hodnoty, stred CR
    
    @NotNull
    @DecimalMax(value="50000") // Max. 50 km. (50 000 m)
    @DecimalMin(value="10")
    private Long range = 500L; // default value 500 m
    
    private String coffeeSiteStatus;
    
    /**
     * Atribut pro model, ktery urcuje, jestli se ve View bude vybirat coffeeSort nebo ne
     */
    private Boolean sortSelected = false; // defaultni hodnota false, nevybira se podle CoffeeSort
    
    private String coffeeSort = CoffeeSortEnum.ESPRESSO.getCoffeeType();
    
}
