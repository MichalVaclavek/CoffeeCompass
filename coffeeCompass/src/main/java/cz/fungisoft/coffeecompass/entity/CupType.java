package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
@RequiredArgsConstructor
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="typ_kelimku", schema="coffeecompass")
public class CupType {

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
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @NotNull
    @Column(name="typ_kelimku", length=45, unique=true, nullable=false)
    private String cupType = CupTypeEnum.PLASTIC.getCupType();       

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        
        CupType other = (CupType) obj;
        if (cupType == null)
        {
            if (other.cupType != null)
                return false;
        } else if (!cupType.equals(other.cupType))
            return false;
        
        return Objects.equals(id, other.id);
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cupType == null) ? 0 : cupType.hashCode());
        result = prime * result + id;
        return result;
    }
    
    @Override
    public String toString() {
        return cupType;
    }
}
