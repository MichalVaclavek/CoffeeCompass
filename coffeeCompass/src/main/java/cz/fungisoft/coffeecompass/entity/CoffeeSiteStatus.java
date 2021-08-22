/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Status coffee situ.<br>
 *  INSERVICE("V provozu")<br>
 *  CANCELED("Zrušeno")<br>
 *  TEMP_CANCELED("Dočasně zrušeno")<br>
 *  TEMP_OPENED("Dočasně otevřeno")<br>
 * 
 * @author Michal Vaclavek
 *
 */
@Data
@Entity
@javax.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="coffee_site_status", schema="coffeecompass")
public class CoffeeSiteStatus {

    /* INNER STATIC CLASS */
    public enum CoffeeSiteStatusEnum implements Serializable {
        INSERVICE("V provozu"),
        CANCELED("Zrušeno"),
        TEMP_CANCELED("Dočasně zrušeno"),
        TEMP_OPENED("Dočasně otevřeno");
        
        String siteStatus;
         
        CoffeeSiteStatusEnum(String siteStatus) {
            this.siteStatus = siteStatus;
        }
         
        public String getSiteStatus() {
            return siteStatus;
        }
    }

    /* ======= INSTANCES VARIABLES ======== */
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @NotNull
    @Column(name="status_podniku", unique=true, nullable=false)
    private String status = CoffeeSiteStatusEnum.INSERVICE.getSiteStatus(); 
    
    @Override
    public String toString() {
        return status;
    }
}
