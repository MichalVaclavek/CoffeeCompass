/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.util.Objects;

/**
 * Slovni a čiselný popis kvality kavy, automatu, podniku ...
 * Stupnice 1 až 5 a příslušné slovní označení.
 *
 * @author Michal Vaclavek
 */
@Getter
@Setter
@Entity
@Table(name="stars_hodnoceni_kvality", schema="coffeecompass")
public class StarsQualityDescription extends BaseEntity {

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
    
    
    @NotNull
    @Column(name = "pocet_hvezdicek")
    private Integer numOfStars; 
    
    public void setNumOfStars(Integer numOfStars) {
        this.numOfStars = numOfStars;

        switch (numOfStars) {
            case 1 -> this.quality = StarsQualityEnum.ONE.getStarsQuality();
            case 2 -> this.quality = StarsQualityEnum.TWO.getStarsQuality();
            case 3 -> this.quality = StarsQualityEnum.THREE.getStarsQuality();
            case 4 -> this.quality = StarsQualityEnum.FOUR.getStarsQuality();
            case 5 -> this.quality = StarsQualityEnum.FIVE.getStarsQuality();
            default -> this.quality = StarsQualityEnum.THREE.getStarsQuality();
        }
    }
    
    @NotNull // Validace vstupu, nesmi byt null
    @Column(name = "slovni_vyjadreni_hvezdicek", unique=true, length=45)
    private String quality = StarsQualityEnum.THREE.getStarsQuality(); // Default value needed, atribute must not be null
    
    public void setQuality(StarsQualityEnum qual) {
        this.quality = qual.starsQuality;

        switch (qual) {
            case ONE -> numOfStars = 1;
            case TWO -> numOfStars = 2;
            case THREE -> numOfStars = 3;
            case FOUR -> numOfStars = 4;
            case FIVE -> numOfStars = 5;
        }
    }
    
    public void setQuality(String quality) {
        this.quality = quality;

        switch (StarsQualityEnum.fromString(quality)) {
            case ONE -> numOfStars = 1;
            case TWO -> numOfStars = 2;
            case THREE -> numOfStars = 3;
            case FOUR -> numOfStars = 4;
            case FIVE -> numOfStars = 5;
        }
    }
    
    @Override
    public String toString() {
        return quality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        StarsQualityDescription that = (StarsQualityDescription) o;
        return numOfStars != null && Objects.equals(numOfStars, that.numOfStars);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
