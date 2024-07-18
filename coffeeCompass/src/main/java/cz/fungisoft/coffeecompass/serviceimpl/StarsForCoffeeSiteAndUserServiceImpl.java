/**
 * 
 */
package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.Optional;
import java.util.UUID;

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
import lombok.extern.slf4j.Slf4j;

/**
 * Implementace prislusneho service interfacu. Vyuziva StarsForCoffeeSiteAndUserRepository
 * 
 * @author Michal Vaclavek
 *
 */
@Service("avgStarsService")
@Transactional
@Slf4j
public class StarsForCoffeeSiteAndUserServiceImpl implements IStarsForCoffeeSiteAndUserService {

    private final StarsForCoffeeSiteAndUserRepository avgStarsRepo;
    
    @Autowired
    private StarsQualityService starsQualService;
    
    @Autowired
    private UserService userService;
    
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
//    @Override
//    public double avgStarsForSite(Long coffeeSiteID) {
//
//        double starsAvg = 0;
//
//        try {
//            starsAvg = avgStarsRepo.averageStarsForSiteID(coffeeSiteID);
//            starsAvg = Math.round(starsAvg * 10.0)/10.0; // one decimal place round
//        }
//        catch (Exception e) {
//            log.info("Average stars could not be calculated: {}", e.getMessage() );
//        }
//
//        return starsAvg;
//    }

    @Override
    public double avgStarsForSite(UUID coffeeSiteExtId) {
        double starsAvg = 0;

        try {
            starsAvg = avgStarsRepo.averageStarsForSiteExternalId(coffeeSiteExtId);
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
//    @Override
//    public double avgStarsForUser(Long userID) {
//        return avgStarsRepo.averageStarsForUserID(userID);
//    }

    @Override
    public void saveStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user, int stars) {
        // Ziskat prislusny objekt StarsForCoffeeSiteAndUser z DB
        StarsForCoffeeSiteAndUser sfcsu = avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getExternalId(), user.getId());
        
        if (sfcsu == null) { // Pokud zaznam pro tohoto uzivatele a CoffeeSite jeste v DB neni, vytvori novy
            sfcsu = new StarsForCoffeeSiteAndUser(coffeeSite, user, starsQualService.findStarsQualityById(stars));
        } else {
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
    public StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(String coffeeSiteExtId, Long userId, int stars) {
        
        Optional<User> logedInUser = userService.getCurrentLoggedInUser();
        
        if (logedInUser.isPresent() && logedInUser.get().getId().equals(userId)) {
            // Ziskat prislusny objekt StarsForCoffeeSiteAndUser z DB
            StarsForCoffeeSiteAndUser sfcsu = avgStarsRepo.getOneStarEvalForSiteAndUser(UUID.fromString(coffeeSiteExtId), userId);

            if (sfcsu != null) {
                sfcsu.setStars(starsQualService.findStarsQualityById(stars));
                // Updatovane hodnoceni ulozit do Repositoy
                sfcsu = avgStarsRepo.save(sfcsu);
                log.info("Stars for Coffee site id {} and User id {} updated. Stars: {}", coffeeSiteExtId, userId, stars);
                return sfcsu;
            }
        }
        log.error("Updating Stars for Coffee site id {} and User id {} failed. Stars: {}", coffeeSiteExtId, userId, stars);
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
                StarsForCoffeeSiteAndUser sfcsu = avgStarsRepo.getOneStarEvalForSiteAndUser(comment.getCoffeeSiteId(), comment.getUserId());
                
                if (sfcsu != null) { // Pokud zaznam pro tohoto uzivatele a CoffeeSite jeste v DB neni, vytvori novy
                    sfcsu.setStars(starsQualService.findStarsQualityById(stars));
                    // Updatovane hodnoceni ulozit do Repositoy
                    sfcsu = avgStarsRepo.save(sfcsu);
                    log.info("Stars for Coffee site id {} and User id {} updated. Stars: {}", comment.getCoffeeSiteId(), comment.getUserId(), stars);
                    log.info("Stars updated for CoffeeSite id {}, from User id {}.", comment.getCoffeeSiteId(), comment.getUserId());
                    return sfcsu;
                }
            }
            
        }
        
        if (comment != null) {
            log.error("Updating Stars for Coffee site id {} and User id {} failed.", comment.getCoffeeSiteId(), comment.getUserId());
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
//    @Override
//    public void saveStarsForCoffeeSiteAndLoggedInUser(Long coffeeSiteID, Integer stars) {
//        coffeeSiteService.findOneById(coffeeSiteID).ifPresent(cs -> {
//            Optional<User> logedInUser = userService.getCurrentLoggedInUser();
//            logedInUser.ifPresent(user -> saveStarsForCoffeeSiteAndUser(cs, user, stars));
//        });
//    }

    public void saveStarsForCoffeeSiteAndLoggedInUser(String coffeeSiteExtId, Integer stars) {
        coffeeSiteService.findOneByExternalId(coffeeSiteExtId).ifPresent(cs -> {
            Optional<User> logedInUser = userService.getCurrentLoggedInUser();
            logedInUser.ifPresent(user -> saveStarsForCoffeeSiteAndUser(cs, user, stars));
        });
    }


    @Override
    public String getStarsForCoffeeSiteAndUser(CoffeeSiteDTO coffeeSite, User user) {
        return (user != null && coffeeSite != null) 
                ? avgStarsRepo.getOneStarEvalForSiteAndUser(UUID.fromString(coffeeSite.getExternalId()), user.getId()).getStars().getQuality()
                : "";
    }
    
//    @Override
//    public Integer getStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user) {
//        return (user != null && coffeeSite != null)
//               ? getStarsForCoffeeSiteAndUser(coffeeSite.getId(), user.getId())
//               : 0;
//    }
    
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
    public StarsQualityDescription getStarsForCoffeeSiteAndLoggedInUser(CoffeeSite coffeeSite) {
        
        Optional<User> logedInUser = userService.getCurrentLoggedInUser();
        StarsForCoffeeSiteAndUser userSiteStars = null;
        if (logedInUser.isPresent()) {
            log.info("Retrieving Stars for Coffee site name {} and User name {}", coffeeSite.getSiteName(), logedInUser.get().getUserName());
            userSiteStars = avgStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getExternalId(), logedInUser.get().getId());
        }
        else
            log.info("Stars for Coffee site name {} cannot be retrieved, no user logged-in.", coffeeSite.getSiteName());
        return (userSiteStars == null) ? null :  userSiteStars.getStars();
    }

    @Override
    public AverageStarsForSiteDTO getStarsAndNumOfHodnoceniForSite(UUID coffeeSiteExtId) {
        AverageStarsForSiteDTO starsDto = new AverageStarsForSiteDTO();
        
        int numOfHodnoceni = avgStarsRepo.getNumOfHodnoceniForSite(coffeeSiteExtId);
        if (numOfHodnoceni > 0) {
            double stars = avgStarsForSite(coffeeSiteExtId);
            starsDto.setAvgStars(stars);
            starsDto.setNumOfHodnoceni(numOfHodnoceni);
        }
        log.info("Average Stars for Coffee site ext-id {} retrieved.", coffeeSiteExtId);
        return starsDto;
    }
}
