package cz.fungisoft.coffeecompass.service;

import cz.fungisoft.coffeecompass.dto.AverageStarsForSiteDTO;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Serisni vrstva pro ziskavani udaju u prumernem hodnoceni urciteho CoffeeSite nebo<br>
 * o prumernem hodnoceni urciteho uzivatele.<br>
 * Vyuziva StarsForCoffeeSiteAndUserRepository
 * @author Michal Vaclavek
 *
 */
public interface IStarsForCoffeeSiteAndUserService {

    Integer getStarsForCoffeeSiteAndUser(String coffeeSiteExtId, String userExtId);

    Integer getStarsForCoffeeSiteAndUser(UUID coffeeSiteExtId, UUID userExtId);
    
    StarsQualityDescription getStarsForCoffeeSiteAndLoggedInUser(CoffeeSite coffeeSite);
    
    void saveStarsForCoffeeSiteAndLoggedInUser(String coffeeSiteExtId, Integer stars);
    void saveStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user, int stars);

    StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(String coffeeSiteExtId, String userExtId, int stars);
    StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(UUID coffeeSiteExtId, UUID userExtId, int stars);
    
    /**
     * Updates stars rating for CoffeeSite included in CommentsDTO object
     *  
     * @param comment
     * @return
     */
    StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(CommentDTO comment);
    
//    void cancelStarsForCoffeeSite(CoffeeSite coffeeSite, User user);
    
//    double avgStarsForSite(UUID coffeeSiteExtId);

//    AverageStarsForSiteDTO getStarsAndNumOfHodnoceniForSite(UUID coffeeSiteExtId);
}
