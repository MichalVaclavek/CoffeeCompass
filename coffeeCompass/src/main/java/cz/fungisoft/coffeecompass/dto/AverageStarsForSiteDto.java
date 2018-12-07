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
 * @author Michal
 *
 */
@Data
public class AverageStarsForSiteDto {

    private Double avgStars = 0D;
    private Integer numOfHodnoceni = 0;
    
    public AverageStarsForSiteDto() {}
    
    public AverageStarsForSiteDto(Double stars, Integer numOfHod) {
        this.avgStars = stars;
        this.numOfHodnoceni = numOfHod;
    }
}
