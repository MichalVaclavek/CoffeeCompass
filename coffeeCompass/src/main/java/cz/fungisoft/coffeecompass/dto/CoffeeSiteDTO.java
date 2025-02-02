package cz.fungisoft.coffeecompass.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Trida pro prenos vybranych informaci o objektu CoffeeSite na clienta, tzv. DTO objekt.
 * A pro mapovani dat z Formulare. 
 * 
 * @author Michal Vaclavek
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CoffeeSiteDTO extends BaseItem {

    @Size(min=3, max=50)
    private String siteName;
    
    public void setSiteName(String siteName) {
        if (siteName != null) {
            this.siteName = siteName.trim();
        }
    }
    
    @JsonFormat(pattern = "dd. MM. yyyy HH:mm", timezone="Europe/Prague")
    private LocalDateTime createdOn;
    
    private Set<CoffeeSortDTO> coffeeSorts;

    private String originalUserName;
    private String lastEditUserName;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CoffeeSiteTypeDTO typPodniku;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CoffeeSiteStatusDTO statusZarizeni;
    
    // status zaznamu je potreba prenaset, protoze uzivatele budou moci tento status u "svych" CoffeeSite menit.
    private CoffeeSiteRecordStatusDTO recordStatus;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CompanyDTO dodavatelPodnik;
    
    private Set<CupTypeDTO> cupTypes;
    
    private AverageStarsForSiteDTO averageStarsWithNumOfHodnoceni;

//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PriceRangeDTO cena;

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

    @DecimalMax(value="9")
    @DecimalMin(value="0")
    private int numOfCoffeeAutomatyVedleSebe = 0;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SiteLocationTypeDTO typLokality;

    private Set<OtherOfferDTO> otherOffers;

    private Set<NextToMachineTypeDTO> nextToMachineTypes;

    /**
     * To indicate if the Image is saved for this CoffeeSite.
     * Used especialy for REST services.
     */
    private String mainImageURL = ""; // default value for image URL. Means, no image available if empty, otherwise URL of the image inserted by CoffeeSite service evaluateOperationalAttributes() method


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
