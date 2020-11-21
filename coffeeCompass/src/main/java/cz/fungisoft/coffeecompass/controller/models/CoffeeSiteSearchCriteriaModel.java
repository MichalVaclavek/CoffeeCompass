package cz.fungisoft.coffeecompass.controller.models;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import cz.fungisoft.coffeecompass.entity.CoffeeSort.CoffeeSortEnum;
import lombok.Data;

/**
 * Object pro View model pro Search operaci pri hledani CoffeeSite podle:<br>
 *  
 *  latitude<br>
 *  longitude<br>
 *  range (meters)<br>
 *  {@link CoffeeSiteStatus}<br>
 *  {@link CoffeeSort}<br>
 *  city name.<br>
 *  
 *  Trida tedy sdruzuje parametry, podle kterych lze vyhledavat CoffeeSite.
 *  Pouzito prevazne ve View coffeesite_search.html
 * 
 * @author Michal Václavek
 */
@Data
public class CoffeeSiteSearchCriteriaModel
{
    
    private final Double STRED_CR_LAT = 49.8250401; // defaultni hodnoty, stred CR
    private final Double STRED_CR_LON = 15.4190817; // defaultni hodnoty, stred CR
    
    @DecimalMax(value="180.0")
    @DecimalMin(value="-180.0")
    private Double lat1 = STRED_CR_LAT; // defaultni hodnoty, stred CR
    
    @DecimalMax(value="180.0")
    @DecimalMin(value="-180.0")
    private Double lon1 = STRED_CR_LON; // defaultni hodnoty, stred CR
    
    @NotNull
    @DecimalMax(value="50000") // Max. 50 km. (50 000 m)
    @DecimalMin(value="10")
    private Long range = 5000L; // default value 5000 m (5 km)
    
    private String coffeeSiteStatus;
    
    /**
     * Atribut pro model, ktery urcuje, jestli se ve View bude vybirat coffeeSort nebo ne
     */
    private Boolean sortSelected = false; // defaultni hodnota false, nevybira se podle CoffeeSort
    
    private String coffeeSort = CoffeeSortEnum.ESPRESSO.getCoffeeType();
    
    private String cityName = "";
    
    /**
     * To specify, if the cityName search criteria is used as city name searched in CoffeeSites
     * or if it is input for api.mapy.cz to find geo location of search point.
     */
    private boolean searchCityNameExactly;
    
    /**
     * Reset search criteria model in case another Form was used for searching
     */
    public void resetSearchFromLocation() {
       this.lat1 = STRED_CR_LAT;
       this.lon1 = STRED_CR_LON;
    }
}
