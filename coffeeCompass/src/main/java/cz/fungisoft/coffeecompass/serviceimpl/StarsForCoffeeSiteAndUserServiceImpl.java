/**
 * 
 */
package cz.fungisoft.coffeecompass.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.dto.AverageStarsForSiteDTO;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.repository.StarsForCoffeeSiteAndUserRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
import cz.fungisoft.coffeecompass.service.UserSecurityService;
import cz.fungisoft.coffeecompass.service.UserService;
import lombok.extern.log4j.Log4j2;

/**
 * Implementace prislusneho service interfacu. Vyuziva StarsForCoffeeSiteAndUserRepository
 * 
 * @author Michal Vaclavek
 *
 */
@Service("avgStarsService")
@Transactional
@Log4j2
public class StarsForCoffeeSiteAndUserServiceImpl implements IStarsForCoffeeSiteAndUserService
{
    private StarsForCoffeeSiteAndUserRepository avgStarsRepo;
    
    @Autowired
    private StarsQualityService starsQualService;
    
    @Autowired
    private UserService userService;
    
//    @Autowired
//    private UserSecurityService userSecurityService;
    
    @Autowired
    private CoffeeSiteService coffeeSiteService;
    
   /**
    * Constructor, basic.
    * 
    * @param starsQaulityRepo
    * @param avgStarsRepo
    */
    @Autowired
    public StarsForCoffeeSiteAndUserServiceImpl(StarsForCoffeeSiteAndUserRepository avgStarsRepo) {
        super();
        this.avgStarsRepo = avgStarsRepo;
    }
    
    /* (non-Javadoc)
     * @see cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService#avgStarsForSite(java.lang.Integer)
     */
    @Override
    public double avgStarsForSite(Long coffeeSiteID) {
        
        double starsAvg = 0;
        
        try {
            starsAvg = avgStarsRepo.averageStarsForSiteID(coffeeSiteID);
            starsAvg = Math.round(starsAvg * 10.0)/10.0; // one decimal place round
        }
        catch (Exception e) {
            log.info("Average stars could not be calculated: {}", e.getMessage() );
        }
        
        return starsAvg;
    }

    /* (non-Javadoc)
     * @see cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService#avgStarsForUser(java.lang.Integer)
     */
    @Override
    public double avgStarsForUser(Integer userID) {
        return avgStarsRepo.averageStarsForUserID(userID);
    }

    @Override
    public void saveStarsForCoffeeSite(CoffeeSite coffeeSite, User user, int stars) {
        // Ziskat prislusny objekt StarsForCoffeeSiteAndUser z DB
        StarsForCoffeeSiteAndUser sfcsu = avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), user.getId());
        
        if (sfcsu == null) { // Pokud zaznam pro tohoto uzivatele a CoffeeSite jeste v DB neni, vytvori novy
            sfcsu = new StarsForCoffeeSiteAndUser(coffeeSite, user, starsQualService.findStarsQualityById(stars));
        }
        else
            sfcsu.setStars(starsQualService.findStarsQualityById(stars));
        
        // Nove/updatovane hodnoceni ulozit do Repositoy
        avgStarsRepo.save(sfcsu);
        log.info("Average stars for Coffee site name {} and User name {} saved. Stars: {}", coffeeSite.getSiteName(), user.getUserName(), stars );
    }

    @Override
    public void cancelStarsForCoffeeSite(CoffeeSite coffeeSite, User user) {
        avgStarsRepo.deleteStarsForSiteAndUser(coffeeSite.getId(), user.getId());
        log.info("Average stars for Coffee site name {} and User name {} canceled.", coffeeSite.getSiteName(), user.getUserName() );
    }

    /**
     * Ulozit hodnoceni pro dany CoffeeSite pro aktualne prihlaseneho uzivatele.
     * Only logged-in user can save the stars.
     */
    @Override
    public void saveStarsForCoffeeSite(Long coffeeSiteID, Integer stars) {
        CoffeeSite cs = coffeeSiteService.findOneById(coffeeSiteID);
        
        User logedInUser = userService.getCurrentLoggedInUser();
        
        if (logedInUser != null)
            saveStarsForCoffeeSite(cs, logedInUser, stars);
    }

    @Override
    public String getStarsForCoffeeSiteAndUser(CoffeeSiteDTO coffeeSite, User user) {
        return (user != null && coffeeSite != null) 
            ? avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), user.getId()).getStars().getQuality()
            : "";
    }
    
    @Override
    public Integer getStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user) {
        return (user != null && coffeeSite != null) 
            ? avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), user.getId()).getStars().getNumOfStars()
            : 0;
    }

    @Override
    public String getStarsStringForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite) {
        User logedInUser = userService.getCurrentLoggedInUser();
        return getStarsForCoffeeSiteAndUser(coffeeSite, logedInUser);
    }

    @Override
    public StarsQualityDescription getStarsForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite) {
        
        User logedInUser = userService.getCurrentLoggedInUser();
        StarsForCoffeeSiteAndUser userSiteStars = null;
        if (logedInUser != null) {
            log.info("Retrieving Stars for Coffee site name {} and User name {}", coffeeSite.getSiteName(), logedInUser.getUserName());
            userSiteStars = avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), logedInUser.getId());
        }
        else
            log.info("Stars for Coffee site name {} cannot be retrieved, no user logged-in.", coffeeSite.getSiteName());
        return (userSiteStars == null) ? null :  userSiteStars.getStars();
    }

    @Override
    public AverageStarsForSiteDTO getStarsAndNumOfHodnoceniForSite(Long coffeeSiteID) {
        AverageStarsForSiteDTO starsDto = new AverageStarsForSiteDTO();
        
        int numOfHodnoceni = avgStarsRepo.getNumOfHodnoceniForSite(coffeeSiteID);
        if (numOfHodnoceni > 0) {
            double stars = avgStarsForSite(coffeeSiteID);
            starsDto.setAvgStars(stars);
            starsDto.setNumOfHodnoceni(numOfHodnoceni);
        }
        log.info("Average Stars for Coffee site id {} retrieved.", coffeeSiteID);
        return starsDto;
    }

}
