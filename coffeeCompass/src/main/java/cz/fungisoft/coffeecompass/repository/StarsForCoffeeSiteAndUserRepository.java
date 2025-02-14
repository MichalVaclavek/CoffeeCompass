package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface s metodami pro cteni z DB repository pro objekty typu StarsForCoffeeSiteAndUser.
 * 
 * @author Michal Vaclavek
 */
public interface StarsForCoffeeSiteAndUserRepository extends JpaRepository<StarsForCoffeeSiteAndUser, UUID> {

    @Query("select stcsu from StarsForCoffeeSiteAndUser stcsu where coffeeSite.id=?1 and user.id=?2")
    Optional<StarsForCoffeeSiteAndUser> getOneStarEvalForSiteAndUser(UUID coffeeSiteId, UUID userID);

    @Query("select avg(stars.numOfStars) from StarsForCoffeeSiteAndUser stcsu where coffeeSite.id=?1")
    double averageStarsForSiteExternalId(UUID coffeeSiteExtId);

    @Query("select count(*) from StarsForCoffeeSiteAndUser stcsu where coffeeSite.id=?1")
    int getNumOfHodnoceniForSite(UUID coffeeSiteExtId);
    
    /**
     * Gets average stars evaluation for one User for all CoffeeSites the user evaluated.
     * @param userID
     * @return
     */
    @Query("select avg(stars.numOfStars) from StarsForCoffeeSiteAndUser stcsu where user.id=?1")
    double averageStarsForUserID(UUID userID);
    
    /**
     * Deletes stars evaluation of one User for one CoffeeSite
     */
    @Query("delete StarsForCoffeeSiteAndUser where coffeeSite.id=?1 and user.id=?1")
    void deleteStarsForSiteAndUser(UUID coffeeSiteID, UUID userID);
}
