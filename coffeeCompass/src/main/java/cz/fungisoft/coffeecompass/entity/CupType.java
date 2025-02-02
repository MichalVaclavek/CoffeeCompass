package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Typ kelimku
 * 
 * @author Michal Václavek
 *
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Entity
@Table(name="typ_kelimku", schema="coffeecompass")
public class CupType extends BaseEntity {

    public enum CupTypeEnum implements Serializable {
        PLASTIC("plastový"),
        PAPER("papírový"),
        OWN("vlastní");
         
        String cupType;
         
        CupTypeEnum(String cupType) {
            this.cupType = cupType;
        }
         
        public String getCupType() {
            return cupType;
        }         
    }
    
    @NotNull
    @Column(name="typ_kelimku", length=45, unique=true, nullable=false)
    private String cupType = CupTypeEnum.PLASTIC.getCupType();       

    @Override
    public String toString() {
        return cupType;
    }
}
