package cz.fungisoft.coffeecompass.service;

import cz.fungisoft.coffeecompass.dto.AverageStarsForSiteDTO;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
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
public interface IStarsForCoffeeSiteAndUserService
{
    /**
     * Hodnoceni jednoho uzivatele pro jeden CoffeeSite.
     * 
     * @param coffeeSite
     * @param user
     * @return
     */
    public Integer getStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user);
    public Integer getStarsForCoffeeSiteAndUser(Long coffeeSiteId, Long userId);
    
    public String getStarsForCoffeeSiteAndUser(CoffeeSiteDTO coffeeSite, User user);
    public String getStarsStringForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite);
    public StarsQualityDescription getStarsForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite);
    
    public void saveStarsForCoffeeSiteAndLoggedInUser(Long coffeeSiteID, Integer stars);
    public void saveStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user, int stars);
    public StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(Long coffeeSiteID, Long userId, int stars);
    
    public void cancelStarsForCoffeeSite(CoffeeSite coffeeSite, User user);
    
    public double avgStarsForSite(Long coffeeSiteID);
    public double avgStarsForUser(Long userID);
    
    public AverageStarsForSiteDTO getStarsAndNumOfHodnoceniForSite(Long coffeeSiteID);
}
