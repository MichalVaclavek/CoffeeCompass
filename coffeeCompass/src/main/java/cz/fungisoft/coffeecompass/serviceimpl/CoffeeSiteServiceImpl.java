package cz.fungisoft.coffeecompass.serviceimpl;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDto;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRecordStatusRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteStatusRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSortRepository;
import cz.fungisoft.coffeecompass.service.CSRecordStatusService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CompanyService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.UserService;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementace CoffeeSiteService. Implementuje vsechny metody, ktere pracuji s CoffeeSite objekty, tj.
 * zvlaste CRUD operace. Podstatnou casti jsou vyhledavaci metody podle ruznych kriterii.
 * 
 * @author Michal Václavek
 *
 */
@Service("coffeeSiteService")
@Transactional
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
    CoffeeSiteRecordStatusRepository csRecordStatusRepo;
    
    private User loggedInUser;
    
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
    
    /**
     * Calls the methods to evaluate attributes used in UI determining allowed operations with CoffeeSite.
     *  
     * @param site
     * @return
     */
    private CoffeeSiteDto evaluateUIAttributes(CoffeeSiteDto site) {
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
    
    private CoffeeSiteDto evaluateAverageStars(CoffeeSiteDto site) {
//        double avgStarsForSite = starsForCoffeeSiteService.avgStarsForSite(site.getId());
        site.setAverageStarsWithNumOfHodnoceni(starsForCoffeeSiteService.getStarsAndNumOfHodnoceniForSite(site.getId()));
//        site.setAverageStars(avgStarsForSite);
        return site;
    }
    
    /**
     * Adds attributes to CoffeeSite to identify what operations can be done with CoffeeSite in UI
     * 
     * @param sites
     * @return
     */
    private List<CoffeeSiteDto> modifyToTransfer(List<CoffeeSite> sites) {
        
        List<CoffeeSiteDto> sitesToTransfer = mapperFacade.mapAsList(sites, CoffeeSiteDto.class);
        
        for (CoffeeSiteDto site : sitesToTransfer) {
            site = evaluateUIAttributes(site);
            site = evaluateAverageStars(site);
        }
        
        return sitesToTransfer;
    }
    
    @Override
    public List<CoffeeSiteDto> findAll(String orderBy, String direction) {
        List<CoffeeSite> items = coffeeSiteRepo.findAll(new Sort(Sort.Direction.fromString(direction.toUpperCase()), orderBy));
        return modifyToTransfer(items);
    }
    
    @Override
    public List<CoffeeSiteDto> findAllWithRecordStatus(CoffeeSiteRecordStatusEnum csRecordStatus) {
        List<CoffeeSite> items = coffeeSiteRepo.findSitesWithRecordStatus(csRecordStatus.getSiteRecordStatus());
        return modifyToTransfer(items);
    }
    
    /**
     *TODO - nikde se nepouziva, nebude lepsi odstranit??
     * 
     * Used to get all CoffeeSites from search point with respective record status. Especially for non logged-in user
     * which can retrieve omly ACTIVE sites.
     */
    @Override
    public List<CoffeeSiteDto> findAllWithinRangeWithRecordStatus(double zemSirka, double zemDelka, long meters, CoffeeSiteRecordStatus csRecordStatus) {
        List<CoffeeSite> items = coffeeSiteRepo.findSitesWithRecordStatus(zemSirka, zemDelka, meters, csRecordStatus);
        return countDistancesAndSortByDist(modifyToTransfer(items), zemSirka, zemDelka);
    }
    
    @Override
    public List<CoffeeSiteDto> findAllFromUser(User user) {
        List<CoffeeSite> items = coffeeSiteRepo.findSitesFromUserID(user.getId());
        return modifyToTransfer(items);
    }
    
    @Override
    public List<CoffeeSiteDto> findAllFromLoggedInUser() {
        loggedInUser = userService.getCurrentLoggedInUser();
        return findAllFromUser(loggedInUser);
    }

    @Override
    public CoffeeSiteDto findOneToTransfer(int id) {
        CoffeeSiteDto site = mapperFacade.map(coffeeSiteRepo.findById(id).orElse(null), CoffeeSiteDto.class);
        
        site = evaluateUIAttributes(site);
        site = evaluateAverageStars(site);
        return site;
    }
    
    @Override
    public CoffeeSite findOneById(int id) {
        return coffeeSiteRepo.findById(id).orElse(null);
    }

    /**
     * Metoda pro provedeni akci pred ulozenim CoffeeSitu a zavolani metody save() z Repository.
     * Tato metoda by mela byt volatelna pouze prihlasenym uzivatelem.
     * 
     * @param - CoffeeSite k ulozeni. Muze jit o novy nebo updatovany CoffeeSite.
     */
    @Override
    public CoffeeSite save(CoffeeSite coffeeSite) {
        loggedInUser = userService.getCurrentLoggedInUser();
        
        if (coffeeSite.getId() == 0) { // Zcela novy CoffeeSite
        
            CoffeeSiteRecordStatus coffeeSiteRecordStatus = csRecordStatusService.findCSRecordStatus(CoffeeSiteRecordStatusEnum.CREATED);
            coffeeSite.setRecordStatus(coffeeSiteRecordStatus);
            if (loggedInUser != null) {   
                coffeeSite.setOriginalUser(loggedInUser);
                loggedInUser.setCreatedSites(loggedInUser.getCreatedSites() + 1);
            }
        } else { // modifikace stavajiciho CoffeeSitu
            updateSite(coffeeSite);
        }
            
        // Zjisteni, jestli Company je nove nebo ne
        Company comp = companyService.findCompanyByName(coffeeSite.getDodavatelPodnik().toString());
        
        if (comp == null) {
            comp = companyService.saveCompany(coffeeSite.getDodavatelPodnik().toString());
        }
        coffeeSite.setDodavatelPodnik(comp);
        
        return coffeeSiteRepo.save(coffeeSite);
    }
    
    /**
     * Ulozeni modifikovaneho CoffeeSite.
     * 
     * @param coffeeSite
     */
    private void updateSite(CoffeeSite coffeeSite)
    {
        CoffeeSite entityFromDB = coffeeSiteRepo.findById(coffeeSite.getId()).orElse(null);
        
        if (entityFromDB != null) {
            loggedInUser = userService.getCurrentLoggedInUser();
            
            entityFromDB.setUpdatedOn(new Timestamp(new Date().getTime()));
            if (loggedInUser != null) {
                loggedInUser.setUpdatedSites(loggedInUser.getUpdatedSites() + 1);
                entityFromDB.setLastEditUser(loggedInUser);
            }
            
            entityFromDB.setCena(coffeeSite.getCena());
            
            for (CoffeeSort cs : coffeeSite.getCoffeeSorts()) {
                entityFromDB.getCoffeeSorts().add(cs);
            }            
            for (CupType cp : coffeeSite.getCupTypes()) {
                entityFromDB.getCupTypes().add(cp);
            }
            for (NextToMachineType ntmt : coffeeSite.getNextToMachineTypes()) {
                entityFromDB.getNextToMachineTypes().add(ntmt);
            }
            for (OtherOffer oo : coffeeSite.getOtherOffers()) {
                entityFromDB.getOtherOffers().add(oo);
            }
            
            Company comp = companyService.findCompanyByName(coffeeSite.getDodavatelPodnik().toString());
            
            if (comp == null) {
                comp = companyService.saveCompany(coffeeSite.getDodavatelPodnik().toString());
            }
            entityFromDB.setDodavatelPodnik(comp);
           
            entityFromDB.setMesto(coffeeSite.getMesto());           
            entityFromDB.setNumOfCoffeeAutomatyVedleSebe(coffeeSite.getNumOfCoffeeAutomatyVedleSebe());           
            entityFromDB.setPristupnostDny(coffeeSite.getPristupnostDny());
            entityFromDB.setPristupnostHod(coffeeSite.getPristupnostHod());
            entityFromDB.setSiteName(coffeeSite.getSiteName());
            entityFromDB.setStatusZarizeni(coffeeSite.getStatusZarizeni());
            entityFromDB.setTypLokality(coffeeSite.getTypLokality());
            entityFromDB.setUliceCP(coffeeSite.getUliceCP());
            entityFromDB.setZemDelka(coffeeSite.getZemDelka());
            entityFromDB.setZemSirka(coffeeSite.getZemSirka());
            entityFromDB.setRecordStatus(coffeeSite.getRecordStatus());
            
            entityFromDB.setOriginalUser(coffeeSite.getOriginalUser());            
        }
    }
    
    @Override
    public CoffeeSite save(CoffeeSiteDto cs) {
        CoffeeSite csToSave = mapperFacade.map(cs, CoffeeSite.class);
        // Insert original user, which was removed during maping from CoffeeSite to CoffeeSiteDto when sending to client
        User origUser = userService.findByUserName(cs.getOriginalUserName());
        csToSave.setOriginalUser(origUser);
 
        return save(csToSave);
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
    public void delete(int id) {
        coffeeSiteRepo.deleteById(id);
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
    public List<CoffeeSiteDto> findAllWithinCircle(double zemSirka, double zemDelka, long meters) {       
       List<CoffeeSite> coffeeSites = coffeeSiteRepo.findSitesWithinRange(zemSirka, zemDelka, meters);
       return countDistancesAndSortByDist(modifyToTransfer(coffeeSites), zemSirka, zemDelka);
    }
    
    /**
     * Najde vsechny CoffeeSites v okruhu meters od polohy zemSirka a zemDelka.
     * Vzdy se bude vracet ve vzestupnem poradi podle vzdalenosti od bodu vyhledavani.
     * <br>
     *  Vyhledava i podle dalsich kriterii a to CoffeeSort a/nebo CoffeeSiteStatus. Podle hodnot
     *  techto kriterii pak vola spravne repository metody.
     */
    @Override
    public List<CoffeeSiteDto> findAllWithinCircleWithCSStatusAndCoffeeSort(double zemSirka, double zemDelka, long meters,
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
        CoffeeSiteRecordStatus csR = csRecordStatusRepo.searchByName(CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.ACTIVE.toString());

        if (noFilter)
            coffeeSites = coffeeSiteRepo.findSitesWithRecordStatus(zemSirka, zemDelka, meters, csR);
        
        if (cSortAndcsStatusFilter) {
            CoffeeSort cf = coffeeSortRepo.searchByName(cSort);
            CoffeeSiteStatus csS =  coffeeSiteStatusRepo.searchByName(siteStatus);
            coffeeSites = coffeeSiteRepo.findSitesWithCoffeeSortAndSiteStatus(zemSirka, zemDelka, meters, cf, csS, csR);
        }
        
        if (cSortFilterOnlyFilter) {
            CoffeeSort cfSort = coffeeSortRepo.searchByName(cSort);
            coffeeSites = coffeeSiteRepo.findSitesWithCoffeeSort(zemSirka, zemDelka, meters, cfSort, csR);
        }
        
        if (csStatusOnlyFilter) {
            CoffeeSiteStatus csS =  coffeeSiteStatusRepo.searchByName(siteStatus);
            coffeeSites = coffeeSiteRepo.findSitesWithStatus(zemSirka, zemDelka, meters, csS, csR);
        }
        
        return countDistancesAndSortByDist(modifyToTransfer(coffeeSites), zemSirka, zemDelka);
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
    private List<CoffeeSiteDto> countDistancesAndSortByDist(List<CoffeeSiteDto> sites, double zemSirka, double zemDelka)
    {
        // Vypocet vzdalenosti pro kazdy vraceny CoffeeSite
        for (CoffeeSiteDto site : sites) {
            site.setDistFromSearchPoint(countDistanceMetersFromSearchPoint(zemSirka, zemDelka, site.getZemSirka(), site.getZemDelka()));
        }     
        
        // Usporadani vysledku db dotazu (seznam CoffeeSites v danem okruhu) podle vzdalenosti od bodu hledani
        // Sama DB tyto vzdalenosti pro dane CoffeeSites nevraci
        sites.sort((cs1, cs2) -> {
            if (cs1.getDistFromSearchPoint() == cs2.getDistFromSearchPoint()) {
                return 0;
            }
            return (cs1.getDistFromSearchPoint() < cs2.getDistFromSearchPoint()) ? -1 : 1;
        });
        
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
    public CoffeeSiteDto findByName(String siteName) {
        CoffeeSiteDto site = mapperFacade.map(coffeeSiteRepo.searchByName(siteName), CoffeeSiteDto.class);
        return site;
    }

    @Override
    public List<CoffeeSiteDto> findByCityName(String cityName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CoffeeSiteDto> findByCityAndStreetNames(String cityName, String streetName) {
        // TODO
        return null;
    }

    /**
     * A method to evaluate, if the CoffeeSite and User are "compatible"
     * i.e. if the user can modify the CoffeeSite.<br>
     * The user must be originator of the Site or has DBA or ADMIN roles
     * @return
     */
    private boolean siteUserMatch(CoffeeSiteDto cs) {
        loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser != null
               && cs.getOriginalUserName() != null)
            return (loggedInUser.getUserName().equals(cs.getOriginalUserName())
                       || userService.hasADMINorDBARole(loggedInUser)
                    );
        else
            return false;
    }
    
    /**
     * CoffeeSite can be modified only if it is in CREATED or INACTIVE statuses.
     */
    @Override
    public boolean canBeModified(CoffeeSiteDto cs) {
        return siteUserMatch(cs) && (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.CREATED)
                                     || cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.INACTIVE));
    }

    @Override
    public boolean canBeActivated(CoffeeSiteDto cs) {
        return siteUserMatch(cs) // all authenticated users can modify from Created to Active or Inactive to Active
                && 
                (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.CREATED)
                 || cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.INACTIVE)
                      
                );    
    }

    @Override
    public boolean canBeDeactivated(CoffeeSiteDto cs) {
        return siteUserMatch(cs) // all allowed users modify from Active to Inactive
                &&
                (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.ACTIVE)
                  || (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.CANCELED) // Admin or DBA users can modify from CANCELED to INACTIVE or Inactive to Active
                         && userService.hasADMINorDBARole(loggedInUser)
                     )    
                );
    }

    @Override
    public boolean canBeCanceled(CoffeeSiteDto cs) {
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
    @Override
    public boolean canBeDeleted(CoffeeSiteDto cs) {
        loggedInUser = userService.getCurrentLoggedInUser();
        
        return (loggedInUser != null) ? userService.hasADMINRole(loggedInUser) : false;
    }

    /**
     * Evaluates if a Comment can be added to the CoffeeSite. 
     */
    @Override
    public boolean canBeCommented(CoffeeSiteDto cs) {
        loggedInUser = userService.getCurrentLoggedInUser();
        return (loggedInUser != null) ? true : false;
    }
    
    /**
     * Evaluates if Stars can be added to the CoffeeSite. 
     */
    @Override
    public boolean canBeRateByStars(CoffeeSiteDto cs) {
        loggedInUser = userService.getCurrentLoggedInUser();
        return (loggedInUser != null) ? true : false;
    }

    /**
     * Evaluates if the site is to be displayed in UI.
     * For anonymous users, only ACTIVE sites are visible
     * For logged-in user:
     *  If not CANCELED, then visible
     *  If CANCELED then only loggedd-in user with ADMIN or DBA Roles can see the CoffeeSite
     */
    @Override
    public boolean isVisible(CoffeeSiteDto cs) {
        if (cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.ACTIVE))
            return true;
        else {
            loggedInUser = userService.getCurrentLoggedInUser();
            
            if (!cs.getRecordStatus().getRecordStatus().equals(CoffeeSiteRecordStatusEnum.CANCELED)) {
                // not ACTIVE site and not CANCELED, logged-in user
                // Logged-in user can see only ACTIVE Sites or only the sites he/she created
                return loggedInUser != null && siteUserMatch(cs);
                
            } else { // is CANCELED, only DBA and ADMIN can see the site 
                 return (loggedInUser == null) ? false : userService.hasADMINorDBARole(loggedInUser);
              }
        }     
    }

    @Override
    public boolean isSiteNameUnique(Integer id, String siteName) {
        CoffeeSite site = coffeeSiteRepo.searchByName(siteName);
        return ( site == null || ((id != null) && (site.getId() == id)));
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
