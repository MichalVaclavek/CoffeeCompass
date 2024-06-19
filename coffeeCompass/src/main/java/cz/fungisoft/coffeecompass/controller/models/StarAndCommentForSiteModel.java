package cz.fungisoft.coffeecompass.controller.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
    @Max(5)
    private int stars;

    @Size(max=320)
    private String comment;
    
    @NotNull
    private String coffeeSiteExtId;
    
    
    public StarAndCommentForSiteModel() {
        stars = 3;
        comment = "";
    }
}
