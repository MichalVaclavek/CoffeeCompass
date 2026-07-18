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
        INSERVICE("V provozu", "In service"),
        CANCELED("Zrušeno", "Cancelled"),
        TEMP_CANCELED("Dočasně zrušeno", "Temporarily cancelled"),
        TEMP_OPENED("Dočasně otevřeno", "Temporarily opened");

        final String siteStatus;
        final String siteStatusEn;

        CoffeeSiteStatusEnum(String siteStatus, String siteStatusEn) {
            this.siteStatus = siteStatus;
            this.siteStatusEn = siteStatusEn;
        }

        public String getSiteStatus() {
            return siteStatus;
        }

        public String getSiteStatusEn() {
            return siteStatusEn;
        }
    }

    /* ======= INSTANCES VARIABLES ======== */
    
    @NotNull
    @Column(name="status_podniku", unique=true, nullable=false)
    private String status = CoffeeSiteStatusEnum.INSERVICE.name();

    public CoffeeSiteStatusEnum getCoffeeSiteStatus() {
        return CoffeeSiteStatusEnum.valueOf(status);
    }
    
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
