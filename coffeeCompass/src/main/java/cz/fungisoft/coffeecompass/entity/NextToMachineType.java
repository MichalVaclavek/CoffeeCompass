/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * Druh automatu, ktere stoji vedle kavoveho automatu.
 * 
 * @author Michal Vaclavek
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Entity
@Table(name="dalsi_automat_vedle_type", schema="coffeecompass")
public class NextToMachineType extends BaseEntity {

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
    
    @NotNull
    @Column(name="druh_automatu", length=45, unique=true, nullable=false)
    private String type = NextToMachineTypeEnum.NAPOJE.getNexToMachineType();

    @Override
    public String toString() {
        return type;
    }
}
