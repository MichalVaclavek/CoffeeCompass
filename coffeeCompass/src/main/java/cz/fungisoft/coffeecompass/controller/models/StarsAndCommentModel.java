package cz.fungisoft.coffeecompass.controller.models;

import javax.validation.constraints.Size;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import lombok.Data;

/**
 * Trida pro model, kterym se pouzije ve formulari na templatu coffeesite_detail.html a v prislusnem CSStarsRatingAndCommentsControlleru<br>
 * pro honoceni a komentar jednoho CoffeeSitu jednim uzivatelem. Kontroler CSStarsRatingAndCommentsControlleru spravne informace rozdeli <br>
 * na Stars a Comment a preda servisni vrstve k ulozeni obou techto slozek.<br>
 * <p>
 * Je ale nejdrive pouzit CoffeeSiteControllerem jako odpoved na prislusny GET pozadavek (/showSite/{id}), kterym se zobrazi v coffeesite_detail.html<br>
 * formular pro zadani hodnoceni a komentare prihlasenym uzivatelem.
 * 
 * @author Michal Vaclavek
 */
@Data
public class StarsAndCommentModel
{
    private StarsQualityDescription stars;

    @Size(max=320)
    private String comment;
    
    
    public StarsAndCommentModel() {
        stars = new StarsQualityDescription();
        comment = "";
    }
    
}
