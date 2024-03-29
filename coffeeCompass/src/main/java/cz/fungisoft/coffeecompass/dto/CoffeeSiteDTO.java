package cz.fungisoft.coffeecompass.dto;

import java.time.LocalDateTime;
import java.util.Set;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import lombok.Data;

/**
 * Trida pro prenos vybranych informaci o objektu CoffeeSite na clienta, tzv. DTO objekt.
 * A pro mapovani dat z Formulare. 
 * 
 * @author Michal Vaclavek
 */
@Data
public class CoffeeSiteDTO {

    private Long id;

    @Size(min=3, max=50)
    private String siteName;
    
    public void setSiteName(String siteName) {
        if (siteName != null) {
            this.siteName = siteName.trim();
        }
    }
    
    @JsonFormat(pattern = "dd. MM. yyyy HH:mm", timezone="Europe/Prague")
    private LocalDateTime createdOn;
    
    private Set<CoffeeSort> coffeeSorts;

    private String originalUserName;
    private String lastEditUserName;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CoffeeSiteType typPodniku;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CoffeeSiteStatus statusZarizeni;
    
    // status zaznamu je potreba prenaset, protoze uzivatele budou moci tento status u "svych" CoffeeSite menit.
    private CoffeeSiteRecordStatusDTO recordStatus;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Company dodavatelPodnik;
    
    private Set<CupType> cupTypes;
    
    private AverageStarsForSiteDTO averageStarsWithNumOfHodnoceni;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    
    public void setMesto(String mesto) {
        if (mesto != null) {
            this.mesto = mesto.trim();
        }
    }

    @Size(max=60)
    private String uliceCP;
    
    public void setUliceCP(String uliceCP) {
        if (uliceCP != null) {
            this.uliceCP = uliceCP.trim();
        }
    }
    
    @Size(max=50)
    private String pristupnostDny;
    
    public void setPristupnostDny(String pristupnostDny) {
        if (pristupnostDny != null) {
            this.pristupnostDny = pristupnostDny.trim();
        }
    }
    
    @Size(max=50)
    private String pristupnostHod;
    
    public void setPristupnostHod(String pristupnostHod) {
        if (pristupnostHod != null) {
            this.pristupnostHod = pristupnostHod.trim();
        }
    }
    
    @Size(max=240)
    private String initialComment;
    
    public void setInitialComment(String initialComment) {
        if (initialComment != null) {
            this.initialComment = initialComment.trim();
        }
    }
    
    @DecimalMax(value="9")
    @DecimalMin(value="0")
    private int numOfCoffeeAutomatyVedleSebe = 0;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SiteLocationType typLokality;
    
    private Set<OtherOffer> otherOffers;
    
    private Set<NextToMachineType> nextToMachineTypes;
   
    /**
     * To indicate if the Image is saved for this CoffeeSite.
     * Used especialy for REST services.
     */
    private String mainImageURL = ""; // default value for image URL. Means, no image available if empty, otherwise URL of the image inserted by CoffeeSite service evaluateOperationalAttributes() method
    
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
