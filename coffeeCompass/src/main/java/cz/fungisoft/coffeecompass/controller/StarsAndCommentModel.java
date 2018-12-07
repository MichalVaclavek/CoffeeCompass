package cz.fungisoft.coffeecompass.controller;

import javax.validation.constraints.Size;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription.StarsQualityEnum;
import lombok.Data;

/**
 * Trida pro model, kterym se uchovava/prenasi inforamce o hodnoceni a komentari ke CoffeeSitu.
 * 
 * @author Michal Vaclavek
 */
@Data
public class StarsAndCommentModel
{
    private StarsQualityDescription stars;

    @Size(max=240)
    private String comment;
    
    public StarsAndCommentModel() {
        stars = new StarsQualityDescription(); 
        stars.setQuality(StarsQualityEnum.THREE);
    }
    
    public void clear() {
        stars.setQuality(StarsQualityEnum.THREE);
        comment="";
    }
}
