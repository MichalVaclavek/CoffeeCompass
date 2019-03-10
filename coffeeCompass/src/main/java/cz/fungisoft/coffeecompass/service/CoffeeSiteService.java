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
    public List<CoffeeSiteDTO> findByCityName(String cityName);
    
    public LatLong getAverageLocation(List<CoffeeSiteDTO> coffeeSites);
    
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
     * @param orderBy - podle ktereho atributu se ma poskladat vysledny List - bude to vzdalenost od vyhledavaciho bodu
     * @param direction - ASC nebo DESC - obvykle ASC tj. od nejmensi vzdalenosti
     * @return
     */
    public List<CoffeeSiteDTO> findAllWithinCircle(double zemSirka, double zemDelka, long meters);
     
    
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
