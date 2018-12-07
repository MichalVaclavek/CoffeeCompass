/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * Slovni popis kvality kavy, automatu, podniku ...
 *
 * @author Michal Vaclavek
 *
 */
@Data
@Entity
@Table(name="stars_hodnoceni_kvality", schema="coffeecompass")
public class StarsQualityDescription
{
  
    public enum StarsQualityEnum implements Serializable
    {
        ONE("Břečka"),
        TWO("Slabota"),
        THREE("Průměr"),
        FOUR("Dobrá"),
        FIVE("Vynikající");
         
        String starsQuality;
         
        private StarsQualityEnum(String starsQuality) {
            this.starsQuality = starsQuality;
        }
         
        public String getStarsQuality() {
            return starsQuality;
        }
    }
    
    @Id
    @NotNull
    @Column(name = "pocet_hvezdicek")
    private Integer numOfStars;  
    
    @NotNull // Validace vstupu, nesmi byt null
    @Column(name = "slovni_vyjadreni_hvezdicek", unique=true, length=45)
    private String quality = StarsQualityEnum.THREE.getStarsQuality(); // Default value needed, atribute must not be null
    
    public void setQuality(StarsQualityEnum qual) {
        quality = qual.starsQuality;
        
        switch (qual)
        {
            case ONE:
                numOfStars = 1;
                break;
                
            case TWO:
                numOfStars = 2;
                break;
                
            case THREE:
                numOfStars = 3;
                break;
                
            case FOUR:
                numOfStars = 4;
                break;
                
            case FIVE:
                numOfStars = 5;
                break;    
    
            default:
                break;
        }
    }
    
    @Override
    public String toString() {
        return quality;
    }

}
