package cz.fungisoft.coffeecompass.service;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.pojo.LatLong;

/**
* Interface CoffeeSiteService. Predepisuje metody pro praci s CoffeeSite objekty, tj.
* zvlaste CRUD operace. Podstatnou casti jsou vyhledavaci metody podle ruznych kriterii.
* 
* @author Michal Václavek
*/
public interface CoffeeSiteService {
    /**
     * Gets all CoffeeSites ordered by selected CoffeeSite's property in given direction
     * 
     * @param orderBy
     * @param direction
     * @return
     */
    List<CoffeeSiteDTO> findAll(String orderBy, String direction);
    Page<CoffeeSiteDTO> findAllPaginated(Pageable pageable);
    
    List<CoffeeSiteDTO> findAllFromUser(User user);
    List<CoffeeSiteDTO> findAllFromUserName(String userName);
    List<CoffeeSiteDTO> findAllFromLoggedInUser();
    Page<CoffeeSiteDTO> findAllFromLoggedInUserPaginated(Pageable pageable);

    /**
     * Same as prevoius method, but filters out CoffeeSites in CANCELLED status
     * Used for REST interface for requests from mobile phone.
     *
     * @param pageable
     * @return
     */
    Page<CoffeeSiteDTO> findAllNotCancelledFromLoggedInUserPaginated(Pageable pageable);
    
    Integer getNumberOfSitesFromUserId(long userId);
    Integer getNumberOfSitesFromLoggedInUser();
    Integer getNumberOfSitesNotCanceledFromUserId(long userId);
    Integer getNumberOfSitesNotCanceledFromLoggedInUser();
    
    Optional<CoffeeSiteDTO> findOneToTransfer(Long siteId);

    /**
     * @param id
     * @return
     */
    Optional<CoffeeSite> findOneById(Long id);
    CoffeeSiteDTO findByName(String siteName);
    
    /**
     * Finds all CoffeeSites, whose "mesto" field is equal to 'cityName' string
     * 
     * @param cityName
     * @return
     */
    List<CoffeeSiteDTO> findAllByCityNameExactly(String cityName);
    
    /**
     * Finds all CoffeeSites, whose "mesto" filed starts by 'cityName' string
     * 
     * @param cityName
     * @return
     */
    List<CoffeeSiteDTO> findAllByCityNameAtStart(String cityName);
    
    /**
     * Counts average longitude and latitude of all List<CoffeeSiteDTO> locations
     * 
     * @param coffeeSites
     * @return
     */
    LatLong getAverageLocation(List<CoffeeSiteDTO> coffeeSites);
    
    /**
     * Gets longitude and latitude of the "search From" point for CoffeeSiteDTO location.
     * Used in case the only one CoffeeSite is to be shown on map and it was not found
     * based on location (but based on city name etc.).
     * The default distance of the returned search point from coffeeSiteDTO's location
     * is 500 m, but the distance can be defined by "distance" parameter, whose
     * values can be from 50 m to 5000 m. Therefore, only simplified method
     * to count coordinates can be used as the curvature of earth is not significant for such short distance.
     * 
     * @param coffeeSiteDTO
     * @param distance
     * @return
     */
    LatLong getSearchFromLocation(CoffeeSiteDTO coffeeSiteDTO, int distance);
    
    /**
     * Returns "numOfSites" number of latest created and ACTIVE CoffeeSites, not older then 60 days (created from now to 60 days before)
     * Used in "welcome" page, in the Statistics and News section
     * 
     * @return
     */
    List<CoffeeSiteDTO> getLatestCoffeeSites(int numOfSites);

    /**
     * Returns CoffeeSites, which were created (and are still ACTIVE) withing the specified amount of days
     * before today. Maximum of days back is 62. If requested days are bigger then 62 or less then 1,
     * 21 will be used instead.<br>
     * Max. number of returned CoffeeSites may be limited to about 100.
     *
     * @param numOfDays number of days from now back for which we are looking for created, ACTIVE CoffeeSites
     * @return CoffeeSites, which were created and are ACTIVE within the specified amount of days
     */
    List<CoffeeSiteDTO> getCoffeeSitesActivatedInLastDays(int numOfDays);

    
    /*
     * Lze zadat mnoho kombinaci vyhledavacich kriterii ... vzdy se take budou vrace jen ty CoffeeSites, ktere jsou casove otevrene
     * a v provozu ... Ma smysl vytvaret metodu, ktera akceptuje vsechny mozna criteria?
     * jako CoffeeSort, CoffeeSiteType, Company, Offer, NextToMachineType, ... 
     */
    
    /**
     * 
     * @param zemSirka
     * @param zemDelka
     * @param rangeMeters
     * @param cfSort
     * @param siteStatus
     * @return
     */
    List<CoffeeSiteDTO> findAllWithinCircleWithCSStatusAndCoffeeSort(double zemSirka, double zemDelka, long rangeMeters,
                                                                            String cfSort, String siteStatus);
    
    List<CoffeeSiteDTO> findAllWithinCircleAndCityWithCSStatusAndCoffeeSort(double zemSirka, double zemDelka, long rangeMeters,
                                                                                   String cfSort, String siteStatus, String cityName);
    
    List<CoffeeSiteDTO> findAllWithinRangeWithRecordStatus(double zemSirka, double zemDelka, long rangeMeters,  CoffeeSiteRecordStatus csRecordStatus);
    
    List<CoffeeSiteDTO> findAllWithRecordStatus(CoffeeSiteRecordStatusEnum csRecordStatus);
    Page<CoffeeSiteDTO> findAllWithRecordStatusPaginated(Pageable pageable, CoffeeSiteRecordStatusEnum csRecordStatus);
    
    /**
     * A method to create a Page of CoffeeSites from given list of CoffeeSites.
     * Usualy used on pages showing CoffeeSites found on location or city criteria, like coffeesite_search.html
     * 
     * @param pageable
     * @param coffeeSitesList
     * @return
     */
    Page<CoffeeSiteDTO> getPageOfCoffeeSitesFromList(Pageable pageable, List<CoffeeSiteDTO> coffeeSitesList);
    
    /**
     * Checks if the CoffeeSite name is already used or not
     * 
     * @param siteId
     * @param siteName
     * @return
     */
    boolean isSiteNameUnique(Long siteId, String siteName);
    
    /**
     * Method, which is called before every request for CoffeeSiteDTO object.
     * The method sets the "operational' attributes of  CoffeeSiteDTO, which
     * determine what operations/transitions between states can be performed with
     * the CoffeeSiteDTO object.<br>
     * Also evaluates, if the CoffeeSite is to be visible in UI and if
     * the CoffeeSite has saved Image assigned.
     * 
     * @param site
     * @return
     */
    CoffeeSiteDTO evaluateOperationalAttributes(CoffeeSiteDTO site);
    
    /**
     * 
     * @param zemSirka - zemepisna sirka bodu od ktereho se ma vyhledatvat
     * @param zemDelka - zemepisna delka bodu od ktereho se ma vyhledatvat
     * @param rangeMeters - vzdalenost v metrech od vyhledavaciho bodu, kde se maji vyhledat CoffeeSites

     * @return
     */
    List<CoffeeSiteDTO> findAllWithinCircle(double zemSirka, double zemDelka, long rangeMeters);
    
    
    /**
     * Checks if there is a CoffeeSites already created within meters circle from 'zemSirka' and 'zemDelka'.<br>
     * Used especially when creating/modifying CoffeeSite to check if it's location is not already occupied by another CoffeeSite.
     * 
     * @param zemSirka - zemepisna sirka bodu od ktereho se ma vyhledatvat (obvykle zem. sirka noveho/modifikovaneho CoffeeSite)
     * @param zemDelka - zemepisna delka bodu od ktereho se ma vyhledatvat (obvykle zem. delka noveho/modifikovaneho CoffeeSite)
     * @param rangeMeters - vzdalenost v metrech od vyhledavaciho bodu, kde se maji vyhledat CoffeeSites
     * @param siteId - id of the CoffeeSites whose location is to be checked

     * @return - true, if the location is already occupied by another CoffeeSite, otherwise false.
     */
    boolean isLocationAlreadyOccupied(double zemSirka, double zemDelka, long rangeMeters, Long siteId);
    
    /**
     * Checks if there is a CoffeeSites already created and in ACTIVE state within meters circle from 'zemSirka' and 'zemDelka'.<br>
     * Used especially when creating/modifying CoffeeSite to check if it's location is not already occupied by another CoffeeSite.
     * 
     * @param zemSirka - zemepisna sirka bodu od ktereho se ma vyhledatvat (obvykle zem. sirka noveho/modifikovaneho CoffeeSite)
     * @param zemDelka - zemepisna delka bodu od ktereho se ma vyhledatvat (obvykle zem. delka noveho/modifikovaneho CoffeeSite)
     * @param rangeMeters - vzdalenost v metrech od vyhledavaciho bodu, kde se maji vyhledat CoffeeSites
     * @param siteId - id of the CoffeeSites whose location is to be checked

     * @return - true, if the location is already occupied by another CoffeeSite, otherwise false.
     */
    boolean isLocationAlreadyOccupiedByActiveSite(double zemSirka, double zemDelka, long rangeMeters, Long siteId);
     
    
    @PreAuthorize("isAuthenticated()")
    CoffeeSite save(CoffeeSite cs);
    
    @PreAuthorize("isAuthenticated()")
    @Transactional
    CoffeeSite save(CoffeeSiteDTO cs);
    
    @PreAuthorize("isAuthenticated()")
    @Transactional
    boolean save(@Valid List<CoffeeSiteDTO> coffeeSites);
    
    /**
     * Ulozeni seznamu novych nebo updatovanych CoffeeSites
     */
    @PreAuthorize("isAuthenticated()")
    @Transactional
    boolean saveOrUpdate(List<CoffeeSiteDTO> coffeeSites);

    /**
     * Ulozeni seznamu novych nebo updatovanych CoffeeSites a vraceni tohoto seznamu zpet (se spravnymi ID
     * pro nove CoffeeSites). If not successfull, returns empty list.
     */
    @PreAuthorize("isAuthenticated()")
    @Transactional
    List<CoffeeSiteDTO> saveOrUpdateWithResult(List<CoffeeSiteDTO> coffeeSites);
    
    @PreAuthorize("isAuthenticated()")
    CoffeeSite updateCSRecordStatusAndSave(CoffeeSite cs, CoffeeSiteRecordStatusEnum newStatus);
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    void delete(Long id);
    
    // Pomocna metoda pro otestovani, ze funguje volani Stored procedure v DB
    double getDistance(double zemSirka1, double zemDelka1, double zemSirka2, double zemDelka2);
    
    /**
     * To obtain complete URL of the image for the requested CoffeeSiteDTO
     * 
     * @return URL of the CoffeeSite's image if available, otherwise empty String
     */
    String getMainImageURL(CoffeeSiteDTO cs);
    
    @PreAuthorize("isAuthenticated()")
    void deleteCoffeeSitesFromUser(Long userId);
    
    @Transactional
    CoffeeSite updateSite(CoffeeSiteDTO cs);
    
}
