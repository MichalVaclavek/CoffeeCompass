package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;

/**
 * Interface s metodami pro cteni z DB repository pro objekty typu StarsForCoffeeSiteAndUser.
 * 
 * @author Michal Vaclavek
 */
public interface StarsForCoffeeSiteAndUserRepository extends JpaRepository<StarsForCoffeeSiteAndUser, Integer>
{
    @Query("select stcsu from StarsForCoffeeSiteAndUser stcsu where coffeeSite.id=?1 and user.id=?2")
    public StarsForCoffeeSiteAndUser getOneStarEvalForSiteAndUser(Integer coffeeSiteID, Integer userID);
    
    @Query("select avg(stars.numOfStars) from StarsForCoffeeSiteAndUser stcsu where coffeeSite.id=?1")
    public double averageStarsForSiteID(Integer coffeeSiteID);
    
    @Query("select count(*) from StarsForCoffeeSiteAndUser stcsu where coffeeSite.id=?1")
    public int getNumOfHodnoceniForSite(Integer coffeeSiteID);
    
    /**
     * Gets average stars evaluation for one USer for all CoffeeSites the user evaluated.
     * @param userID
     * @return
     */
    @Query("select avg(stars.numOfStars) from StarsForCoffeeSiteAndUser stcsu where user.id=?1")
    public double averageStarsForUserID(Integer userID);
    
    /**
     * Deletes stars evaluation of one User for one CoffeeSite
     */
    @Query("delete StarsForCoffeeSiteAndUser where coffeeSite.id=?1 and user.id=?1")
    public void deleteStarsForSiteAndUser(Integer coffeeSiteID, Integer userID);
}
