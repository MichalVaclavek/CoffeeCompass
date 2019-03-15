package cz.fungisoft.coffeecompass.controller.models;

import javax.validation.constraints.Size;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription.StarsQualityEnum;
import lombok.Data;

/**
 * Trida pro model, kterym se pouzije ve formulari na templatu coffeesite_detail.html a v prislusnem CSStarsRatingAndCommentsControlleru<br>
 * pro honoceni a komentar jednoho CoffeeSitu jednim uzivatelem. Kontroler CSStarsRatingAndCommentsControlleru spravne informace rozdeli <br>
 * na Stars a Comment a preda servisni vrstve k ulozeni obou techto slozek.<br>
 * <br>
 * Je ale nejdrive pouzit CoffeeSiteControllerem jako odpoved na prislusny GET pozadavek (/showSite/{id}), kterym se zobrazi v coffeesite_detail.html<br>
 * formular pro zadani hodnoceni a komentare prihlasenym uzivatelem.
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
        stars.setQuality(StarsQualityEnum.THREE); // default value, prumerna kvalita
    }
    
    //TODO - check if the method is really needed
    /**
     * Clears the model, before nex usage. Is it really needed or the new model 
     * is created every time the evaluation is requested.
     *
     */
    public void clear() {
        stars.setQuality(StarsQualityEnum.THREE); // set back to default
        comment="";
    }
}
