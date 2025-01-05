/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

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
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name="coffee_site_status", schema="coffeecompass")
public class CoffeeSiteStatus extends BaseEntity {

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
    
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Integer longId;
    
    @NotNull
    @Column(name="status_podniku", unique=true, nullable=false)
    private String status = CoffeeSiteStatusEnum.INSERVICE.getSiteStatus(); 
    
    @Override
    public String toString() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CoffeeSiteStatus that = (CoffeeSiteStatus) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
