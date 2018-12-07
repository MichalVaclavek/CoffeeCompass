/**
 * 
 */
package cz.fungisoft.coffeecompass.dto;

import lombok.Data;

/**
 * Trida pro prenos prumerneho "Stars" hodnoceni daneho CoffeeSitu, vcetne info o
 * poctu hodnoceni.<br>
 * Prenasi se jako soucast objektu/tridy CoffeeSiteDto
 *  
 * @author Michal Vaclavek
 *
 */
@Data
public class AverageStarsForSiteDto {

    private Double avgStars = 0D;
    private Integer numOfHodnoceni = 0;
    
    /**
     *  Retezcova reprezentace hodnoceni ve tvaru avgStars (numOfHodnoceni) pokud numOfHodnoceni > 0
     */
    private String common;
    
    public String getCommon() {
        return numOfHodnoceni > 0 ? avgStars + " (" + numOfHodnoceni + ")" : avgStars+"";
    }
    
    public AverageStarsForSiteDto() {}
    
    public AverageStarsForSiteDto(Double stars, Integer numOfHod) {
        this.avgStars = stars;
        this.numOfHodnoceni = numOfHod;
    }
}
