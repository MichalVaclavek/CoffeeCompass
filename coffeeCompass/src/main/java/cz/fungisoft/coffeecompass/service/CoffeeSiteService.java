package cz.fungisoft.coffeecompass.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDto;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.User;

/**
* Interface CoffeeSiteService. Predepisuje metody pro praci s CoffeeSite objekty, tj.
* zvlaste CRUD operace. Podstatnou casti jsou vyhledavaci metody podle ryznych kriterii.
* 
* @author Michal Václavek
*/
public interface CoffeeSiteService
{
    public List<CoffeeSiteDto> findAll(String orderBy, String direction);
    public List<CoffeeSiteDto> findAllFromUser(User user);
    public List<CoffeeSiteDto> findAllFromLoggedInUser();
    
    public CoffeeSiteDto findOneToTransfer(Long siteId);
    public CoffeeSite findOneById(Long id);
    public CoffeeSiteDto findByName(String siteName);
    public List<CoffeeSiteDto> findByCityName(String cityName);
    public List<CoffeeSiteDto> findByCityAndStreetNames(String cityName, String streetName);
    
    public boolean isSiteNameUnique(Long siteId, String siteName);
    
    /**
     * Methods to evaluate if the CoffeSite can be modified according its current status
     * and according loggedin user type.
     * 
     * @param cs
     * @return
     */
    public boolean canBeModified(CoffeeSiteDto cs);
    public boolean canBeActivated(CoffeeSiteDto cs);
    public boolean canBeDeactivated(CoffeeSiteDto cs);
    public boolean canBeCanceled(CoffeeSiteDto cs);
    public boolean canBeDeleted(CoffeeSiteDto cs);
    public boolean canBeCommented(CoffeeSiteDto cs);
    public boolean canBeRateByStars(CoffeeSiteDto cs);
    public boolean isVisible(CoffeeSiteDto cs);
    
    /**
     * 
     * @param zemSirka - zemepisna sirka bodu od ktereho se ma vyhledatvat
     * @param zemDelka - zemepisna delka bodu od ktereho se ma vyhledatvat
     * @param meters - vzdalenost v metrech od vyhledavaciho bodu, kde se maji vyhledat CoffeeSites
     * @param orderBy - podle ktereho atributu se ma poskladat vysledny List - bude to vzdalenost od vyhledavaciho bodu
     * @param direction - ASC nebo DESC - obvykle ASC tj. od nejmensi vzdalenosti
     * @return
     */
    public List<CoffeeSiteDto> findAllWithinCircle(double zemSirka, double zemDelka, long meters);
     
    
    @PreAuthorize("isAuthenticated()")
    public CoffeeSite save(CoffeeSite cs);
    @PreAuthorize("isAuthenticated()")
    public CoffeeSite save(CoffeeSiteDto cs);
    @PreAuthorize("isAuthenticated()")
    public CoffeeSite updateCSRecordStatusAndSave(CoffeeSite cs, CoffeeSiteRecordStatusEnum newStatus);
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(Long id);
    
    // Pomocna metoda pro otestovani, ze funguje volani Stored procedure v DB
    public double getDistance(double zemSirka1, double zemDelka1, double zemSirka2, double zemDelka2);
    
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
    public List<CoffeeSiteDto> findAllWithinCircleWithCSStatusAndCoffeeSort(double zemSirka, double zemDelka, long meters,
                                                                            String cfSort, String siteStatus);
    public List<CoffeeSiteDto> findAllWithinRangeWithRecordStatus(double zemSirka, double zemDelka, long meters,  CoffeeSiteRecordStatus csRecordStatus);
    
    public List<CoffeeSiteDto> findAllWithRecordStatus(CoffeeSiteRecordStatusEnum csRecordStatus);
}
