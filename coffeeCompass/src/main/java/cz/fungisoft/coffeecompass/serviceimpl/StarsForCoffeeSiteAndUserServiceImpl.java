/**
 * 
 */
package cz.fungisoft.coffeecompass.serviceimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.dto.AverageStarsForSiteDto;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDto;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.repository.StarsForCoffeeSiteAndUserRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
import cz.fungisoft.coffeecompass.service.UserService;

/**
 * Implementace prislusneho service interfacu. Vyuziva StarsForCoffeeSiteAndUserRepository
 * 
 * @author Michal Vaclavek
 *
 */
@Service("avgStarsService")
@Transactional
public class StarsForCoffeeSiteAndUserServiceImpl implements IStarsForCoffeeSiteAndUserService
{
    private static final Logger logger = LoggerFactory.getLogger(StarsForCoffeeSiteAndUserServiceImpl.class); 
    
    private StarsForCoffeeSiteAndUserRepository avgStarsRepo;
    
    @Autowired
    private StarsQualityService starsQualService;
    
    @Autowired
    private UserService userService;
    
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
            logger.info("Average stars could not be calculated: {}", e.getMessage() );
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
    }

    @Override
    public void cancelStarsForCoffeeSite(CoffeeSite coffeeSite, User user) {
        avgStarsRepo.deleteStarsForSiteAndUser(coffeeSite.getId(), user.getId());
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
    public int getStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getStarsStringForCoffeeSiteAndLoggedInUser(CoffeeSiteDto coffeeSite) {
        User logedInUser = userService.getCurrentLoggedInUser();
        
        if (logedInUser != null)
            return avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), logedInUser.getId()).getStars().getQuality();
        else 
            return "";
    }

    @Override
    public StarsQualityDescription getStarsForCoffeeSiteAndLoggedInUser(CoffeeSiteDto coffeeSite) {
        
        User logedInUser = userService.getCurrentLoggedInUser();
        
        // Already saved stars for this CoffeeSite from logged-in user
        if (logedInUser != null) {
            StarsForCoffeeSiteAndUser userSiteStars = avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), logedInUser.getId());
            return (userSiteStars == null) ? null :  userSiteStars.getStars();
        }
        else
            return null;
    }

    @Override
    public AverageStarsForSiteDto getStarsAndNumOfHodnoceniForSite(Long coffeeSiteID) {
        AverageStarsForSiteDto starsDto = new AverageStarsForSiteDto();
        
        int numOfHodnoceni = avgStarsRepo.getNumOfHodnoceniForSite(coffeeSiteID);
        if (numOfHodnoceni > 0) {
            double stars = avgStarsForSite(coffeeSiteID);
            starsDto.setAvgStars(stars);
            starsDto.setNumOfHodnoceni(numOfHodnoceni);
        }
        return starsDto;
    }
    
}
