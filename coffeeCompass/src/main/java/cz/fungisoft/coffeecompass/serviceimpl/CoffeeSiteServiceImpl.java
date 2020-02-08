package cz.fungisoft.coffeecompass.serviceimpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.configuration.ConfigProperties;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.pojo.LatLong;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRecordStatusRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteStatusRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSortRepository;
import cz.fungisoft.coffeecompass.service.CSRecordStatusService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CompanyService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.ImageStorageService;
import cz.fungisoft.coffeecompass.service.UserService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;

/**
 * Implementace CoffeeSiteService. Implementuje vsechny metody, ktere pracuji s CoffeeSite objekty, tj.
 * zvlaste CRUD operace. Podstatnou casti jsou vyhledavaci metody podle ruznych kriterii.
 * 
 * @author Michal Václavek
 *
 */
@Service("coffeeSiteService")
@Transactional
@Log4j2
public class CoffeeSiteServiceImpl implements CoffeeSiteService
{
    private CoffeeSiteRepository coffeeSiteRepo;
    
    private CoffeeSortRepository coffeeSortRepo;
    
    private CoffeeSiteStatusRepository coffeeSiteStatusRepo;

    private MapperFacade mapperFacade;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private CSRecordStatusService csRecordStatusService;
    
    @Autowired
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    @Autowired
    private CoffeeSiteRecordStatusRepository csRecordStatusRepo;
    
    @Autowired
    private ImageStorageService imageService;
    
    private ConfigProperties config;
    
    private Optional<User> loggedInUser;
    
    
    @Autowired
    public CoffeeSiteServiceImpl(CoffeeSiteRepository coffeeSiteRepository,
                                 CoffeeSortRepository coffeeSortRepository,
                                 CoffeeSiteStatusRepository coffeeSiteStatusRepo,
                                 MapperFacade mapperFacade)  {
        this.coffeeSiteRepo = coffeeSiteRepository;
        this.coffeeSortRepo = coffeeSortRepository;
        this.coffeeSiteStatusRepo = coffeeSiteStatusRepo;
        this.mapperFacade = mapperFacade;
    }
    
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
        // Currently logged-in user needed for evaluation of available operations for CoffeeSiteDto 
        loggedInUser = userService.getCurrentLoggedInUser();
        
        site.setCanBeActivated(canBeActivated(site));
        site.setCanBeCanceled(canBeCanceled(site));
        site.setCanBeDeleted(canBeDeleted(site));
        site.setCanBeDeactivated(canBeDeactivated(site));
        site.setCanBeModified(canBeModified(site));
        site.setVisible(isVisible(site));
        site.setCanBeCommented(canBeCommented(site));
        site.setCanBeRatedByStars(canBeRateByStars(site));
        site.setAnyOtherSiteActiveOnSamePosition(isLocationAlreadyOccupiedByActiveSite(site.getZemSirka(), site.getZemDelka(), 5, site.getId()));
        
        site.setMainImageURL(getMainImageURL(site));
        
        return site;
    }
    
    private CoffeeSiteDTO evaluateAverageStars(CoffeeSiteDTO site) {
        site.setAverageStarsWithNumOfHodnoceni(starsForCoffeeSiteService.getStarsAndNumOfHodnoceniForSite(site.getId()));
        return site;
    }
    
    /**
     * Adds attributes to CoffeeSite to identify what operations can be done with CoffeeSite in UI
     * 
     * @param sites
     * @return
     */
    private List<CoffeeSiteDTO> modifyToTransfer(List<CoffeeSite> sites) {
        
        List<CoffeeSiteDTO> sitesToTransfer = mapperFacade.mapAsList(sites, CoffeeSiteDTO.class);
        
        for (CoffeeSiteDTO site : sitesToTransfer) {
            site = evaluateOperationalAttributes(site);
            site = evaluateAverageStars(site);
        }
        
        return sitesToTransfer;
    }
    
    @Override
    public List<CoffeeSiteDTO> findAll(String orderBy, String direction) {
        List<CoffeeSite> items = coffeeSiteRepo.findAll(new Sort(Sort.Direction.fromString(direction.toUpperCase()), orderBy));
        log.info("All Coffee sites retrieved: " + items.size());
        return modifyToTransfer(items);
    }
    
    @Override
    public List<CoffeeSiteDTO> findAllWithRecordStatus(CoffeeSiteRecordStatusEnum csRecordStatus) {
        List<CoffeeSite> items = coffeeSiteRepo.findSitesWithRecordStatus(csRecordStatus.getSiteRecordStatus());
        log.info("All Coffee sites with status {} retrieved: {}",  csRecordStatus.toString(), items.size());
        return modifyToTransfer(items);
    }
    
    /** 
     * Used to get all CoffeeSites from search point with respective record status. Especially for non logged-in user
     * which can retrieve omly ACTIVE sites.
     */
    @Override
    public List<CoffeeSiteDTO> findAllWithinRangeWithRecordStatus(double zemSirka, double zemDelka, long meters, CoffeeSiteRecordStatus csRecordStatus) {
        List<CoffeeSite> items = coffeeSiteRepo.findSitesWithRecordStatus(zemSirka, zemDelka, meters, csRecordStatus);
        return countDistancesAndSortByDist(modifyToTransfer(items), zemSirka, zemDelka);
    }
    
    @Override
    public List<CoffeeSiteDTO> findAllFromUser(User user) {
        List<CoffeeSite> items = coffeeSiteRepo.findSitesFromUserID(user.getId());
        log.info("All Coffee sites from user {} retrieved: {}",  user.getUserName(), items.size());
        return modifyToTransfer(items);
    }
    
    @Override
    public Integer getNumberOfSitesFromUserId(long userId) {
        Integer numberOfSitesFromUser = coffeeSiteRepo.getNumberOfSitesFromUserID(userId);
        return (numberOfSitesFromUser != null) ? numberOfSitesFromUser : 0;
    }
    
    @Override
    public Integer getNumberOfSitesFromLoggedInUser() {
        loggedInUser = userService.getCurrentLoggedInUser();
        return (loggedInUser.isPresent()) ? getNumberOfSitesFromUserId(loggedInUser.get().getId())
                                          : 0;
    }
    
    @Override
    public Integer getNumberOfSitesNotCanceledFromUserId(long userId) {
        Integer numberOfSitesFromUser = coffeeSiteRepo.getNumberOfSitesNotCanceledFromUserID(userId);
        return (numberOfSitesFromUser != null) ? numberOfSitesFromUser : 0;
    }

    @Override
    public Integer getNumberOfSitesNotCanceledFromLoggedInUser() {
        loggedInUser = userService.getCurrentLoggedInUser();
        return (loggedInUser.isPresent()) ? getNumberOfSitesNotCanceledFromUserId(loggedInUser.get().getId())
                                          : 0;
    }
    
    @Override
    public List<CoffeeSiteDTO> findAllFromUserName(String userName) {
        Optional<User> user = userService.findByUserName(userName);
        return (user.isPresent()) ? findAllFromUser(user.get()) : null;
    }
    
    @Override
    public List<CoffeeSiteDTO> findAllFromLoggedInUser() {
        loggedInUser = userService.getCurrentLoggedInUser();
        return findAllFromUser(mapperFacade.map(loggedInUser.get(), User.class));
    }

    @Override
    public CoffeeSiteDTO findOneToTransfer(Long id) {
        CoffeeSite site = findOneById(id);
        CoffeeSiteDTO siteDto = null;
        if (site != null) {
            siteDto = mapperFacade.map(site, CoffeeSiteDTO.class);
        
            siteDto = evaluateOperationalAttributes(siteDto);
            siteDto = evaluateAverageStars(siteDto);
        }
        return siteDto;
    }
    
    @Override
    public CoffeeSite findOneById(Long id) {
        CoffeeSite site = coffeeSiteRepo.findById(id).orElse(null);
        if (site == null) {
            log.error("Coffee site with id {} not found in DB.",  id);
            throw new EntityNotFoundException("Coffee site with id " + id + " not found.");
        }
        log.info("Coffee site with id {} retrieved.",  id);
        return site;
    }

    /**
     * Metoda pro provedeni akci pred ulozenim CoffeeSitu a zavolani metody save() z Repository.
     * Tato metoda by mela byt volatelna pouze prihlasenym uzivatelem.
     * 
     * @param - CoffeeSite k ulozeni. Melo by ukladat pouze novy CoffeeSite.
     */
    @Override
    public CoffeeSite save(CoffeeSite coffeeSite) {
        loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser.isPresent()) {
            User user = loggedInUser.get();
            if (coffeeSite.getId() == 0) { // Zcela novy CoffeeSite
                CoffeeSiteRecordStatus coffeeSiteRecordStatus = csRecordStatusService.findCSRecordStatus(CoffeeSiteRecordStatusEnum.CREATED);
                coffeeSite.setRecordStatus(coffeeSiteRecordStatus);
                coffeeSite.setOriginalUser(user);
                user.setCreatedSites(user.getCreatedSites() + 1);
                if (coffeeSite.getCreatedOn() == null) {
                    coffeeSite.setCreatedOn(new Timestamp(new Date().getTime()));
                }
                userService.saveUser(user);
            }
        }
            
        // Zjisteni, jestli Company je nove nebo ne
        if (coffeeSite.getDodavatelPodnik() != null) {
            Company comp = companyService.findCompanyByName(coffeeSite.getDodavatelPodnik().toString());
            
            if (comp == null) { // Save new company
                comp = companyService.saveCompany(coffeeSite.getDodavatelPodnik().toString());
            }
            coffeeSite.setDodavatelPodnik(comp);
        }
        log.info("CoffeeSite name {} saved into DB.", coffeeSite.getSiteName());
        return coffeeSiteRepo.save(coffeeSite);
    }
    
    @Override
    public CoffeeSite save(CoffeeSiteDTO cs) {
        
        CoffeeSite csToSave = mapperFacade.map(cs, CoffeeSite.class);
        return save(csToSave);
    }
    
    
    /**
     * Ulozeni modifikovaneho CoffeeSiteDTO.
     * 
     * @param coffeeSite
     */
    public CoffeeSite updateSite(CoffeeSiteDTO coffeeSite)
    {
        CoffeeSite entityFromDB = coffeeSiteRepo.findById(coffeeSite.getId()).orElse(null);
        
        if (entityFromDB != null) {
            loggedInUser = userService.getCurrentLoggedInUser();
            
            entityFromDB.setUpdatedOn(new Timestamp(new Date().getTime()));
            
            if (loggedInUser.isPresent()) {
                User user = loggedInUser.get();
                user.setUpdatedSites(user.getUpdatedSites() + 1);
                userService.saveUser(user);
                entityFromDB.setLastEditUser(user);
            }
            
            entityFromDB.setCena(coffeeSite.getCena());
            
            if (coffeeSite.getCoffeeSorts() != null) {
                for (CoffeeSort cs : coffeeSite.getCoffeeSorts()) {
                    entityFromDB.getCoffeeSorts().add(cs);
                }
            }
            
            if (coffeeSite.getCupTypes() != null) {
                for (CupType cp : coffeeSite.getCupTypes()) {
                    entityFromDB.getCupTypes().add(cp);
                }
            }
            if (coffeeSite.getNextToMachineTypes() != null) {
                for (NextToMachineType ntmt : coffeeSite.getNextToMachineTypes()) {
                    entityFromDB.getNextToMachineTypes().add(ntmt);
                }
            }
            if (coffeeSite.getOtherOffers() != null) {
                for (OtherOffer oo : coffeeSite.getOtherOffers()) {
                    entityFromDB.getOtherOffers().add(oo);
                }
            }
            
            Company comp = null;
            if (coffeeSite.getDodavatelPodnik() != null) {
                comp = companyService.findCompanyByName(coffeeSite.getDodavatelPodnik().toString());
                if (comp == null) {
                    comp = companyService.saveCompany(coffeeSite.getDodavatelPodnik().toString());
                }
            }
            
            entityFromDB.setDodavatelPodnik(comp);
           
            entityFromDB.setMesto(coffeeSite.getMesto());
            entityFromDB.setNumOfCoffeeAutomatyVedleSebe(coffeeSite.getNumOfCoffeeAutomatyVedleSebe());           
            entityFromDB.setPristupnostDny(coffeeSite.getPristupnostDny());
            entityFromDB.setPristupnostHod(coffeeSite.getPristupnostHod());
            entityFromDB.setSiteName(coffeeSite.getSiteName());
            entityFromDB.setStatusZarizeni(coffeeSite.getStatusZarizeni());
            entityFromDB.setTypPodniku(coffeeSite.getTypPodniku());
            entityFromDB.setTypLokality(coffeeSite.getTypLokality());
            entityFromDB.setUliceCP(coffeeSite.getUliceCP());
            entityFromDB.setZemDelka(coffeeSite.getZemDelka());
            entityFromDB.setZemSirka(coffeeSite.getZemSirka());
            entityFromDB.setInitialComment(coffeeSite.getInitialComment());
            
            if (coffeeSite.getRecordStatus() != null) {
                entityFromDB.setRecordStatus(coffeeSite.getRecordStatus());
            }
            
            log.info("CoffeeSite name {} updated.", coffeeSite.getSiteName());
        }
        
        //coffeeSiteRepo.save(entityFromDB);
        return entityFromDB;
        
    }
    
    /**
     * Zmena CoffeeSite record statusu
     */
    @Override
    public CoffeeSite updateCSRecordStatusAndSave(CoffeeSite cs, CoffeeSiteRecordStatusEnum newStatus) {
        cs.setRecordStatus(csRecordStatusService.findCSRecordStatus(newStatus));
        return coffeeSiteRepo.save(cs);
    }
    
    @Override
    public void delete(Long id) {
        coffeeSiteRepo.deleteById(id);
        log.info("CoffeeSite id {} deleted from DB.", id);
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
    @Override
    public boolean isLocationAlreadyOccupied(double zemSirka, double zemDelka, long meters, Long siteId) {
        long numOfSites = coffeeSiteRepo.getNumberOfSitesWithinRange(zemSirka, zemDelka, meters);
        // If only one site is found in the neighborhood, check if it is a new site or curently modified site
        // if it is current modified site, then the location is considered to be available.
        // Means only move of the CoffeeSite to correct new position with no other neighbors
        if (numOfSites == 1 && siteId > 0) {
            CoffeeSite neighborSite = findOneById(siteId);
            if (neighborSite != null) { // its only requested site here, so, the position is not ocupied
                return false;
            }
        }
            
        return numOfSites > 0;
    }
    
    
    /**
     * True, if there is already created and ACTIVE different CoffeeSite on 'zemSirka', 'zemDelka' location within meters range,
     * otherwise false.
     */
    @Override
    public boolean isLocationAlreadyOccupiedByActiveSite(double zemSirka, double zemDelka, long meters, Long siteId) {
        
        CoffeeSiteRecordStatus activeRecordStatus = csRecordStatusService.findCSRecordStatus(CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.ACTIVE);
        
        long numOfSites = coffeeSiteRepo.getNumberOfSitesWithinRangeInGivenStatus(zemSirka, zemDelka, meters, activeRecordStatus.getId());
        // If only one site is found in the neighborhood, check if it is a new site or curently modified site
        // if it is current modified site, then the location is considered to be available.
        // Means only move of the CoffeeSite to correct new position with no other ACTIVE neighbors
        if (numOfSites == 1 && siteId > 0) {
            CoffeeSite neighborSite = findOneById(siteId);
            if (neighborSite != null 
                && neighborSite.getRecordStatus().getRecordStatus() == CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.ACTIVE) { // its only requested site with ACTIVE status here, so, the position is not ocupied by any other ACTIVE site
                return false;
            }
        }
            
        return numOfSites > 0;
    }
    
    /**
     * Najde vsechny CoffeeSites v okruhu meters od polohy zemSirka a zemDelka.
     * Vzdy se bude vracet ve vzestupnem poradi podle vzdalenosti od bodu vyhledavani.
     * <br>
     *  Vyhledava i podle dalsich kriterii a to CoffeeSort a/nebo CoffeeSiteStatus. Podle hodnot
     *  techto kriterii pak vola spravne repository metody.
     */
    @Override
    public List<CoffeeSiteDTO> findAllWithinCircleWithCSStatusAndCoffeeSort(double zemSirka, double zemDelka, long meters,
                                                                            String cSort, String siteStatus) {       
        List<CoffeeSite> coffeeSites = new ArrayList<CoffeeSite>();
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
            CoffeeSort cf = coffeeSortRepo.searchByName(cSort);
            CoffeeSiteStatus csS =  coffeeSiteStatusRepo.searchByName(siteStatus);
            if ((cf != null) && (csS != null)) {
                coffeeSites = coffeeSiteRepo.findSitesWithCoffeeSortAndSiteStatus(zemSirka, zemDelka, meters, cf, csS, csRS);
            }
        }
        
        if (cSortFilterOnlyFilter) {
            CoffeeSort cfSort = coffeeSortRepo.searchByName(cSort);
            if (cfSort != null) {
                coffeeSites = coffeeSiteRepo.findSitesWithCoffeeSort(zemSirka, zemDelka, meters, cfSort, csRS);
            }
        }
        
        if (csStatusOnlyFilter) {
            CoffeeSiteStatus csS =  coffeeSiteStatusRepo.searchByName(siteStatus);
            if (csS != null) {
                coffeeSites = coffeeSiteRepo.findSitesWithStatus(zemSirka, zemDelka, meters, csS, csRS);
            }
        }
        
        log.info("Coffee sites within circle (Latit.: {} , Long.: {}, range: {}) retrieved: {}", zemSirka, zemDelka, meters, coffeeSites.size());
        return countDistancesAndSortByDist(modifyToTransfer(coffeeSites), zemSirka, zemDelka);
    }
    
    /**
     * TODO - NOT working with CoffeeSite status is null and CoffeeSort is not null. Error in Repository.
     * 
     * @param sirka
     * @param delka
     * @param rangeMeters
     * @param cfSortStr - muze byt prazde nebo null
     * @param siteStatus - muze byt prazde nebo null
     * @param csRecordStatus - muze byt prazde nebo null
     * @param cityName - muze byt prazde nebo null
     */
    @Override
    public List<CoffeeSiteDTO> findAllWithinCircleAndCityWithCSStatusAndCoffeeSort(double zemSirka, double zemDelka, long rangeMeters,
                                                                                   String cfSortStr, String siteStatus, String cityName) {
        
        CoffeeSiteRecordStatus csRS = csRecordStatusRepo.searchByName(CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.ACTIVE.toString());
        CoffeeSort cfSort = coffeeSortRepo.searchByName(cfSortStr);
        CoffeeSiteStatus csStatus =  coffeeSiteStatusRepo.searchByName(siteStatus);
        
        List<CoffeeSite> coffeeSites = new ArrayList<CoffeeSite>();
        coffeeSites = coffeeSiteRepo.findSitesWithSortAndSiteStatusAndRangeAndCity(zemSirka, zemDelka, rangeMeters, cfSort, csStatus, csRS, cityName);
        
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
    public List<CoffeeSiteDTO> findAllByCityNameExactly(String cityName) {
        List<CoffeeSite> items = coffeeSiteRepo.getAllSitesInCityExactly(cityName);
        log.info("All Coffee sites of '{}' city retrieved: {}", cityName, items.size());
        return modifyToTransfer(items);
    }
    
    @Override
    public List<CoffeeSiteDTO> findAllByCityNameAtStart(String cityName) {
        List<CoffeeSite> items = coffeeSiteRepo.getAllSitesInCity(cityName);
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
    private List<CoffeeSiteDTO> countDistancesAndSortByDist(List<CoffeeSiteDTO> sites, double zemSirka, double zemDelka)
    {
        // Vypocet vzdalenosti pro kazdy vraceny CoffeeSite
        for (CoffeeSiteDTO site : sites) {
            site.setDistFromSearchPoint(countDistanceMetersFromSearchPoint(zemSirka, zemDelka, site.getZemSirka(), site.getZemDelka()));
        }     
        
        // Usporadani vysledku db dotazu (seznam CoffeeSites v danem okruhu) podle vzdalenosti od bodu hledani
        // Sama DB tyto vzdalenosti pro dane CoffeeSites nevraci
        sites.sort((cs1, cs2) -> { 
            return (cs1.getDistFromSearchPoint() == cs2.getDistFromSearchPoint()) ? 0
                   : (cs1.getDistFromSearchPoint() < cs2.getDistFromSearchPoint()) ? -1
                      : 1;}
        );
        
        return sites;
    }

    /**
     * Pomocna metoda pro vypocet vzdalenosti mezi 2 body na mape/globu. Souradnice bodu ve formatu double.
     * Prevzato ze stackoverflow.com
     *  
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * 
     * @return vzdalenost 2 zadanych bodu v metrech
     */
    private static long countDistanceMetersFromSearchPoint(double lat1, double lon1, double lat2, double lon2) {
        long eRadius = 6372000; // polomer Zeme v metrech, v CR?
        long distance;       
        double c, a;
  
        double latDist = Math.toRadians( lat2 - lat1 );
        double lonDist = Math.toRadians( lon2 - lon1 );
        a = Math.pow( Math.sin( latDist/2 ), 2 ) + Math.cos( Math.toRadians( lat1 ) ) * Math.cos( Math.toRadians( lat2 ) ) * Math.pow( Math.sin( lonDist / 2 ), 2 );
        c  = 2 * Math.atan2( Math.sqrt( a ), Math.sqrt( 1 - a ) );
      
        distance = Math.round(eRadius * c);       
 
        return distance;
    }

    @Override
    public CoffeeSiteDTO findByName(String siteName) {
        CoffeeSiteDTO site = mapperFacade.map(coffeeSiteRepo.searchByName(siteName), CoffeeSiteDTO.class);
        if (site == null) {
            log.error("Coffee site with name {} not found in DB.",  siteName);
            throw new EntityNotFoundException("Coffee site with name " + siteName + " not found.");
        }
        return site;
    }

   
    /**
     * A method to evaluate, if the CoffeeSite and User are "compatible"
     * i.e. if the user can modify the CoffeeSite.<br>
     * The user must be originator of the Site or has DBA or ADMIN roles
     * @return
     */
    private boolean siteUserMatch(CoffeeSiteDTO cs) {
        if (loggedInUser.isPresent() && cs.getOriginalUserName() != null) {
            return (loggedInUser.get().getUserName().equals(cs.getOriginalUserName()) || userService.hasADMINorDBARole(loggedInUser.get()));
        }
        else {
            return false;
        }
    }
    
    /**
     * CoffeeSite can be modified only if it is in CREATED or INACTIVE states.
     */
    private boolean canBeModified(CoffeeSiteDTO cs) {
        return siteUserMatch(cs) && (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.CREATED)
                                     || cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.INACTIVE));
    }

    /**
     * Position cannot be occupied by any other ACTIVE CoffeeSite before activating.
     * @param cs
     * @return
     */
    private boolean canBeActivated(CoffeeSiteDTO cs) {
        return siteUserMatch(cs) // all authenticated users can modify from Created to Active or Inactive to Active
               && 
               (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.CREATED)
                || cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.INACTIVE)
               );
               //&& !isLocationAlreadyOccupiedByActiveSite(cs.getZemSirka(), cs.getZemDelka(), 5, cs.getId()); 
    }

    private boolean canBeDeactivated(CoffeeSiteDTO cs) {
        return siteUserMatch(cs) // all allowed users modify from Active to Inactive
               &&
               (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.ACTIVE)
                || (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.CANCELED) // Admin or DBA users can modify from CANCELED to INACTIVE or Inactive to Active
                    && userService.hasADMINorDBARole(loggedInUser.get())
                   )    
               );
    }

    /**
     * 
     * @param cs
     * @return
     */
    private boolean canBeCanceled(CoffeeSiteDTO cs) {
        //return siteUserMatch(cs) && !userService.hasDBARole(loggedInUser.get()) // all users allowed to modify are also allowed change status from Inactive to Cancel or from CREATED to Cancel, except those with DBA role
        return siteUserMatch(cs) // all users allowed to modify are also allowed change status from Inactive to Cancel or from CREATED to Cancel
               &&
               (
                  cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.INACTIVE)
                  ||
                  cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.CREATED)
                );
    }

    /**
     * Only ADMIN is allowed to Delete site permanently (from every CoffeeSite state)
     */
    private boolean canBeDeleted(CoffeeSiteDTO cs) {
        return (loggedInUser.isPresent()) ? userService.hasADMINRole(loggedInUser.get()) : false;
    }

    /**
     * Evaluates if a Comment can be added to the CoffeeSite. 
     */
    private boolean canBeCommented(CoffeeSiteDTO cs) {
        return (loggedInUser.isPresent());
    }
    
    /**
     * Evaluates if Stars can be added to the CoffeeSite. 
     */
    private boolean canBeRateByStars(CoffeeSiteDTO cs) {
        return (loggedInUser.isPresent());
    }

    /**
     * Evaluates if the site is to be displayed in UI.
     * For anonymous users, only ACTIVE sites are visible
     * For logged-in user:
     *  If not CANCELED, then visible
     *  If CANCELED then only loggedd-in user with ADMIN or DBA Roles can see the CoffeeSite
     */
    private boolean isVisible(CoffeeSiteDTO cs) {
        if (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.ACTIVE))
            return true;
        else {
            
            if (!cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.CANCELED)) {
                // not ACTIVE site and not CANCELED, logged-in user
                // Logged-in user can see only ACTIVE Sites or only the sites he/she created
                return loggedInUser.isPresent() && siteUserMatch(cs);
                
            } else { // is CANCELED, only DBA and ADMIN can see the site 
                 return (loggedInUser.isPresent()) ? userService.hasADMINorDBARole(loggedInUser.get()) : false;
              }
        }     
    }
    
    /**
     * Based on ImageStorageService evaluates if the CoffeeSite with siteId
     * has saved Image.
     * 
     * @return URL of the CoffeeSite's image if available, otherwise empty String
     */
    @Override
    public String getMainImageURL(CoffeeSiteDTO cs) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        UriComponentsBuilder extBuilder = builder.replacePath(imageService.getBaseImageURLPath()).replaceQuery("");
        String imageURI = extBuilder.build().toUriString() + cs.getId();
        return (imageService.isImageAvailableForSiteId(cs.getId())) ? imageURI : "";
    }

    /**
     * Checks if the coffe site name is already used or not
     */
    @Override
    public boolean isSiteNameUnique(Long id, String siteName) {
        CoffeeSite site = coffeeSiteRepo.searchByName(siteName);
        return ( site == null || ((id != null) && site.getId().equals(id)));
    }

    @Override
    public LatLong getAverageLocation(List<CoffeeSiteDTO> coffeeSites) {
        OptionalDouble avgLat = coffeeSites.stream().mapToDouble(cs -> cs.getZemSirka()).average();
        OptionalDouble avgLong = coffeeSites.stream().mapToDouble(cs -> cs.getZemDelka()).average();
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
     * @param distance - distance in meters of returned "search location" from coffeeSiteDTO's location. Allowed values are from 50 to 5000, otherwise default 500 is set. 
     */
    @Override
    public LatLong getSearchFromLocation(CoffeeSiteDTO coffeeSiteDTO, int distance) {
        
        if (distance < 50 || distance > 5000) {
            distance = 500;
        }
        
        double distanceNaDruhou = distance * distance;
        double distSouth = Math.sqrt(distanceNaDruhou - Math.pow(4/12d * distance, 2));
        double distWest = Math.sqrt(distanceNaDruhou - Math.pow(3/12d * distance, 2));

        long eRadius = 6372000;
        double obvodZeme = 2 * Math.PI * eRadius;
        double stupneNaMetr = 360d / obvodZeme; // one meter on Earth as part of the whole circle degrees (360 deggrees) 
        
        double searchPointLat = coffeeSiteDTO.getZemSirka() - distSouth * stupneNaMetr;
        double searchPointLong = coffeeSiteDTO.getZemDelka() - distWest * stupneNaMetr;
        
        return new LatLong(searchPointLat, searchPointLong);
    }

    @Override
    public void deleteCoffeeSitesFromUser(Long userId) {
        coffeeSiteRepo.deleteAllFromUser(userId);
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
