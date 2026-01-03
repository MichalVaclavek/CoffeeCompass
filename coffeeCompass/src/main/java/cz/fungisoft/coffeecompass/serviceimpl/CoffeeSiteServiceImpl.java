package cz.fungisoft.coffeecompass.serviceimpl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import cz.fungisoft.coffeecompass.dto.*;
import cz.fungisoft.coffeecompass.entity.*;
import cz.fungisoft.coffeecompass.mappers.CoffeeSiteMapper;
import cz.fungisoft.coffeecompass.repository.*;
import cz.fungisoft.coffeecompass.service.*;
import cz.fungisoft.coffeecompass.serviceimpl.images.ImagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.configuration.ConfigProperties;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.listeners.OnNewCoffeeSiteEvent;
import cz.fungisoft.coffeecompass.pojo.LatLong;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementace CoffeeSiteService. Implementuje vsechny metody, ktere pracuji s CoffeeSite objekty, tj.
 * zvlaste CRUD operace. Podstatnou casti jsou vyhledavaci metody podle ruznych kriterii.
 *
 * @author Michal Václavek
 */
@Service("coffeeSiteService")
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CoffeeSiteServiceImpl implements CoffeeSiteService {

    private static final String ERROR_SAVING_SITES = "Error saving list of CoffeeSites.";

    private final CoffeeSiteRepository coffeeSiteRepo;
    private final CoffeeSitePageableRepository coffeeSitePaginatedRepo;
    private final CoffeeSiteMapper coffeeSiteMapper;

    private final UserService userService;

    private final CoffeeSortService coffeeSortService;

    private final CoffeeSiteTypeService coffeeSiteTypeService;

    private final CSStatusService coffeeSiteStatusService;

    private final PriceRangeService priceRangeService;

    private final CompanyService companyService;

    private final CSRecordStatusService csRecordStatusService;

    private final SiteLocationTypeService siteLocationTypeService;

    private final CupTypeService cupTypeService;

    private final NextToMachineTypeService nextToMachineTypeService;

    private final OtherOfferService otherOfferService;

    private final StarsForCoffeeSiteAndUserRepository siteStarsRepo;
    private final CoffeeSiteRecordStatusRepository csRecordStatusRepo;

    private final ImageStorageService localImageService;

    private final ApplicationEventPublisher eventPublisher; // to inform about new/deleted CoffeeSites

    private ConfigProperties config;

    private User loggedInUser;

    private final ImagesService imagesService;

    @Autowired
    public void setConfig(ConfigProperties config) {
        this.config = config;
    }

    /**
     * Calls the methods to evaluate attributes used in UI determining allowed operations with CoffeeSite.
     *
     * @param site
     * @return
     */
    @Override
    public CoffeeSiteDTO evaluateOperationalAttributes(CoffeeSiteDTO site) {
        // logged-in user needed for evaluation of available operations for CoffeeSiteDto
        loggedInUser = userService.getCurrentLoggedInUser().orElse(null);

        site.setCanBeActivated(canBeActivated(site));
        site.setCanBeCanceled(canBeCanceled(site));
        site.setCanBeDeleted(canBeDeleted(site));
        site.setCanBeDeactivated(canBeDeactivated(site));
        site.setCanBeModified(canBeModified(site));
        site.setVisible(isVisible(site));
        site.setCanBeCommented(canBeCommented(site));
        site.setCanBeRatedByStars(canBeRateByStars(site));

        return site;
    }

    private CoffeeSiteDTO evaluateAverageStars(CoffeeSiteDTO site) {
        site.setAverageStarsWithNumOfHodnoceni(getStarsAndNumOfHodnoceniForSite(site.getExtId()));
        return site;
    }

    /**
     * Adds attributes to CoffeeSite to identify what operations can be done with CoffeeSite in UI
     *
     * @param sites
     * @return
     */
    private List<CoffeeSiteDTO> modifyToTransfer(List<CoffeeSite> sites) {
        return sites.stream().map(this::mapOneToTransfer).toList();
    }

    @Cacheable(cacheNames = "coffeeSitesCache")
    @Override
    public List<CoffeeSiteDTO> findAll(String orderBy, String direction) {
        List<CoffeeSite> items = coffeeSiteRepo.findAll(Sort.by(Sort.Direction.fromString(direction.toUpperCase()), orderBy));
        log.info("All Coffee sites retrieved: " + items.size());
        return modifyToTransfer(items);
    }


    @Override
    public Page<CoffeeSiteDTO> findAllPaginated(Pageable pageable) {
        Page<CoffeeSite> coffeeSitesPage = coffeeSitePaginatedRepo.findAll(pageable);
        // Transforms content to CoffeeSiteDTO
        return coffeeSitesPage.map(this::mapOneToTransfer);
    }

    @Cacheable(cacheNames = "coffeeSitesCache")
    @Override
    public List<CoffeeSiteDTO> findAllWithRecordStatus(CoffeeSiteRecordStatusEnum csRecordStatus) {
        List<CoffeeSite> items = coffeeSiteRepo.findSitesWithRecordStatus(csRecordStatus.getSiteRecordStatus());
        log.info("All Coffee sites with status {} retrieved: {}", csRecordStatus, items.size());
        return modifyToTransfer(items);
    }

    /**
     * Same method as findAllWithRecordStatus, but with paginated result
     */
    @Override
    public Page<CoffeeSiteDTO> findAllWithRecordStatusPaginated(Pageable pageable, CoffeeSiteRecordStatusEnum csRecordStatus) {
        Page<CoffeeSite> coffeeSitesPage = coffeeSitePaginatedRepo.findByRecordStatus(csRecordStatusService.findCSRecordStatus(csRecordStatus), pageable);
        return coffeeSitesPage.map(this::mapOneToTransfer); // Transforms content to CoffeeSiteDTO
    }


    /**
     * Used to get all CoffeeSites from search point with respective record status. Especially for non logged-in user
     * which can retrieve only ACTIVE sites.
     */
    @Cacheable(cacheNames = "coffeeSitesCache")
    @Override
    public List<CoffeeSiteDTO> findAllWithinRangeWithRecordStatus(double zemSirka, double zemDelka, long meters, CoffeeSiteRecordStatus csRecordStatus) {
        List<CoffeeSite> items = coffeeSiteRepo.findSitesWithRecordStatus(zemSirka, zemDelka, meters, csRecordStatus);
        return countDistancesAndSortByDist(modifyToTransfer(items), zemSirka, zemDelka);
    }

    /**
     * Used to get number of CoffeeSites from search point in different distances from search point with respective record status. Especially for non logged-in user
     * which can retrieve only ACTIVE sites.
     */
    @Override
    public Map<String, Integer> findNumbersOfSitesInGivenDistances(double zemSirka, double zemDelka, List<Integer> distances, String siteStatus) {
        Optional<CoffeeSiteStatus> csS = coffeeSiteStatusService.findCoffeeSiteStatusByName(siteStatus);
        // only ACTIVE sites are relevant for distance searching - all users (even non-registered, not loggedd-in) can search, so only ACTIVE are interesting
        return csS.map(coffeeSiteStatus -> coffeeSiteRepo.findNumbersOfSitesInGivenDistances(zemSirka, zemDelka, distances, coffeeSiteStatus, csRecordStatusService.findCSRecordStatus(CoffeeSiteRecordStatusEnum.ACTIVE)))
                .orElse(Collections.emptyMap());
    }

    @Cacheable(cacheNames = "coffeeSitesCache")
    @Override
    public List<CoffeeSiteDTO> findAllFromUser(User user) {
        List<CoffeeSite> items = coffeeSiteRepo.findSitesFromUserID(user.getId());
        log.info("All Coffee sites from user {} retrieved: {}", user.getUserName(), items.size());
        return modifyToTransfer(items);
    }

    @Override
    public Integer getNumberOfSitesFromUserId(UUID userId) {
        Integer numberOfSitesFromUser = coffeeSiteRepo.getNumberOfSitesFromUserID(userId);
        return (numberOfSitesFromUser != null) ? numberOfSitesFromUser : 0;
    }

    @Override
    public Integer getNumberOfSitesFromLoggedInUser() {
        return userService.getCurrentLoggedInUser()
                .map(user -> getNumberOfSitesFromUserId(user.getId()))
                .orElse(0);
    }

    @Override
    public Integer getNumberOfSitesNotCanceledFromUserId(UUID userId) {
        Integer numberOfSitesFromUser = coffeeSiteRepo.getNumberOfSitesNotCanceledFromUserID(userId);
        return (numberOfSitesFromUser != null) ? numberOfSitesFromUser : 0;
    }

    @Override
    public Integer getNumberOfSitesNotCanceledFromLoggedInUser() {
        return userService.getCurrentLoggedInUser()
                .map(user -> getNumberOfSitesNotCanceledFromUserId(user.getId()))
                .orElse(0);
    }

    @Cacheable(cacheNames = "coffeeSitesCache")
    @Override
    public List<CoffeeSiteDTO> findAllFromLoggedInUser() {
        return userService.getCurrentLoggedInUser()
                .map(this::findAllFromUser)
                .orElse(Collections.emptyList());
    }


    @Override
    public Page<CoffeeSiteDTO> findAllFromLoggedInUserPaginated(Pageable pageable) {
        return userService.getCurrentLoggedInUser()
                .map(user -> coffeeSitePaginatedRepo.findByOriginalUser(user, pageable))
                .map(coffeeSitesPage -> coffeeSitesPage.map(this::mapOneToTransfer))
                .orElseGet(Page::empty);
    }

    @Cacheable(cacheNames = "coffeeSitesCache")
    @Override
    public Page<CoffeeSiteDTO> findAllNotCancelledFromLoggedInUserPaginated(Pageable pageable) {
        return userService.getCurrentLoggedInUser()
                .map(user -> coffeeSitePaginatedRepo.findByOriginalUserAndRecordStatusStatusNot(user, "CANCELED", pageable)
                        .map(this::mapOneToTransfer))
                .orElseGet(Page::empty);
    }


    @Override
    public Optional<CoffeeSiteDTO> findOneToTransfer(String externalId) {
        return findOneByExternalId(externalId).map(this::mapOneToTransfer);
    }

    @Override
    public Optional<CoffeeSiteDTO> findOneToTransfer(UUID externalId) {
        return findOneByExternalId(externalId).map(this::mapOneToTransfer);
    }

    public CoffeeSiteDTO mapOneToTransfer(CoffeeSite site) {
        CoffeeSiteDTO siteDto = null;
        if (site != null) {
            siteDto = coffeeSiteMapper.coffeeSiteToCoffeeSiteDTO(site);
            siteDto.setMainImageURL(getMainImageURL(site));
            siteDto = evaluateAverageStars(evaluateOperationalAttributes(siteDto));
        }
        return siteDto;
    }

    @Override
    public Optional<CoffeeSite> findOneByExternalId(String externalId) {
        return findOneByExternalId(UUID.fromString(externalId));
    }

    @Cacheable(cacheNames = "coffeeSitesCache")
    @Override
    public Optional<CoffeeSite> findOneByExternalId(UUID externalId) {
        Optional<CoffeeSite> site = coffeeSiteRepo.findById(externalId);
        site.ifPresent(cs -> log.info("Coffee site with id {} retrieved.", externalId));
        return site;
    }

    @Override
    public CoffeeSite save(CoffeeSiteDTO cs) {
        CoffeeSite csToSave = coffeeSiteMapper.coffeeSiteDtoToCoffeeSite(cs);
        return save(csToSave);
    }

    /**
     * Metoda pro provedeni akci pred ulozenim CoffeeSitu a zavolani metody save() z Repository.
     * Tato metoda by mela byt volatelna pouze prihlasenym uzivatelem.
     *
     * @param - CoffeeSite k ulozeni. Melo by ukladat pouze novy CoffeeSite.
     */
    @Override
    public CoffeeSite save(CoffeeSite coffeeSite) {
        Optional<User> currentLoggedInUser = userService.getCurrentLoggedInUser();
        AtomicReference<CoffeeSite> savedCoffeeSite = new AtomicReference<>();
        currentLoggedInUser.ifPresent(user -> {
            if (coffeeSite.getId() == null) { // Zcela novy CoffeeSite
                coffeeSite.setId(UUID.randomUUID());

                CoffeeSiteRecordStatus coffeeSiteRecordStatus = csRecordStatusService.findCSRecordStatus(CoffeeSiteRecordStatusEnum.CREATED);
                coffeeSite.setRecordStatus(coffeeSiteRecordStatus);

                coffeeSite.setOriginalUser(user);
                user.setCreatedSites(user.getCreatedSites() + 1);
                if (coffeeSite.getCreatedOn() == null) {
                    coffeeSite.setCreatedOn(LocalDateTime.now());
                }
                userService.saveUser(user);
            }
            // Zjisteni, jestli Company je nove nebo ne
            if (coffeeSite.getDodavatelPodnik() != null) {
                Company comp = companyService.findCompanyByName(coffeeSite.getDodavatelPodnik().toString());

                if (comp == null) { // Save new company
                    comp = companyService.saveCompany(coffeeSite.getDodavatelPodnik().toString());
                }
                coffeeSite.setDodavatelPodnik(comp);
            }

            Optional<CoffeeSiteType> coffeeSiteType = coffeeSiteTypeService.findById(coffeeSite.getTypPodniku().getId());
            coffeeSiteType.ifPresent(coffeeSite::setTypPodniku);

            Optional<CoffeeSiteStatus> coffeeSiteStatus = coffeeSiteStatusService.findCoffeeSiteStatusById(coffeeSite.getStatusZarizeni().getId());
            coffeeSiteStatus.ifPresent(coffeeSite::setStatusZarizeni);

            Optional<PriceRange> priceRange = priceRangeService.findPriceRangeById(coffeeSite.getCena().getId());
            priceRange.ifPresent(coffeeSite::setCena);

            Optional<SiteLocationType> siteLocationType = siteLocationTypeService.findSiteLocationTypeById(coffeeSite.getTypLokality().getId());
            siteLocationType.ifPresent(coffeeSite::setTypLokality);

            // Load all CoffeeSorts from DB and assign to CoffeeSite
            if (coffeeSite.getCoffeeSorts() != null) {
                var coffeeSorts = coffeeSite.getCoffeeSorts().stream().map(cs -> coffeeSortService.findCoffeeSortById(cs.getId()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
                coffeeSite.setCoffeeSorts(coffeeSorts);

            }

            // Load all CupTypes from DB and assign to CoffeeSite
            if (coffeeSite.getCupTypes() != null) {
                var cupTypes = coffeeSite.getCupTypes().stream().map(ct -> cupTypeService.findCupTypeById(ct.getId()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
                coffeeSite.setCupTypes(cupTypes);
            }

            // Load all NextToMachineTypes from DB and assign to CoffeeSite
            if (coffeeSite.getNextToMachineTypes() != null) {
                var nextToMachineTypes = coffeeSite.getNextToMachineTypes().stream().map(ntmt -> nextToMachineTypeService.findById(ntmt.getId()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
                coffeeSite.setNextToMachineTypes(nextToMachineTypes);
            }

            // Load all OtherOffers from DB and assign to CoffeeSite
            if (coffeeSite.getOtherOffers() != null) {
                var otherOffers = coffeeSite.getOtherOffers().stream().map(oo -> otherOfferService.findOtherOfferById(oo.getId()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
                coffeeSite.setOtherOffers(otherOffers);
            }

            savedCoffeeSite.set(coffeeSiteRepo.save(coffeeSite));
            log.info("CoffeeSite name {} saved into DB.", savedCoffeeSite.get().getSiteName());
        });

        // If currentLoggedInUser is not present, then return Exception
        // This should not happen, as this method should be called only by logged-in user
        if (currentLoggedInUser.isEmpty()) {
            throw new EntityNotFoundException("User not found when saving CoffeeSite.");
        }
        return savedCoffeeSite.get();
    }

    /**
     * Ulozeni seznamu novych CoffeeSites
     */
    @Override
    public boolean save(List<CoffeeSiteDTO> coffeeSites) {
        try {
            coffeeSites.forEach(this::save);
            return true;
        } catch (Exception ex) {
            log.error(ERROR_SAVING_SITES);
            return false;
        }
    }

    /**
     * Ulozeni seznamu novych nebo updatovanych CoffeeSites
     */
    @Override
    public boolean saveOrUpdate(List<CoffeeSiteDTO> coffeeSites) {
        try {
            coffeeSites.forEach(cs -> {
                if (cs.getExtId() == null) {
                    this.save(cs);
                } else {
                    this.updateSite(cs);
                }
            });
            return true;
        } catch (Exception ex) {
            log.error(ERROR_SAVING_SITES);
            return false;
        }
    }


    /**
     * Saves list of new or updated CoffeeSites and returns them as saved/updated if successful.
     * If operation fails, returns empty list.
     *
     * @param coffeeSites CoffeeSites to be saved/updated
     * @return list of saved/updated CoffeeSites if successful, otherwise empty list
     */
    public List<CoffeeSiteDTO> saveOrUpdateWithResult(List<CoffeeSiteDTO> coffeeSites) {
        List<CoffeeSiteDTO> retVal = new ArrayList<>();
        try {
            coffeeSites.forEach(cs ->
                    retVal.add(cs.getExtId() == null ? mapOneToTransfer(save(cs))
                            : mapOneToTransfer(updateSite(cs)))
            );
            return retVal;
        } catch (Exception ex) {
            log.error(ERROR_SAVING_SITES);
            return Collections.emptyList();
        }
    }


    /**
     * Ulozeni modifikovaneho CoffeeSiteDTO. Status zaznamu se touto metodou menit nemuze. Pro zmenu statusu zaznamu
     * existuji samostatne metody.
     *
     * @param coffeeSite
     */
    public CoffeeSite updateSite(CoffeeSiteDTO coffeeSite) {
        CoffeeSite entityFromDB = coffeeSiteRepo.findById(coffeeSite.getExtId()).orElse(null);

        CoffeeSite coffeeSiteUpdatedByUser = coffeeSiteMapper.coffeeSiteDtoToCoffeeSite(coffeeSite);

        if (entityFromDB != null) {
            entityFromDB.setUpdatedOn(LocalDateTime.now());
            userService.getCurrentLoggedInUser()
                    .ifPresent(user -> {
                        user.setUpdatedSites(user.getUpdatedSites() + 1);
                        userService.saveUser(user);
                        entityFromDB.setLastEditUser(user);
                    });

            entityFromDB.setCena(coffeeSiteUpdatedByUser.getCena());

            if (coffeeSiteUpdatedByUser.getCoffeeSorts() != null) {
                entityFromDB.getCoffeeSorts().clear();
                for (CoffeeSort cs : coffeeSiteUpdatedByUser.getCoffeeSorts()) {
                    entityFromDB.getCoffeeSorts().add(cs);
                }
            }

            if (coffeeSiteUpdatedByUser.getCupTypes() != null) {
                entityFromDB.getCupTypes().clear();
                for (CupType cp : coffeeSiteUpdatedByUser.getCupTypes()) {
                    entityFromDB.getCupTypes().add(cp);
                }
            }
            if (coffeeSiteUpdatedByUser.getNextToMachineTypes() != null) {
                entityFromDB.getNextToMachineTypes().clear();
                for (NextToMachineType ntmt : coffeeSiteUpdatedByUser.getNextToMachineTypes()) {
                    entityFromDB.getNextToMachineTypes().add(ntmt);
                }
            }
            if (coffeeSiteUpdatedByUser.getOtherOffers() != null) {
                entityFromDB.getOtherOffers().clear();
                for (OtherOffer oo : coffeeSiteUpdatedByUser.getOtherOffers()) {
                    entityFromDB.getOtherOffers().add(oo);
                }
            }

            Company company = null;
            if (coffeeSiteUpdatedByUser.getDodavatelPodnik() != null) {
                company = companyService.findCompanyByName(coffeeSiteUpdatedByUser.getDodavatelPodnik().toString());
                if (company == null) {
                    company = companyService.saveCompany(coffeeSiteUpdatedByUser.getDodavatelPodnik().toString());
                }
            }

            entityFromDB.setDodavatelPodnik(company);

            entityFromDB.setMesto(coffeeSiteUpdatedByUser.getMesto());
            entityFromDB.setNumOfCoffeeAutomatyVedleSebe(coffeeSiteUpdatedByUser.getNumOfCoffeeAutomatyVedleSebe());
            entityFromDB.setPristupnostDny(coffeeSiteUpdatedByUser.getPristupnostDny());
            entityFromDB.setPristupnostHod(coffeeSiteUpdatedByUser.getPristupnostHod());
            entityFromDB.setSiteName(coffeeSiteUpdatedByUser.getSiteName());
            entityFromDB.setStatusZarizeni(coffeeSiteUpdatedByUser.getStatusZarizeni());
            entityFromDB.setTypPodniku(coffeeSiteUpdatedByUser.getTypPodniku());
            entityFromDB.setTypLokality(coffeeSiteUpdatedByUser.getTypLokality());
            entityFromDB.setUliceCP(coffeeSiteUpdatedByUser.getUliceCP());
            entityFromDB.setZemDelka(coffeeSiteUpdatedByUser.getZemDelka());
            entityFromDB.setZemSirka(coffeeSiteUpdatedByUser.getZemSirka());
            entityFromDB.setInitialComment(coffeeSiteUpdatedByUser.getInitialComment());

            log.info("CoffeeSite name {} updated.", coffeeSiteUpdatedByUser.getSiteName());
        }

        return entityFromDB;
    }

    /**
     * Zmena CoffeeSite record statusu
     */
    @Override
    public CoffeeSite updateCSRecordStatusAndSave(CoffeeSite cs, CoffeeSiteRecordStatusEnum newStatus) {
        cs.setRecordStatus(csRecordStatusService.findCSRecordStatus(newStatus));
        // to Listeners want to know that new CoffeeSite is activated
        // in our case used to send push notifications to those subscribed for new CoffeeSites
        if (cs.getRecordStatus().getRecordStatus() == CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.ACTIVE) {
            eventPublisher.publishEvent(new OnNewCoffeeSiteEvent(cs));
        }
        return coffeeSiteRepo.save(cs);
    }

    @Override
    public void delete(Long id) {
        coffeeSiteRepo.cancelById(id);
        log.info("CoffeeSite id {} deleted from DB.", id);
    }

    @Override
    public void delete(String externalId) {
//        coffeeSiteRepo.deleteByExternalId(UUID.fromString(externalId));
        coffeeSiteRepo.deleteById(UUID.fromString(externalId));
        log.info("CoffeeSite external id {} deleted from DB.", externalId);
    }

    @Override
    public double getDistance(double zemDelka1, double zemSirka1, double zemDelka2, double zemSirka2) {
        return coffeeSiteRepo.callStoredProcedureCalculateDistance(zemDelka1, zemSirka1, zemDelka2, zemSirka2);
    }

    /**
     * Najde vsechny CoffeeSites v okruhu meters od polohy zemSirka a zemDelka.<br>
     * Vzdy se bude vracet ve vzestupnem poradi podle vzdalenosti od bodu vyhledavani
     */
    @Override
    public List<CoffeeSiteDTO> findAllWithinCircle(double zemSirka, double zemDelka, long meters) {
        List<CoffeeSite> coffeeSites = coffeeSiteRepo.findSitesWithinRange(zemSirka, zemDelka, meters);
        log.info("All Coffee sites within circle (Latit.: {} , Long.: {}, range: {}) retrieved: {}", zemSirka, zemDelka, meters, coffeeSites.size());
        return countDistancesAndSortByDist(modifyToTransfer(coffeeSites), zemSirka, zemDelka);
    }

    /**
     * True, if there is already created a different CoffeeSite on 'zemSirka', 'zemDelka' location within meters range,
     * otherwise false.
     */
//    @Override
//    public boolean isLocationAlreadyOccupied(double zemSirka, double zemDelka, long meters, String siteId) {
//        long numOfSites = coffeeSiteRepo.getNumberOfSitesWithinRange(zemSirka, zemDelka, meters);
//        // If only one site is found in the neighborhood, check if it is a new site or curently modified site
//        // if it is current modified site, then the location is considered to be available.
//        // Means only move of the CoffeeSite to correct new position with no other neighbors
//        if (numOfSites == 1 && siteId != null) {
//            return findOneByExternalId(siteId).isEmpty();
//        }
//
//        return numOfSites > 0;
//    }


    /**
     * True, if there is already created and ACTIVE different CoffeeSite on 'zemSirka', 'zemDelka' location within meters range,
     * otherwise false.
     */
    @Override
    public boolean isLocationAlreadyOccupiedByActiveSite(double zemSirka, double zemDelka, long meters, UUID siteId) {
        CoffeeSiteRecordStatus activeRecordStatus = csRecordStatusService.findCSRecordStatus(CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.ACTIVE);

        long numOfSites = coffeeSiteRepo.getNumberOfSitesWithinRangeInGivenStatus(zemSirka, zemDelka, meters, activeRecordStatus.getId());
        // If only one site is found in the neighborhood, check if it is a new site or currently modified site
        // if it is current modified site, then the location is considered to be available.
        // Means only move of the CoffeeSite to correct new position with no other ACTIVE neighbors
        if (numOfSites == 1 && siteId != null) { //TODO - check if this situation may ocure?
            return findOneByExternalId(siteId).filter(neighborSite -> neighborSite.getRecordStatus().getRecordStatus() == CoffeeSiteRecordStatusEnum.ACTIVE).isEmpty();
        }

        return numOfSites > 0;
    }

    /**
     * Najde vsechny CoffeeSites v okruhu meters od polohy zemSirka a zemDelka.
     * Vzdy se bude vracet ve vzestupnem poradi podle vzdalenosti od bodu vyhledavani.
     * <br>
     * Vyhledava i podle dalsich kriterii a to CoffeeSort a/nebo CoffeeSiteStatus. Podle hodnot
     * techto kriterii pak vola spravne repository metody.
     */
    @Override
    public List<CoffeeSiteDTO> findAllWithinCircleWithCSStatusAndCoffeeSort(double zemSirka, double zemDelka, long meters,
                                                                            String cSort, String siteStatus) {
        List<CoffeeSite> coffeeSites = new ArrayList<>();
        /*
         * Jsou 4 mozne kombinace parametru cSort a siteStatus podle toho jestli jsou nebo nejsou prazdne
         * Podle techto kombinaci se volaji 4 ruzne metody v Repository
         */
        boolean csStatusFilter = (siteStatus != null) && !siteStatus.isEmpty();
        boolean cSortFilter = (cSort != null) && !cSort.isEmpty();

        boolean csStatusOnlyFilter = csStatusFilter && !cSortFilter;
        boolean cSortFilterOnlyFilter = !csStatusFilter && cSortFilter;
        boolean cSortAndcsStatusFilter = csStatusFilter && cSortFilter;
        boolean noFilter = !csStatusFilter && !cSortFilter;

        // only ACTIVE sites are relevant for distance searching - all users (even non-registered, not loggedd-in) can search, so only ACTIVE are interesting
        CoffeeSiteRecordStatus csRS = csRecordStatusRepo.searchByName(CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.ACTIVE.toString());

        if (noFilter) {
            coffeeSites = coffeeSiteRepo.findSitesWithRecordStatus(zemSirka, zemDelka, meters, csRS);
        }

        if (cSortAndcsStatusFilter) {
            Optional<CoffeeSort> cs = coffeeSortService.searchByName(cSort);
            Optional<CoffeeSiteStatus> csS = coffeeSiteStatusService.findCoffeeSiteStatusByName(siteStatus);
            coffeeSites = cs.flatMap(coffeeSort ->
                    csS.map(coffeeSiteStatus ->
                            coffeeSiteRepo.findSitesWithCoffeeSortAndSiteStatus(zemSirka, zemDelka, meters, coffeeSort, coffeeSiteStatus, csRS))).orElse(List.of());
        }

        if (cSortFilterOnlyFilter) {
            Optional<CoffeeSort> cfSort = coffeeSortService.searchByName(cSort);
            coffeeSites = cfSort.map(coffeeSort -> coffeeSiteRepo.findSitesWithCoffeeSort(zemSirka, zemDelka, meters, coffeeSort, csRS)).orElse(List.of());
        }

        if (csStatusOnlyFilter) {
            Optional<CoffeeSiteStatus> csS = coffeeSiteStatusService.findCoffeeSiteStatusByName(siteStatus);
            coffeeSites = csS.map(coffeeSiteStatus -> coffeeSiteRepo.findSitesWithStatus(zemSirka, zemDelka, meters, coffeeSiteStatus, csRS)).orElse(List.of());
        }

        log.info("Coffee sites within circle (Latit.: {} , Long.: {}, range: {}) retrieved: {}", zemSirka, zemDelka, meters, coffeeSites.size());
        return countDistancesAndSortByDist(modifyToTransfer(coffeeSites), zemSirka, zemDelka);
    }


    /**
     * TODO - NOT working with CoffeeSite status is null and CoffeeSort is not null. Error in Repository.
     *
     * @param zemSirka
     * @param zemDelka
     * @param rangeMeters
     * @param siteStatus  - muze byt prazde nebo null
     * @param cityName    - muze byt prazde nebo null
     */
    @Override
    public List<CoffeeSiteDTO> findAllWithinCircleAndCityWithCSStatusAndCoffeeSort(double zemSirka, double zemDelka, long rangeMeters,
                                                                                   String siteStatus, String cityName) {
        CoffeeSiteRecordStatus csRS = csRecordStatusRepo.searchByName(CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.ACTIVE.toString());
        Optional<CoffeeSiteStatus> csStatus = siteStatus != null ? coffeeSiteStatusService.findCoffeeSiteStatusByName(siteStatus)
                : coffeeSiteStatusService.findCoffeeSiteStatusByName(CoffeeSiteStatus.CoffeeSiteStatusEnum.INSERVICE.getSiteStatus());

        List<CoffeeSite> coffeeSites;
        coffeeSites = csStatus.map(csS -> coffeeSiteRepo.findSitesWithSortAndSiteStatusAndRangeAndCity(zemSirka, zemDelka, rangeMeters, csS, csRS, cityName)).
                orElse(List.of());

        log.info("Coffee sites within circle (Latit.: {} , Long.: {}, range: {}) and city {} retrieved: {}", zemSirka, zemDelka, rangeMeters, cityName, coffeeSites.size());
        return countDistancesAndSortByDist(modifyToTransfer(coffeeSites), zemSirka, zemDelka);
    }


    @Override
    public List<CoffeeSiteDTO> getLatestCoffeeSites(int numOfLatestSites) {
        List<CoffeeSite> items = coffeeSiteRepo.getLatestSites(numOfLatestSites, config.getDaysBackForNewestSites());
        log.info("Newest Coffee sites retrieved: " + items.size());
        return modifyToTransfer(items);
    }

    @Override
    public List<CoffeeSiteDTO> getCoffeeSitesActivatedInLastDays(int numOfDays) {
        List<CoffeeSite> items = coffeeSiteRepo.getLatestSites(99, (numOfDays > 62 || numOfDays < 1) ? 21 : numOfDays);
        log.info("Newest Coffee sites of the last {} days retrieved: {}", numOfDays, items.size());
        return modifyToTransfer(items);
    }


    @Override
    public List<CoffeeSiteDTO> findAllByCityNameExactly(String cityName) {
        List<CoffeeSite> items = coffeeSiteRepo.getAllSitesInCityExactly(cityName);
        log.info("All Coffee sites of '{}' city retrieved: {}", cityName, items.size());
        return modifyToTransfer(items);
    }

    @Override
    public List<CoffeeSiteDTO> findAllByCityNameAtStart(String cityName) {
        List<CoffeeSite> items = coffeeSiteRepo.getAllActiveSitesInCity(cityName);
        log.info("All Coffee sites in city name starting with '{}' retrieved: {}", cityName, items.size());
        return modifyToTransfer(items);
    }


    /**
     * Pomocna metoda pro vypocet vzdalenosti vsech CoffeeSite v seznamu od bodu zemSirka, zemDelka
     * a usporadani seznamu podle vzdalenosti.
     *
     * @param sites
     * @param zemSirka
     * @param zemDelka
     * @return
     */
    private List<CoffeeSiteDTO> countDistancesAndSortByDist(List<CoffeeSiteDTO> sites, double zemSirka, double zemDelka) {
        // Vypocet vzdalenosti pro kazdy vraceny CoffeeSite
        for (CoffeeSiteDTO site : sites) {
            site.setDistFromSearchPoint(countDistanceMetersFromSearchPoint(zemSirka, zemDelka, site.getZemSirka(), site.getZemDelka()));
        }

        // Usporadani vysledku db dotazu (seznam CoffeeSites v danem okruhu) podle vzdalenosti od bodu hledani
        // Sama DB tyto vzdalenosti pro dane CoffeeSites nevraci
        return sites.stream()
                .sorted((Comparator.comparingLong(CoffeeSiteDTO::getDistFromSearchPoint)))
                .toList();
    }

    /**
     * Pomocna metoda pro vypocet vzdalenosti mezi 2 body na mape/globu. Souradnice bodu ve formatu double.
     * Prevzato ze stackoverflow.com
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return vzdalenost 2 zadanych bodu v metrech
     */
    private static long countDistanceMetersFromSearchPoint(double lat1, double lon1, double lat2, double lon2) {
        long eRadius = 6372000; // polomer Zeme v metrech, v CR?
        long distance;
        double c, a;

        double latDist = Math.toRadians(lat2 - lat1);
        double lonDist = Math.toRadians(lon2 - lon1);
        a = Math.pow(Math.sin(latDist / 2), 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(lonDist / 2), 2);
        c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        distance = Math.round(eRadius * c);

        return distance;
    }

    @Override
    public CoffeeSiteDTO findByName(String siteName) {
        CoffeeSiteDTO site = coffeeSiteMapper.coffeeSiteToCoffeeSiteDTO(coffeeSiteRepo.searchByName(siteName));
        if (site == null) {
            log.error("Coffee site with name {} not found in DB.", siteName);
            throw new EntityNotFoundException("Coffee site with name " + siteName + " not found.");
        }
        return site;
    }


    /**
     * A method to evaluate, if the CoffeeSite and User are "compatible"
     * i.e. if the user can modify the CoffeeSite.<br>
     * The user must be originator of the Site or has DBA or ADMIN roles
     *
     * @return
     */
    private boolean siteUserMatch(CoffeeSiteDTO cs) {
        if (loggedInUser != null && cs.getOriginalUserName() != null) {
            return (loggedInUser.getUserName().equals(cs.getOriginalUserName()) || userService.hasADMINorDBARole(loggedInUser));
        } else {
            return false;
        }
    }

    /**
     * CoffeeSite can be modified only if it is in CREATED or INACTIVE states.
     */
    private boolean canBeModified(CoffeeSiteDTO cs) {
        return siteUserMatch(cs) && (
                CoffeeSiteRecordStatusEnum.CREATED.toString().equals(cs.getRecordStatus().getStatus())
                        ||
                        CoffeeSiteRecordStatusEnum.INACTIVE.toString().equals(cs.getRecordStatus().getStatus())
        );
    }

    /**
     * Evaluates if the CoffeeSite can be activated based on logged-in user Privileges and CoffeeSite's current status.
     *
     * @param cs
     * @return
     */
    private boolean canBeActivated(CoffeeSiteDTO cs) {
        return siteUserMatch(cs) /* all authenticated users can modify from Created to Active or Inactive to Active */
                &&
                (CoffeeSiteRecordStatusEnum.CREATED.toString().equals(cs.getRecordStatus().getStatus())
                        ||
                        CoffeeSiteRecordStatusEnum.INACTIVE.toString().equals(cs.getRecordStatus().getStatus())
                );
    }

    private boolean canBeDeactivated(CoffeeSiteDTO cs) {
        return siteUserMatch(cs) /* all allowed users modify from Active to Inactive */
                &&
                (CoffeeSiteRecordStatusEnum.ACTIVE.toString().equals(cs.getRecordStatus().getStatus())
                        ||
                        (CoffeeSiteRecordStatusEnum.CANCELED.toString().equals(cs.getRecordStatus().getStatus()) // Admin or DBA users can modify from CANCELED to INACTIVE or Inactive to Active
                                &&
                                loggedInUser != null && userService.hasADMINorDBARole(loggedInUser)
                        )
                );
    }

    /**
     * @param cs
     * @return
     */
    private boolean canBeCanceled(CoffeeSiteDTO cs) {
        return siteUserMatch(cs) /* all users allowed to modify are also allowed change status from Inactive to Cancel or from CREATED to Cancel */
                &&
                (CoffeeSiteRecordStatusEnum.INACTIVE.toString().equals(cs.getRecordStatus().getStatus())
                        ||
                        CoffeeSiteRecordStatusEnum.CREATED.toString().equals(cs.getRecordStatus().getStatus())
                );
    }

    /**
     * Only ADMIN is allowed to Delete site permanently (from every CoffeeSite state)
     */
    private boolean canBeDeleted(CoffeeSiteDTO cs) {
        return Optional.ofNullable(loggedInUser)
                .filter(user -> userService.hasADMINRole(user))
                .isPresent();
    }

    /**
     * Evaluates if a Comment can be added to the CoffeeSite.
     * Currently, any logged-in user can comment the site.
     */
    private boolean canBeCommented(CoffeeSiteDTO cs) {
        return (loggedInUser != null);
    }

    /**
     * Evaluates if Stars can be added to the CoffeeSite.
     * Currently, any logged-in user can rate the site.
     */
    private boolean canBeRateByStars(CoffeeSiteDTO cs) {
        return (loggedInUser != null);
    }

    /**
     * Evaluates if the site is to be displayed in UI.
     * For anonymous users, only ACTIVE sites are visible
     * For logged-in user:
     * If not CANCELED, then visible
     * If CANCELED then only loggedd-in user with ADMIN or DBA Roles can see the CoffeeSite
     */
    private boolean isVisible(CoffeeSiteDTO cs) {
        if (CoffeeSiteRecordStatusEnum.ACTIVE.toString().equals(cs.getRecordStatus().getStatus()))
            return true;
        else {
            if (!CoffeeSiteRecordStatusEnum.CANCELED.toString().equals(cs.getRecordStatus().getStatus())) {
                // not ACTIVE site and not CANCELED, logged-in user
                // Logged-in user can see only ACTIVE Sites or only the sites he/she created
                return loggedInUser != null && siteUserMatch(cs);
            } else { // is CANCELED, only DBA and ADMIN can see the site
                return (loggedInUser != null) && userService.hasADMINorDBARole(loggedInUser);
            }
        }
    }

    /**
     * Based on ImageStorageService evaluates if the CoffeeSite with siteId
     * has saved Image.
     *
     * @return URL of the CoffeeSite's image if available
     */
    @Override
    public String getMainImageURL(CoffeeSite cs) {
        return imagesService.getBasicObjectImageUrl(cs.getId().toString())
                .orElseGet(() -> getLocalCoffeeSiteImageUrl(cs));
    }

    @Override
    public String getLocalCoffeeSiteImageUrl(CoffeeSite cs) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        UriComponentsBuilder extBuilder = builder.scheme("https").replacePath(localImageService.getBaseImageURLPath()).replaceQuery("").port("8443");
        String imageURI = extBuilder.build().toUriString() + cs.getId();
        return localImageService.isImageAvailableForSiteId(cs.getId()) ? imageURI : "";
    }

    /**
     * Checks if the coffe site name is already used or not
     */
    @Override
    public boolean isSiteNameUnique(UUID id, String siteName) {
        CoffeeSite site = coffeeSiteRepo.searchByName(siteName);
        return (site == null || (site.getId().equals(id)));
    }

    @Override
    public LatLong getAverageLocation(List<CoffeeSiteDTO> coffeeSites) {
        OptionalDouble avgLat = coffeeSites.stream().mapToDouble(CoffeeSiteDTO::getZemSirka).average();
        OptionalDouble avgLong = coffeeSites.stream().mapToDouble(CoffeeSiteDTO::getZemDelka).average();
        return new LatLong(avgLat.orElse(0), avgLong.orElse(0));
    }

    /**
     * Counts and returns the latitude and longitude of the "search point", which is
     * in "distance" from coffeeSiteDTO's location. The "search point" location
     * is counted in ratio 3/4/5 to south and west i.e. 5 is a distance, 4 is
     * the distance to the west and 3 is distance to the south. Only flat surface
     * pythagorian theorem is used, curvature of Eartch is not taken into account
     * as the limits from {distance is only from 50 to 5000 m.
     *
     * @param coffeeSiteDTO - the site from it's location the new "search location" is counted
     * @param distance      - distance in meters of returned "search location" from coffeeSiteDTO's location. Allowed values are from 50 to 5000, otherwise default 500 is set.
     */
    @Override
    public LatLong getSearchFromLocation(CoffeeSiteDTO coffeeSiteDTO, int distance) {

        if (distance < 50 || distance > 5000) {
            distance = 500;
        }

        double distanceNaDruhou = distance * distance;
        double distSouth = Math.sqrt(distanceNaDruhou - Math.pow(4 / 12d * distance, 2));
        double distWest = Math.sqrt(distanceNaDruhou - Math.pow(3 / 12d * distance, 2));

        long eRadius = 6372000;
        double obvodZeme = 2 * Math.PI * eRadius;
        double stupneNaMetr = 360d / obvodZeme; // one meter on Earth as part of the whole circle degrees (360 deggrees) 

        double searchPointLat = coffeeSiteDTO.getZemSirka() - distSouth * stupneNaMetr;
        double searchPointLong = coffeeSiteDTO.getZemDelka() - distWest * stupneNaMetr;

        return new LatLong(searchPointLat, searchPointLong);
    }

    @Override
    public void deleteCoffeeSitesFromUser(UUID userId) {
        Optional<User> user = userService.findByExtId(userId);
        user.ifPresent(u -> coffeeSiteRepo.deleteAllFromUser(u.getId()));
    }


    @Override
    public Page<CoffeeSiteDTO> getPageOfCoffeeSitesFromList(Pageable pageable, List<CoffeeSiteDTO> coffeeSitesList) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<CoffeeSiteDTO> list;

        if (coffeeSitesList.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, coffeeSitesList.size());
            list = coffeeSitesList.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), coffeeSitesList.size());
    }

    public AverageStarsForSiteDTO getStarsAndNumOfHodnoceniForSite(UUID coffeeSiteExtId) {
        AverageStarsForSiteDTO starsDto = new AverageStarsForSiteDTO();

        int numOfHodnoceni = siteStarsRepo.getNumOfHodnoceniForSite(coffeeSiteExtId);
        if (numOfHodnoceni > 0) {
            double stars = avgStarsForSite(coffeeSiteExtId);
            starsDto.setAvgStars(stars);
            starsDto.setNumOfHodnoceni(numOfHodnoceni);
        }
        log.info("Average Stars for Coffee site ext-id {} retrieved.", coffeeSiteExtId);
        return starsDto;
    }

    private double avgStarsForSite(UUID coffeeSiteExtId) {
        double starsAvg = 0;

        try {
            starsAvg = siteStarsRepo.averageStarsForSiteExternalId(coffeeSiteExtId);
            starsAvg = Math.round(starsAvg * 10.0) / 10.0; // one decimal place round
        } catch (Exception e) {
            log.info("Average stars could not be calculated: {}", e.getMessage());
        }

        return starsAvg;
    }


    //TODO dalsi vyhledavaci metody podle ruznych kriterii?
    // CriteriaQuery a CriteriaQueryBuilder
    /*
     * session.getTransaction().begin();
                                           
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<CoffeeSite> cq = cb.createQuery(CoffeeSite.class);
            Root<CoffeeSite> cms = cq.from(CoffeeSite.class);

            cq.select(cms).where(cb.equal(cms.get("userName"), userName));        
            coffeeSites = (List<CoffeeSite>)session.createQuery(cq).getResultList();
                
             session.getTransaction().commit();
         } catch (Exception e)
         {
             logger.error("Error retrieving CoffeeSites: " + ... + " Exception: " + e);
             session.getTransaction().rollback();

     */
}
