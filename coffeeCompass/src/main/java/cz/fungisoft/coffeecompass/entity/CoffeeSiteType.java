/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;


/**
 * Typ "podniku" - automat, bistro, kavarna atp.
 * 
 * @author Michal Vaclavek
 */
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name="typ_podniku", schema="coffeecompass")
public class CoffeeSiteType extends BaseEntity {

    /* ======= INSTANCES VARIABLES ======== */
    @NotNull
    @Column(name = "typ_zarizeni", unique=true, length=45)
    private String coffeeSiteType;
    
    @Override
    public String toString() {
        return coffeeSiteType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CoffeeSiteType that = (CoffeeSiteType) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
