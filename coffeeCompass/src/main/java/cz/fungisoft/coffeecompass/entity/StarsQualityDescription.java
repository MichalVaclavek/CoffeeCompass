/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

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
         
        private final String quality;
         
        StarsQualityEnum(String starsQuality) {
            this.quality = starsQuality;
        }
         
        public String getQuality() {
            return quality;
        }
        
        public static StarsQualityEnum fromString(String text) {
            for (StarsQualityEnum b : StarsQualityEnum.values()) {
                if (b.quality.equalsIgnoreCase(text)) {
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
            case 1 -> this.quality = StarsQualityEnum.ONE.getQuality();
            case 2 -> this.quality = StarsQualityEnum.TWO.getQuality();
            case 3 -> this.quality = StarsQualityEnum.THREE.getQuality();
            case 4 -> this.quality = StarsQualityEnum.FOUR.getQuality();
            case 5 -> this.quality = StarsQualityEnum.FIVE.getQuality();
            default -> this.quality = StarsQualityEnum.THREE.getQuality();
        }
    }
    
    @NotNull // Validace vstupu, nesmi byt null
    @Column(name = "slovni_vyjadreni_hvezdicek", unique=true, length=45)
    private String quality = StarsQualityEnum.THREE.getQuality(); // Default value needed, atribute must not be null
    
    public void setQuality(StarsQualityEnum qual) {
        this.quality = qual.quality;

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
