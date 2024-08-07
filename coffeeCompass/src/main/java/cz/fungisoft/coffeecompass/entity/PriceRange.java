/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Objects;

/**
 * Cenovy rozsah.
 * 
 * @author Michal Vaclavek
 */
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="price_range", schema="coffeecompass")
public class PriceRange {

    /* ======= INSTANCES VARIABLES ======== */
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @NotNull // Validace vstupu, nesmi byt null
    @Column(name = "price_range", unique=true, length=45)
    private String priceRange;
    
    @Override
    public String toString() {
        return priceRange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PriceRange that = (PriceRange) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}