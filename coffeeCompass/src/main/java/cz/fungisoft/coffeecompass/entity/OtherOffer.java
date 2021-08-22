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
import javax.validation.constraints.Size;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Dalsi nabidka, krome kafe ... caj, kafe s necim, polivka atp.
 * 
 * @author Michal Vaclavek
 */
@Data
@Entity
@javax.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="nabidka", schema="coffeecompass")
public class OtherOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @NotNull // Validace vstupu, nesmi byt null
    @Size(min = 3, max = 45) // Validace vstupu, pocet znaku
    @Column(name = "nabidka", unique=true)
    private String offer;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        
        OtherOffer other = (OtherOffer) obj;
        if (id != other.id)
            return false;
        if (offer == null)
        {
            if (other.offer != null)
                return false;
        } else if (!offer.equals(other.offer))
            return false;
        
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id; // Pokud se do vypoctu hashCode() pouzije id, pak metody assertThat().equals()
                                      // pro 2 stejne objekty Offer vrati false :-(
        result = prime * result + ((offer == null) ? 0 : offer.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return offer;
    }
}
