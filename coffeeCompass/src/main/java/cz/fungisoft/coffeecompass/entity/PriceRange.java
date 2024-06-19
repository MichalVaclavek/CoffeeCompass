/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Cenovy rozsah.
 * 
 * @author Michal Vaclavek
 */
@Data
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="price_range", schema="coffeecompass")
public class PriceRange {

    /* ======= INSTANCES VARIABLES ======== */
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @NotNull // Validace vstupu, nesmi byt null
    @Column(name = "price_range", unique=true, length=45)
    private String priceRange;
    
    @Override
    public String toString() {
        return priceRange;
    }
}