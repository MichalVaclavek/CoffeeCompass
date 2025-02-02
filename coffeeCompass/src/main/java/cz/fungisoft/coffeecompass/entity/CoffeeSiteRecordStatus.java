package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

/**
 * Status zaznamu o Coffee situ.
 * 
 * @author Michal Václavek
 *
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name="status_coffee_site_zaznamu", schema="coffeecompass")
public class CoffeeSiteRecordStatus extends BaseEntity {

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
    
    @NotNull
    @Column(name="status_zaznamu", length=15, unique=true, nullable=false)
    private String status = CoffeeSiteRecordStatusEnum.CREATED.getSiteRecordStatus(); // defaultni hodnota pri vytvoreni
    
    public CoffeeSiteRecordStatusEnum getRecordStatus() {
        return CoffeeSiteRecordStatusEnum.valueOf(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CoffeeSiteRecordStatus that = (CoffeeSiteRecordStatus) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
