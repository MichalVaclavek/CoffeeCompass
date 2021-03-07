package cz.fungisoft.coffeecompass.controller.models;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * Upravena verze {@link StarsAndCommentModel}, ktera slouzi pro hromadne ulozeni vice komentaru a hodnoceni k vice CoffeeSitum,
 * pro pripad kdy client z duvodu ztraty internetu ukladal komentare a hodnoceni do lokalni DB 
 * a nyni je hce poslat najednou. 
 * 
 * @author Michal Vaclavek
 */
@Data
public class StarAndCommentForSiteModel {
    
    @Min(1)
    @Max(1)
    private int stars;

    @Size(max=320)
    private String comment;
    
    @NotNull
    private long coffeeSiteId;
    
    
    public StarAndCommentForSiteModel() {
        stars = 3;
        comment = "";
    }
}
