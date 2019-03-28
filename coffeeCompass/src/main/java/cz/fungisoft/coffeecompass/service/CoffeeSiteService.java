package cz.fungisoft.coffeecompass.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

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
public interface CoffeeSiteService
{
    public List<CoffeeSiteDTO> findAll(String orderBy, String direction);
    public List<CoffeeSiteDTO> findAllFromUser(User user);
    public List<CoffeeSiteDTO> findAllFromLoggedInUser();
    
    public CoffeeSiteDTO findOneToTransfer(Long siteId);
    public CoffeeSite findOneById(Long id);
    public CoffeeSiteDTO findByName(String siteName);
    
    /**
     * Finds all CoffeeSites, whose "mesto" field is equal to 'cityName' string
     * 
     * @param cityName
     * @return
     */
    public List<CoffeeSiteDTO> findAllByCityNameExactly(String cityName);
    
    /**
     * Finds all CoffeeSites, whose "mesto" filed starts by 'cityName' string
     * 
     * @param cityName
     * @return
     */
    public List<CoffeeSiteDTO> findAllByCityNameAtStart(String cityName);
    
    /**
     * Counts average longitude and latitude of all List<CoffeeSiteDTO> locations
     * 
     * @param coffeeSites
     * @return
     */
    public LatLong getAverageLocation(List<CoffeeSiteDTO> coffeeSites);
    
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
    public LatLong getSearchFromLocation(CoffeeSiteDTO coffeeSiteDTO, int distance);
    
    /**
     * Returns "numOfSites" number of latest created and ACTIVE CoffeeSites, not older then 60 days (created from now to 60 days before)
     * Used in "welcome" page, in the Statistics and News section
     * 
     * @return
     */
    public List<CoffeeSiteDTO> getLatestCoffeeSites(int numOfSites);
    
    /*
     * Lze zadat mnoho kombinaci vyhledavacich kriterii ... vzdy se take budou vrace jen ty CoffeeSites, ktere jsou casove otevrene
     * a v provozu ... Ma smysl vytvaret metodu, ktera akceptuje vsechny mozna criteria?
     * jako CoffeeSort, CoffeeSiteType, Company, Offer, NextToMachineType, ... 
     */
    
    /**
     * 
     * @param zemSirka
     * @param zemDelka
     * @param meters
     * @param cfSort
     * @param siteStatus
     * @return
     */
    public List<CoffeeSiteDTO> findAllWithinCircleWithCSStatusAndCoffeeSort(double zemSirka, double zemDelka, long meters,
                                                                            String cfSort, String siteStatus);
    public List<CoffeeSiteDTO> findAllWithinRangeWithRecordStatus(double zemSirka, double zemDelka, long meters,  CoffeeSiteRecordStatus csRecordStatus);
    public List<CoffeeSiteDTO> findAllWithRecordStatus(CoffeeSiteRecordStatusEnum csRecordStatus);
    
    /**
     * Checks if the CoffeeSite name is already used or not
     * 
     * @param siteId
     * @param siteName
     * @return
     */
    public boolean isSiteNameUnique(Long siteId, String siteName);
    
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
    public CoffeeSiteDTO evaluateOperationalAttributes(CoffeeSiteDTO site);
    
    /**
     * 
     * @param zemSirka - zemepisna sirka bodu od ktereho se ma vyhledatvat
     * @param zemDelka - zemepisna delka bodu od ktereho se ma vyhledatvat
     * @param meters - vzdalenost v metrech od vyhledavaciho bodu, kde se maji vyhledat CoffeeSites

     * @return
     */
    public List<CoffeeSiteDTO> findAllWithinCircle(double zemSirka, double zemDelka, long meters);
    
    
    /**
     * Checks if thre is a CoffeeSites already created within meters circle from 'zemSirka' and 'zemDelka'.<br>
     * Used especially when creating a new CoffeeSite to check if it's location is not already occupied by another CoffeeSite.
     * 
     * @param zemSirka - zemepisna sirka bodu od ktereho se ma vyhledatvat
     * @param zemDelka - zemepisna delka bodu od ktereho se ma vyhledatvat
     * @param meters - vzdalenost v metrech od vyhledavaciho bodu, kde se maji vyhledat CoffeeSites

     * @return - true, if the location is already occupied by another CoffeeSite, otherwise false.
     */
    public boolean isLocationAlreadyOccupied(double zemSirka, double zemDelka, long meters);
     
    
    @PreAuthorize("isAuthenticated()")
    public CoffeeSite save(CoffeeSite cs);
    @PreAuthorize("isAuthenticated()")
    public CoffeeSite save(CoffeeSiteDTO cs);
    @PreAuthorize("isAuthenticated()")
    public CoffeeSite updateCSRecordStatusAndSave(CoffeeSite cs, CoffeeSiteRecordStatusEnum newStatus);
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(Long id);
    
    // Pomocna metoda pro otestovani, ze funguje volani Stored procedure v DB
    public double getDistance(double zemSirka1, double zemDelka1, double zemSirka2, double zemDelka2);
    
}
