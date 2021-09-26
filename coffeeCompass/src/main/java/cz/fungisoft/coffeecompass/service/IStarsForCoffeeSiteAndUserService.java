package cz.fungisoft.coffeecompass.service;

import cz.fungisoft.coffeecompass.dto.AverageStarsForSiteDTO;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.User;

/**
 * Serisni vrstva pro ziskavani udaju u prumernem hodnoceni urciteho CoffeeSite nebo<br>
 * o prumernem hodnoceni urciteho uzivatele.<br>
 * Vyuziva StarsForCoffeeSiteAndUserRepository
 * @author Michal Vaclavek
 *
 */
public interface IStarsForCoffeeSiteAndUserService {

    /**
     * Hodnoceni jednoho uzivatele pro jeden CoffeeSite.
     * 
     * @param coffeeSite
     * @param user
     * @return
     */
    Integer getStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user);
    Integer getStarsForCoffeeSiteAndUser(Long coffeeSiteId, Long userId);
    
    String getStarsForCoffeeSiteAndUser(CoffeeSiteDTO coffeeSite, User user);
    String getStarsStringForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite);
    StarsQualityDescription getStarsForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite);
    
    void saveStarsForCoffeeSiteAndLoggedInUser(Long coffeeSiteID, Integer stars);
    void saveStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user, int stars);
    StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(Long coffeeSiteID, Long userId, int stars);
    
    /**
     * Updates stars rating for CoffeeSite included in CommentsDTO object
     *  
     * @param comment
     * @return
     */
    StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(CommentDTO comment);
    
    void cancelStarsForCoffeeSite(CoffeeSite coffeeSite, User user);
    
    double avgStarsForSite(Long coffeeSiteID);
    double avgStarsForUser(Long userID);
    
    AverageStarsForSiteDTO getStarsAndNumOfHodnoceniForSite(Long coffeeSiteID);
}
