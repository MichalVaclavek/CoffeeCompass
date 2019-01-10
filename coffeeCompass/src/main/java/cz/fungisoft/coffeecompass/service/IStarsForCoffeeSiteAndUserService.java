package cz.fungisoft.coffeecompass.service;

import cz.fungisoft.coffeecompass.dto.AverageStarsForSiteDTO;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.User;

/**
 * Serisni vrstva pro ziskavani udaju u prumernem hodnoceni urciteho CoffeeSite nebo
 * o prumernem hodnoceni urciteho uzivatele.<br>
 * Vyuziva StarsForCoffeeSiteAndUserRepository
 * @author Michal
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
    public String getStarsForCoffeeSiteAndUser(CoffeeSiteDTO coffeeSite, User user);
    public String getStarsStringForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite);
    public StarsQualityDescription getStarsForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite);
    
    public void saveStarsForCoffeeSite(Long coffeeSiteID, Integer stars);
    
    public void saveStarsForCoffeeSite(CoffeeSite coffeeSite, User user, int stars);
    public void cancelStarsForCoffeeSite(CoffeeSite coffeeSite, User user);
    
    public double avgStarsForSite(Long coffeeSiteID);
    public double avgStarsForUser(Integer userID);
    
    public AverageStarsForSiteDTO getStarsAndNumOfHodnoceniForSite(Long coffeeSiteID);
}
