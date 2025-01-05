/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;

/**
 * Dalsi nabidka, krome kafe ... caj, kafe s necim, polivka atp.
 * 
 * @author Michal Vaclavek
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@Entity
@Table(name="nabidka", schema="coffeecompass")
public class OtherOffer extends BaseEntity {

//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Integer longId;
    
    @NotNull // Validace vstupu, nesmi byt null
    @Size(min = 3, max = 45) // Validace vstupu, pocet znaku
    @Column(name = "nabidka", unique=true)
    private String offer;

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//
//        OtherOffer other = (OtherOffer) obj;
//        if (!Objects.equals(longId, other.longId))
//            return false;
//        if (offer == null)
//        {
//            return other.offer == null;
//        } else return offer.equals(other.offer);
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + longId; // Pokud se do vypoctu hashCode() pouzije id, pak metody assertThat().equals()
//                                      // pro 2 stejne objekty Offer vrati false :-(
//        result = prime * result + ((offer == null) ? 0 : offer.hashCode());
//        return result;
//    }
    
    @Override
    public String toString() {
        return offer;
    }
}
