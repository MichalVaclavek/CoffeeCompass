package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

/**
 * Interface s metodami pro cteni z DB repository pro objekty typu StarsForCoffeeSiteAndUser.
 * 
 * @author Michal Vaclavek
 */
public interface StarsForCoffeeSiteAndUserRepository extends JpaRepository<StarsForCoffeeSiteAndUser, Integer> {

    @Query("select stcsu from StarsForCoffeeSiteAndUser stcsu where coffeeSite.id=?1 and user.id=?2")
    StarsForCoffeeSiteAndUser getOneStarEvalForSiteAndUser(Long coffeeSiteID, Long userID);

    @Query("select avg(stars.numOfStars) from StarsForCoffeeSiteAndUser stcsu where coffeeSite.id=?1")
    double averageStarsForSiteID(Long coffeeSiteID);

    @Query("select count(*) from StarsForCoffeeSiteAndUser stcsu where coffeeSite.id=?1")
    int getNumOfHodnoceniForSite(Long coffeeSiteID);
    
    /**
     * Gets average stars evaluation for one USer for all CoffeeSites the user evaluated.
     * @param userID
     * @return
     */
    @Query("select avg(stars.numOfStars) from StarsForCoffeeSiteAndUser stcsu where user.id=?1")
    double averageStarsForUserID(Long userID);
    
    /**
     * Deletes stars evaluation of one User for one CoffeeSite
     */
    @Query("delete StarsForCoffeeSiteAndUser where coffeeSite.id=?1 and user.id=?1")
    void deleteStarsForSiteAndUser(Long coffeeSiteID, Long userID);
}
