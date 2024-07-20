/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;

/**
 * Druh automatu, ktere stoji vedle kavoveho automatu.
 * 
 * @author Michal Vaclavek
 */
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name="dalsi_automat_vedle_type", schema="coffeecompass")
public class NextToMachineType {

    /* INNER STATIC CLASS */
    public enum NextToMachineTypeEnum implements Serializable {
        NAPOJE("Nápoje"),
        SLADKOSTI("Sladkosti"),
        BAGETY("Bagety"),
        OSTATNI("Další");
         
        String nextToMachineType;
         
        NextToMachineTypeEnum(String nextToMachineType) {
            this.nextToMachineType = nextToMachineType;
        }
         
        public String getNexToMachineType() {
            return nextToMachineType;
        }
    }
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @NotNull
    @Column(name="druh_automatu", length=45, unique=true, nullable=false)
    private String type = NextToMachineTypeEnum.NAPOJE.getNexToMachineType();

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        
        NextToMachineType other = (NextToMachineType) obj;
        if (id != other.id)
            return false;
        
        if (type == null)
        {
            return other.type == null;
        } else return type.equals(other.type);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id; // id ve vypoctu hashCode() ovlivnuje vyhodnoceni assertThat().equals pri testech
        // repository. Prestoze id i type jsou shodne, tak pokud je jich vice v Set<NextToMachineType>
        // AssertThat. .. equal() vyhodnoti 2 takove Sety jako false
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return type;
    }
}
