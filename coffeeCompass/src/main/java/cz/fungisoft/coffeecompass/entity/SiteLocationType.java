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
 * Typ lokality kde se vyskytuje CoffeeSite. Napr. nadrazi, obchodni centrum atp.
 * 
 * @author Michal Václavek
 */
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name="typ_lokality", schema="coffeecompass")
public class SiteLocationType extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer longId;
    
    @NotNull // Validace vstupu, nesmi byt null
    @Column(name = "lokalita", unique=true, length=55)
    private String locationType;
    
    @Override
    public String toString() {
        return locationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SiteLocationType that = (SiteLocationType) o;
        return longId != null && Objects.equals(longId, that.longId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
