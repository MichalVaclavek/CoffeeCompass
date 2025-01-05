/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Druh kavy. Obsahuje i výčet konstantních hodnot, které odpovídají hodnotám v DB.
 * 
 * @author Michal Vaclavek
 *
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@Entity
@Table(name="druhy_kavy", schema="coffeecompass")
public class CoffeeSort extends BaseEntity {

//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Integer longId;
    
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
    
    
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((coffeeSort == null) ? 0 : coffeeSort.hashCode());
//        return result;
//    }


//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//
//        CoffeeSort other = (CoffeeSort) obj;
//        if (coffeeSort == null)
//        {
//            if (other.coffeeSort != null)
//                return false;
//        } else if (!coffeeSort.equals(other.coffeeSort))
//            return false;
//
//        return (Objects.equals(id, other.id));
//    }

    @Override
    public String toString() {
        return coffeeSort;
    }
}