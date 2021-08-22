/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

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
 * Typ "podniku" - automat, bistro, kavarna atp.
 * 
 * @author Michal Vaclavek
 */
@Data
@Entity
@javax.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="typ_podniku", schema="coffeecompass")
public class CoffeeSiteType {

    /* ======= INSTANCES VARIABLES ======== */
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @NotNull
    @Column(name = "typ_zarizeni", unique=true, length=45)
    private String coffeeSiteType;
    
    @Override
    public String toString() {
        return coffeeSiteType;
    }
}
