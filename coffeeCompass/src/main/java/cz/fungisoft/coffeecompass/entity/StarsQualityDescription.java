/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Slovni a čiselný popis kvality kavy, automatu, podniku ...
 * Stupnice 1 až 5 a příslušné slovní označení.
 *
 * @author Michal Vaclavek
 */
@Data
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="stars_hodnoceni_kvality", schema="coffeecompass")
public class StarsQualityDescription {

    public enum StarsQualityEnum implements Serializable {
        ONE("Břečka"),
        TWO("Slabota"),
        THREE("Průměr"),
        FOUR("Dobrá"),
        FIVE("Vynikající");
         
        private final String starsQuality;
         
        StarsQualityEnum(String starsQuality) {
            this.starsQuality = starsQuality;
        }
         
        public String getStarsQuality() {
            return starsQuality;
        }
        
        public static StarsQualityEnum fromString(String text) {
            for (StarsQualityEnum b : StarsQualityEnum.values()) {
                if (b.starsQuality.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found.");
        }
    }
    
    /**
     * Basic constructor for this class.
     * Sets default values for {@code numOfStars} and for {@code quality} properties.
     */
    public StarsQualityDescription() {
        setNumOfStars(3);
    }
    
    
    @Id
    @NotNull
    @Column(name = "pocet_hvezdicek")
    private Integer numOfStars; 
    
    public void setNumOfStars(Integer numOfStars) {
        this.numOfStars = numOfStars;
        
        switch (numOfStars)
        {
          case 1:
              this.quality = StarsQualityEnum.ONE.getStarsQuality();
              break;
          case 2:
              this.quality = StarsQualityEnum.TWO.getStarsQuality();
              break;
          case 3:
              this.quality = StarsQualityEnum.THREE.getStarsQuality();
              break;
          case 4:
              this.quality = StarsQualityEnum.FOUR.getStarsQuality();
              break;
          case 5:
              this.quality = StarsQualityEnum.FIVE.getStarsQuality();
              break;    
          default:
              break;
        }
    }
    
    @NotNull // Validace vstupu, nesmi byt null
    @Column(name = "slovni_vyjadreni_hvezdicek", unique=true, length=45)
    private String quality = StarsQualityEnum.THREE.getStarsQuality(); // Default value needed, atribute must not be null
    
    public void setQuality(StarsQualityEnum qual) {
        this.quality = qual.starsQuality;
        
        switch (qual) {
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
    
    public void setQuality(String quality) {
        this.quality = quality;
        
        switch (StarsQualityEnum.fromString(quality)) {
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
