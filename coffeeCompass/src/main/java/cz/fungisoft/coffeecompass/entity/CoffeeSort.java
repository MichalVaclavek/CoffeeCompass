/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * Druh kavy. Obsahuje i výčet konstantních hodnot, které odpovídají hodnotám v DB.
 * 
 * @author Michal Vaclavek
 *
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Entity
@Table(name="druhy_kavy", schema="coffeecompass")
public class CoffeeSort extends BaseEntity {

    @NotNull
    @Column(name = "druh_kavy", unique=true)
    private String coffeeSort;
   
    /* CONST FOR BASIC COFFEESORT INNER STATIC CLASS */
    public enum CoffeeSortEnum implements Serializable {
        ESPRESSO("espresso"),
        FILTERED("překapávaná"),
        INSTANT("instantní"),
        BEANS("zrnková"),
        TUREK("turek");
        
        String coffeeType;
         
        CoffeeSortEnum(String cfType) {
            this.coffeeType = cfType;
        }
         
        public String getCoffeeType() {
            return coffeeType;
        }
    }
    
    @Override
    public String toString() {
        return coffeeSort;
    }
}