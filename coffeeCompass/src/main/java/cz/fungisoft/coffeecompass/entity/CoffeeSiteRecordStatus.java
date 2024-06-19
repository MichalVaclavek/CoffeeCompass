package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Status zaznamu o Coffee situ.
 * 
 * @author Michal Václavek
 *
 */
@Data
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="status_coffee_site_zaznamu", schema="coffeecompass")
public class CoffeeSiteRecordStatus {

    public enum CoffeeSiteRecordStatusEnum implements Serializable {
        ACTIVE("ACTIVE"),
        INACTIVE("INACTIVE"),
        CANCELED("CANCELED"),
        CREATED("CREATED");
         
        String siteRecordStatus;
         
        CoffeeSiteRecordStatusEnum(String siteRecordStatus) {
            this.siteRecordStatus = siteRecordStatus;
        }
         
        public String getSiteRecordStatus() {
            return siteRecordStatus;
        }         
    }

    /* ======= INSTANCE VARIABLES ======== */
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @NotNull
    @Column(name="status_zaznamu", length=15, unique=true, nullable=false)
    private String status = CoffeeSiteRecordStatusEnum.CREATED.getSiteRecordStatus(); // defaultni hodnota pri vytvoreni
    
    public CoffeeSiteRecordStatusEnum getRecordStatus() {
        return CoffeeSiteRecordStatusEnum.valueOf(status);
    }
}
