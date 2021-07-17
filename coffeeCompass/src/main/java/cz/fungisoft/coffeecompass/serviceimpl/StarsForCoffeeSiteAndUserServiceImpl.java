/**
 * 
 */
package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.dto.AverageStarsForSiteDTO;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.repository.StarsForCoffeeSiteAndUserRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;

/**
 * Implementace prislusneho service interfacu. Vyuziva StarsForCoffeeSiteAndUserRepository
 * 
 * @author Michal Vaclavek
 *
 */
@Service("avgStarsService")
@Transactional
@Log4j2
public class StarsForCoffeeSiteAndUserServiceImpl implements IStarsForCoffeeSiteAndUserService {

    private StarsForCoffeeSiteAndUserRepository avgStarsRepo;
    
    @Autowired
    private StarsQualityService starsQualService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MapperFacade mapperFacade;
    
    @Autowired
    private CoffeeSiteService coffeeSiteService;
    
   /**
    * Constructor, basic.
    * 
    * @param avgStarsRepo
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
    public double avgStarsForUser(Long userID) {
        return avgStarsRepo.averageStarsForUserID(userID);
    }

    @Override
    public void saveStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user, int stars) {
        // Ziskat prislusny objekt StarsForCoffeeSiteAndUser z DB
        StarsForCoffeeSiteAndUser sfcsu = avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), user.getId());
        
        if (sfcsu == null) { // Pokud zaznam pro tohoto uzivatele a CoffeeSite jeste v DB neni, vytvori novy
            sfcsu = new StarsForCoffeeSiteAndUser(coffeeSite, user, starsQualService.findStarsQualityById(stars));
        }
        else {
            sfcsu.setStars(starsQualService.findStarsQualityById(stars));
        }
        
        // Nove/updatovane hodnoceni ulozit do Repositoy
        avgStarsRepo.save(sfcsu);
        log.info("Stars for Coffee site name {} and User name {} saved. Stars: {}", coffeeSite.getSiteName(), user.getUserName(), stars );
    }
    
    /**
     * Updates Stars for given CoffeeSite and User. Only Stars of the loggedIn user can be updated.
     *  
     * @return 
     */
    @Override
    public StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(Long coffeeSiteID, Long userId, int stars) {
        
        Optional<User> logedInUser = userService.getCurrentLoggedInUser();
        
        if (logedInUser.isPresent() && logedInUser.get().getId().equals(userId)) {
            // Ziskat prislusny objekt StarsForCoffeeSiteAndUser z DB
            StarsForCoffeeSiteAndUser sfcsu = avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSiteID, userId);
            
            if (sfcsu != null) { // Pokud zaznam pro tohoto uzivatele a CoffeeSite jeste v DB neni, vytvori novy
                sfcsu.setStars(starsQualService.findStarsQualityById(stars));
                // Updatovane hodnoceni ulozit do Repositoy
                sfcsu = avgStarsRepo.save(sfcsu);
                log.info("Stars for Coffee site id {} and User id {} updated. Stars: {}", coffeeSiteID, userId, stars);
                return sfcsu;
            }
        }
        log.error("Updating Stars for Coffee site id {} and User id {} failed. Stars: {}", coffeeSiteID, userId, stars);
        return null;
    }
    
    /**
     * Updates Stars for given CoffeeSite and User. Only Stars of the loggedIn user can be updated.
     *  
     * @return 
     */
    @Override
    public StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(CommentDTO comment) {
        // Ulozit updated Stars if not 0
        if (comment != null && comment.getStarsFromUser() >= (StarsQualityDescription.StarsQualityEnum.ONE.ordinal() + 1)
             && comment.getStarsFromUser() <= (StarsQualityDescription.StarsQualityEnum.FIVE.ordinal() + 1)
             && comment.getUserId() > 0) {
            
            int stars = comment.getStarsFromUser();
            Optional<User> logedInUser = userService.getCurrentLoggedInUser();
            
            if (logedInUser.isPresent() && logedInUser.get().getId() == comment.getUserId()) {
                // Ziskat prislusny objekt StarsForCoffeeSiteAndUser z DB
                StarsForCoffeeSiteAndUser sfcsu = avgStarsRepo.getOneStarEvalForSiteAndUser(comment.getCoffeeSiteID(), comment.getUserId());
                
                if (sfcsu != null) { // Pokud zaznam pro tohoto uzivatele a CoffeeSite jeste v DB neni, vytvori novy
                    sfcsu.setStars(starsQualService.findStarsQualityById(stars));
                    // Updatovane hodnoceni ulozit do Repositoy
                    sfcsu = avgStarsRepo.save(sfcsu);
                    log.info("Stars for Coffee site id {} and User id {} updated. Stars: {}", comment.getCoffeeSiteID(), comment.getUserId(), stars);
                    log.info("Stars updated for CoffeeSite id {}, from User id {}.", comment.getCoffeeSiteID(), comment.getUserId());
                    return sfcsu;
                }
            }
            
        }
        
        if (comment != null) {
            log.error("Updating Stars for Coffee site id {} and User id {} failed.", comment.getCoffeeSiteID(), comment.getUserId());
        }
        return null;
    }

    @Override
    public void cancelStarsForCoffeeSite(CoffeeSite coffeeSite, User user) {
        avgStarsRepo.deleteStarsForSiteAndUser(coffeeSite.getId(), user.getId());
        log.info("Stars for Coffee site name {} and User name {} canceled.", coffeeSite.getSiteName(), user.getUserName() );
    }

    /**
     * Ulozit/updatovat hodnoceni pro dany CoffeeSite pro aktualne prihlaseneho uzivatele.
     * Only logged-in user can save the stars.
     */
    @Override
    public void saveStarsForCoffeeSiteAndLoggedInUser(Long coffeeSiteID, Integer stars) {
        CoffeeSite cs = coffeeSiteService.findOneById(coffeeSiteID);
        Optional<User> logedInUser = userService.getCurrentLoggedInUser();
        
        if (logedInUser.isPresent()) {
            saveStarsForCoffeeSiteAndUser(cs, mapperFacade.map(logedInUser.get(), User.class), stars);
        }
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
               ? getStarsForCoffeeSiteAndUser(coffeeSite.getId(), user.getId())
               : 0;
    }
    
    /**
     * Returns number of stars for CoffeeSiteId and userID already entered by user.
     * If no star were already save, returns 0
     */
    @Override
    public Integer getStarsForCoffeeSiteAndUser(Long coffeeSiteId, Long userId) {
        if (coffeeSiteId != null && userId != null) {
            StarsForCoffeeSiteAndUser stars = avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSiteId, userId);
            if (stars != null) {
                return stars.getStars().getNumOfStars();
            }
        }
        // in all other cases return 0
        return 0;
        
    }

    @Override
    public String getStarsStringForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite) {
        Optional<User> logedInUser = userService.getCurrentLoggedInUser();
        return getStarsForCoffeeSiteAndUser(coffeeSite, logedInUser.get());
    }

    @Override
    public StarsQualityDescription getStarsForCoffeeSiteAndLoggedInUser(CoffeeSiteDTO coffeeSite) {
        
        Optional<User> logedInUser = userService.getCurrentLoggedInUser();
        StarsForCoffeeSiteAndUser userSiteStars = null;
        if (logedInUser.isPresent()) {
            log.info("Retrieving Stars for Coffee site name {} and User name {}", coffeeSite.getSiteName(), logedInUser.get().getUserName());
            userSiteStars = avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), logedInUser.get().getId());
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
