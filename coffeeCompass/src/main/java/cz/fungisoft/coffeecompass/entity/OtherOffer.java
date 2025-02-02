/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Dalsi nabidka, krome kafe ... caj, kafe s necim, polivka atp.
 * 
 * @author Michal Vaclavek
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Entity
@Table(name="nabidka", schema="coffeecompass")
public class OtherOffer extends BaseEntity {

    @NotNull // Validace vstupu, nesmi byt null
    @Size(min = 3, max = 45) // Validace vstupu, pocet znaku
    @Column(name = "nabidka", unique=true)
    private String offer;

    @Override
    public String toString() {
        return offer;
    }
}
