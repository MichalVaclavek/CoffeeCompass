/**
 *
 */
package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.entity.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.dto.AverageStarsForSiteDTO;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
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

    private final StarsForCoffeeSiteAndUserRepository siteStarsRepo;

    private final StarsQualityService starsQualityService;

    private final UserService userService;

    @Lazy
    private final CoffeeSiteService coffeeSiteService;

    /**
     * Constructor, basic.
     *
     * @param siteStarsRepo
     * @param siteStarsRepo
     */
    public StarsForCoffeeSiteAndUserServiceImpl(StarsForCoffeeSiteAndUserRepository siteStarsRepo, StarsQualityService starsQualityService, UserService userService, CoffeeSiteService coffeeSiteService) {
        super();
        this.siteStarsRepo = siteStarsRepo;
        this.starsQualityService = starsQualityService;
        this.userService = userService;
        this.coffeeSiteService = coffeeSiteService;
    }

//    @Override
//    public double avgStarsForSite(UUID coffeeSiteExtId) {
//        double starsAvg = 0;
//
//        try {
//            starsAvg = siteStarsRepo.averageStarsForSiteExternalId(coffeeSiteExtId);
//            starsAvg = Math.round(starsAvg * 10.0) / 10.0; // one decimal place round
//        } catch (Exception e) {
//            log.info("Average stars could not be calculated: {}", e.getMessage());
//        }
//
//        return starsAvg;
//    }

    @Override
    public void saveStarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user, int stars) {
        // Ziskat prislusny objekt StarsForCoffeeSiteAndUser z DB
        var sfcsu = siteStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), user.getId());
        var starsQuality = starsQualityService.findStarsQualityByNumOfStars(stars);

        sfcsu.ifPresent(starsForCoffeeSiteAndUser -> {
            starsForCoffeeSiteAndUser.setStars(starsQuality);
            siteStarsRepo.save(starsForCoffeeSiteAndUser);
        });
        if (sfcsu.isEmpty()) {
            var newStarsForSiteAndUser = new StarsForCoffeeSiteAndUser(coffeeSite, user, starsQuality);
            siteStarsRepo.save(newStarsForSiteAndUser);
        }

        log.info("Stars for Coffee site name {} and User name {} saved. Stars: {}", coffeeSite.getSiteName(), user.getUserName(), stars);
    }

    @Override
    public StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(String coffeeSiteExtId, String userExtId, int stars) {
        return updateStarsForCoffeeSiteAndUser(UUID.fromString(coffeeSiteExtId), UUID.fromString(userExtId), stars);
    }

    @Override
    public StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(UUID coffeeSiteExtId, UUID userExtId, int stars) {
        Optional<User> logedInUser = userService.getCurrentLoggedInUser();
        Optional<CoffeeSite> coffeeSite = coffeeSiteService.findOneByExternalId(coffeeSiteExtId);
        Optional<User> user = userService.findByExtId(userExtId);
        if (logedInUser.isPresent() && coffeeSite.isPresent() && user.isPresent()
                && logedInUser.get().getId().equals(user.get().getId())) {
            Optional<StarsForCoffeeSiteAndUser> sfcsu = siteStarsRepo.getOneStarEvalForSiteAndUser(coffeeSiteExtId, userExtId);

            return sfcsu.map(starsForCoffeeSiteAndUser -> {
                starsForCoffeeSiteAndUser.setStars(starsQualityService.findStarsQualityByNumOfStars(stars));
                // Updatovane hodnoceni ulozit do Repository
                starsForCoffeeSiteAndUser = siteStarsRepo.save(starsForCoffeeSiteAndUser);
                log.info("Stars for Coffee site id {} and User id {} updated. Stars: {}", coffeeSiteExtId, userExtId, stars);
                return starsForCoffeeSiteAndUser;
            }).orElse(null);
        }
        return null;
    }

    /**
     * Updates Stars for given CoffeeSite and User. Only Stars of the loggedIn user can be updated.
     *
     * @return
     */
    @Override
    public StarsForCoffeeSiteAndUser updateStarsForCoffeeSiteAndUser(CommentDTO commentDTO) {
        // Ulozit updated Stars if not 0
        if (commentDTO != null && commentDTO.getStarsFromUser() >= (StarsQualityDescription.StarsQualityEnum.ONE.ordinal() + 1)
                && commentDTO.getStarsFromUser() <= (StarsQualityDescription.StarsQualityEnum.FIVE.ordinal() + 1)
                && commentDTO.getUserId() != null) {

            int stars = commentDTO.getStarsFromUser();
            Optional<User> logedInUser = userService.getCurrentLoggedInUser();

            if (logedInUser.isPresent() && logedInUser.get().getId().equals(commentDTO.getUserId())) {
                // Ziskat prislusny objekt StarsForCoffeeSiteAndUser z DB
                Optional<CoffeeSite> coffeeSite = coffeeSiteService.findOneByExternalId(commentDTO.getCoffeeSiteId());
                Optional<User> user = userService.findByExtId(commentDTO.getUserId());
                if (coffeeSite.isPresent() && user.isPresent()) {
                    return updateStarsForCoffeeSiteAndUser(coffeeSite.get().getId(), user.get().getId(), stars);
                }
            }

        }

        if (commentDTO != null) {
            log.error("Updating Stars for Coffee site id {} and User id {} failed.", commentDTO.getCoffeeSiteId(), commentDTO.getUserId());
        }
        return null;
    }

//    @Override
//    public void cancelStarsForCoffeeSite(CoffeeSite coffeeSite, User user) {
//        siteStarsRepo.deleteStarsForSiteAndUser(coffeeSite.getId(), user.getId());
//        log.info("Stars for Coffee site name {} and User name {} canceled.", coffeeSite.getSiteName(), user.getUserName());
//    }

    /**
     * Ulozit/updatovat hodnoceni pro dany CoffeeSite pro aktualne prihlaseneho uzivatele.
     * Only logged-in user can save the stars.
     */
    public void saveStarsForCoffeeSiteAndLoggedInUser(String coffeeSiteExtId, Integer stars) {
        coffeeSiteService.findOneByExternalId(coffeeSiteExtId).ifPresent(cs -> {
            Optional<User> logedInUser = userService.getCurrentLoggedInUser();
            logedInUser.ifPresent(user -> saveStarsForCoffeeSiteAndUser(cs, user, stars));
        });
    }


    /**
     * Returns number of stars for CoffeeSiteId and userID already entered by user.
     * If no star were already save, returns 0
     */
    @Override
    public Integer getStarsForCoffeeSiteAndUser(String coffeeSiteExtId, String userExtId) {
        return getStarsForCoffeeSiteAndUser(UUID.fromString(coffeeSiteExtId), UUID.fromString(userExtId));
    }

    @Override
    public Integer getStarsForCoffeeSiteAndUser(UUID coffeeSiteExtId, UUID userExtId) {
        Optional<CoffeeSite> coffeeSite = coffeeSiteService.findOneByExternalId(coffeeSiteExtId);
        return coffeeSite.flatMap(cs -> userService.findByExtId(userExtId)
                        .flatMap(user -> siteStarsRepo.getOneStarEvalForSiteAndUser(cs.getId(), user.getId())))
                .map(stars -> stars.getStars().getNumOfStars())
                .orElse(0);
    }

    @Override
    public StarsQualityDescription getStarsForCoffeeSiteAndLoggedInUser(CoffeeSite coffeeSite) {

        Optional<User> logedInUser = userService.getCurrentLoggedInUser();
        Optional<StarsForCoffeeSiteAndUser> userSiteStars = Optional.empty();
        if (logedInUser.isPresent()) {
            log.info("Retrieving Stars for Coffee site name {} and User name {}", coffeeSite.getSiteName(), logedInUser.get().getUserName());
            userSiteStars = siteStarsRepo.getOneStarEvalForSiteAndUser(coffeeSite.getId(), logedInUser.get().getId());
        } else
            log.info("Stars for Coffee site name {} cannot be retrieved, no user logged-in.", coffeeSite.getSiteName());
        return userSiteStars.map(StarsForCoffeeSiteAndUser::getStars).orElse(null);
    }

//    @Override
//    public AverageStarsForSiteDTO getStarsAndNumOfHodnoceniForSite(UUID coffeeSiteExtId) {
//        AverageStarsForSiteDTO starsDto = new AverageStarsForSiteDTO();
//
//        int numOfHodnoceni = siteStarsRepo.getNumOfHodnoceniForSite(coffeeSiteExtId);
//        if (numOfHodnoceni > 0) {
//            double stars = avgStarsForSite(coffeeSiteExtId);
//            starsDto.setAvgStars(stars);
//            starsDto.setNumOfHodnoceni(numOfHodnoceni);
//        }
//        log.info("Average Stars for Coffee site ext-id {} retrieved.", coffeeSiteExtId);
//        return starsDto;
//    }
}
