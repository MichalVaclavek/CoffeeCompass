package cz.fungisoft.coffeecompass.repository;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;

/**
 * Pomocny interface pro definici metod, ktera umozni volat interni Stored procedure, ktera vraci {@code ResultList},
 * pomoci {@code JpaRepository}. {@code JpaRepository} ma problem s mapovanim parametru pro NamedStoredProceduresQuery.<br>
 * Umoznuje dodatecnou deklaraci a posleze definici i dalsich slozitejsich metod, ktere napr. vyuzivaji {@code CriteriaQuery}.
 * <p>
 * Vytvoreno podle https://stackoverflow.com/questions/46786528/error-in-namedstoredprocedurequery-in-spring-jpa-found-named-stored-procedure/46942745
 * a podle dokumentace na strankach spring.io projektu Spring.
 * <br> 
 * Jmeno interfacu vychazi ze zakladniho {@link CoffeeSiteRepository} doplnenim o slovo Custom.
 *  
 * @author Michal Vaclavek
 */
public interface CoffeeSiteRepositoryCustom 
{    
   /**
    * Vyhledani vsech CoffeeSites od bodu sirka, delka v rozmezi meters pomoci ulozene DB procedury.
    * 
    * @param sirka
    * @param delka
    * @param rangeMeters
    * @return
    */
    public List<CoffeeSite> findSitesWithinRangeByProcedure(double sirka, double delka, long rangeMeters);   
   
    /**
     * Vyhledani vsech CoffeeSites od bodu sirka, delka v rozmezi meters pomoci NamedQuery a EntityManageru.
     *   
     * @param sirka
     * @param delka
     * @param rangeMeters
     * @return
     */
    public List<CoffeeSite> findSitesWithinRange(double sirka, double delka, long rangeMeters);  
    
    /**
     * Vyhledani vsech CoffeeSites od bodu sirka, delka v rozmezi meters a s typem kavy,
     * vse s pomoci NamedQuery a/nebo EntityManageru v prislusne implementaci.
     *  
     * @param sirka
     * @param delka
     * @param rangeMeters
     *
     * @return
     */
    public List<CoffeeSite> findSitesWithStatus(double sirka, double delka, long rangeMeters, CoffeeSiteStatus siteStatus, CoffeeSiteRecordStatus csRecordStatus);

    public List<CoffeeSite> findSitesWithCoffeeSort(double sirka, double delka, long rangeMeters, CoffeeSort sort, CoffeeSiteRecordStatus csRecordStatus);

    public List<CoffeeSite> findSitesWithRecordStatus(double sirka, double delka, long rangeMeters, CoffeeSiteRecordStatus csRecordStatus);

    public List<CoffeeSite> findSitesWithCoffeeSortAndSiteStatus(double sirka, double delka, long rangeMeters, CoffeeSort sort,
                                                                 CoffeeSiteStatus siteStatus, CoffeeSiteRecordStatus csRecordStatus);
    /**
     * Varianta zakladniho dotazu, kdy se k zakladnim vyhledavacim parametrum, jako je poloha, pridava jeste vyhledavani podle:
     *  {@link CoffeeSort},
     *  {@link CoffeeSiteStatus},
     *  {@link CoffeeSiteRecordStatus}.<br>
     * Vyuzito pro vyhledavani v mape, kdy uzivatel zada vyhledavani podle mesta. Z mapy.cz API se ziskaji souradnice tohoto mesta
     * a lze pak podle souradnic najit i ty CoffeeSite zaznamy, ktere nemaji vyplneno pole 'mesto' nebo je toto pole zadane spatne.
     *  
     * @param sirka
     * @param delka
     * @param rangeMeters
     * @param sort
     * @param siteStatus
     * @param csRecordStatus
     * @param cityName
     *
     * @return
     */
    public List<CoffeeSite> findSitesWithSortAndSiteStatusAndRangeAndCity(double sirka, double delka, long rangeMeters,
                                                                          CoffeeSort sort,
                                                                          CoffeeSiteStatus siteStatus,
                                                                          CoffeeSiteRecordStatus csRecordStatus,
                                                                          String cityName);
    
    public Long countNumOfSitesInGivenState(CoffeeSiteRecordStatus csRecordStatus);
    
    /**
     * Gets 5 cities with most ACTIVE CoffeeSites
     * 
     * @return
     */
     public List<DBReturnPair> getTop5CityNames();
}
