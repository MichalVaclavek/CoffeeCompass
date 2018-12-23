package cz.fungisoft.coffeecompass.dto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import lombok.Data;

/**
 * Trida pro prenos vybranych informaci o objektu CoffeeSite na clienta. Tzv. DTO objekt.
 * 
 * @author Michal Vaclavek
 */
@Data
public class CoffeeSiteDto
{
    private Long id;

    @Size(min=3, max=50)
    private String siteName;
    
    private Date createdOn;
    
    private Set<CoffeeSort> coffeeSorts;

    private String originalUserName;
    private String lastEditUserName;
    
    private CoffeeSiteType typPodniku;
    
    private CoffeeSiteStatus statusZarizeni;
    
    // status zaznamu je potreba prenaset, protoze uzivatele budou moci tento status u "svych" CoffeeSite menit.
    private CoffeeSiteRecordStatus recordStatus; 
    
    private Company dodavatelPodnik;
    
    private Set<CupType> cupTypes;
    
    private AverageStarsForSiteDto averageStarsWithNumOfHodnoceni; 
    
    private PriceRange cena;
    
    @NotNull
    @DecimalMax(value="180.0")
    @DecimalMin(value="-180.0") 
    private Double zemDelka;

    @NotNull
    @DecimalMax(value="180.0")
    @DecimalMin(value="-180.0")
    private Double zemSirka;

    /**
     * Vzdalenost od searchPointu, vypocita CoffeeSiteServiceImpl v pripade, ze se vola jeho metoda 
     * public List<CoffeeSiteDto> findAllWithinCircle(double zemSirka, double zemDelka, int meters, String orderBy, String direction);
     */
    private long distFromSearchPoint;
    
    @Size(max=60)
    private String mesto;

    @Size(max=60)
    private String uliceCP;
    
    @Size(max=50)
    private String pristupnostDny;
    
    @Size(max=50)
    private String pristupnostHod;
    
    @Size(max=240)
    private String initialComment;
    
    @DecimalMax(value="9")
    @DecimalMin(value="0")
    private int numOfCoffeeAutomatyVedleSebe = 1;

    private SiteLocationType typLokality;
    
    private Set<OtherOffer> otherOffers;
    
    private Set<NextToMachineType> nextToMachineTypes;
   
    
    /**
     * Image of the coffeeSite if available
     */
//    private String base64Image;

    /* 
     * Attributes to hold info about "editable" status of the CoffeeSite
     */
    private boolean isVisible;
    private boolean canBeModified;
    private boolean canBeActivated;
    private boolean canBeDeactivated;
    private boolean canBeCanceled;
    private boolean canBeDeleted;
    private boolean canBeCommented;
    private boolean canBeRatedByStars;
}
