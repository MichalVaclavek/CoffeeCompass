package cz.fungisoft.coffeecompass.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;

/**
 * Trida implementujici Custom Repository interface  urcena pro specilani dotazy (napr. pouzivajici StoredProcedure
 * a CriteriaQuery) a pracujici s CoffeSite entitou.
 * <br>
 * Jmeno teto tridy musi byt ve tvaru {jmeno_zakladniho_Repository}Impl pricemz zakladni Repository interface 
 * rozsiruje CoffeeSiteRepositoryCustom.
 * 
 * //TODO - zabudovani Query sitesInRange do dalsich CriteriaQuery ?? Toto pravdepodobne nelze :-(
 * Asi bude potreba udelat jinak ... asi kombinaci klasickych JPQL resp. Hibernate QL dotazu
 * se subselectem pomoci namedquery getSitesWithinRange. Budou jen omezene kombinace pro vyhledavani
 * Zakladem je tedy vysledek getSitesWithinRange a ten by mel byt vstupem do dalsiho SELECT podle: ???
 * 
 * NOTE 
 * Mela by ale jit pouzit varianta CriteriaQuery a "function" neboli ulozena procedura viz
 *  https://vladmihalcea.com/hibernate-sql-function-jpql-criteria-api-query/ 
 * 
 * @author Michal Vaclavek
 *
 */
@Repository
@Transactional
public class CoffeeSiteRepositoryImpl implements CoffeeSiteRepositoryCustom
{
    /**
     * Skoro vSechny vyhledavaci SQL query v teto tride budou mit stejny tvar. Na zacatku SELECT vsech polozek z coffeecompass.coffee_site tab. a na konci
     * omezeni podle geograficke polohy pomoci podminky s ulozenou procedurou. Proto se tento dotaz bude rozdelovat na 3 casti,
     * kde se bude menit akorat stred dotazu s dalsimi podminkami.
     * Zatim nepouzito.
     */
    private static final String SITES_IN_RANGE_QUERY = "SELECT *, poloha_gps_sirka, poloha_gps_delka FROM coffeecompass.coffee_site WHERE distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3";
    private static final String QUERY_START_WHERE = "SELECT *, poloha_gps_sirka, poloha_gps_delka FROM coffeecompass.coffee_site AS cs WHERE ";
    private static final String QUERY_START_JOIN = "SELECT *, poloha_gps_sirka, poloha_gps_delka FROM coffeecompass.coffee_site AS cs JOIN ";
    private static final String QUERY_END = " AND (distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
        
    @PersistenceContext
    private EntityManager em;

    /**
     * Implementuje vyhledani CoffeeSites pomoci ulozene procedury. Bohuzel nefunguje, Hibernate resp.
     * Postgres DB vraci chybu, ktera rika, ze chybi hodnoty nekterych parametru :-( Hibernate z nejakeho
     * duvodu vola proceduru sitesInRange se 6 parametry ?, prestoze je definovana pouze se 3-mi parametry.
     */
    @Override
    public List<CoffeeSite> findSitesWithinRangeByProcedure(double sirka, double delka, long rangeMeters) {        
        StoredProcedureQuery sitesInRange = em.createNamedStoredProcedureQuery("sitesInRangeProcedure");               
        
        sitesInRange.registerStoredProcedureParameter(2, Double.class, ParameterMode.IN);
        sitesInRange.registerStoredProcedureParameter(3, Double.class, ParameterMode.IN);
        sitesInRange.registerStoredProcedureParameter(4, Long.class, ParameterMode.IN);
        
        sitesInRange.setParameter(2, sirka);
        sitesInRange.setParameter(3, delka);
        sitesInRange.setParameter(4, rangeMeters);
        
        return sitesInRange.getResultList();
    }

    /**
     * Pomocna/cvicna implementace metody, ktera je "defaultne" implementovana v CoffeeSiteRepository interfacu pomoci @Query
     * Tato metoda se vola primarne, ikdyz prislusny atribut v CoffeeSite service je deklarovan jako instance CoffeeSiteRepository interfacu
     * Spring pravdepodobne vytvori spravne instanci teto tridy resp. asi vytvori nejakou ...<br>
     * 
     * V dokumentaci je uvedeno:
     * Custom implementations have a higher priority than the base implementation and repository aspects.
     * This ordering lets you override base repository and aspect methods and resolves ambiguity if two fragments
     * contribute the same method signature. 
     * 
     * ale jak je to udelano zde popsano neni
     */
    @Override
    public List<CoffeeSite> findSitesWithinRange(double sirka, double delka, long rangeMeters) {
        Query sitesInRangeQuery = em.createNamedQuery("getSitesWithinRange", CoffeeSite.class);
        
        sitesInRangeQuery.setParameter(1, sirka);
        sitesInRangeQuery.setParameter(2, delka);
        sitesInRangeQuery.setParameter(3, rangeMeters);
        
        // Lze nastavit omezeni poctu vysledku
        sitesInRangeQuery.setFirstResult(0);
        sitesInRangeQuery.setMaxResults(10);
        
        return sitesInRangeQuery.getResultList(); 
    }

    @Override
    public List<CoffeeSite> findSitesWithCoffeeSortAndSiteStatus(double sirka, double delka, long rangeMeters, CoffeeSort sort, CoffeeSiteStatus siteStatus, CoffeeSiteRecordStatus csRecordStatus) {
        String selectQuery = "SELECT *, poloha_gps_sirka, poloha_gps_delka"
                + " FROM coffeecompass.coffee_site AS cs, coffeecompass.coffee_site_to_druhy_kavy AS cs_dk"
                + " JOIN coffeecompass.druhy_kavy AS dk ON dk.id=?5"
                + " WHERE cs_dk.druhy_kavy_id=?5"
                + " AND cs.id=cs_dk.coffee_site_id"
                + " AND status_zarizeni_id=?4"
                + " AND status_zaznamu_id=?6"
                + " AND (distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
                      
        Query sites = em.createNativeQuery(selectQuery, CoffeeSite.class);
        
        sites.setParameter(1, sirka);
        sites.setParameter(2, delka);
        sites.setParameter(3, rangeMeters);
        
        sites.setParameter(4, siteStatus.getId());
        sites.setParameter(5, sort.getId()); 
        sites.setParameter(6, csRecordStatus.getId()); 
        
        return sites.getResultList();
    }
    
    /*
    @Override
    public List<CoffeeSite> findSitesWithCoffeeSortAndSiteStatus(double sirka, double delka, long rangeMeters, CoffeeSort sort, CoffeeSiteStatus siteStatus)
    {
        String selectQuery = "SELECT *, poloha_gps_sirka, poloha_gps_delka"
                + " FROM coffeecompass.coffee_site AS cs, coffeecompass.coffee_site_to_druhy_kavy AS cs_dk"
                + " JOIN coffeecompass.druhy_kavy AS dk ON dk.id=?5"
                + " WHERE cs_dk.druhy_kavy_id=?5 AND cs.id=cs_dk.coffee_site_id AND status_zarizeni_id=?4 AND (distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
                      
        Query sites = em.createNativeQuery(selectQuery, CoffeeSite.class);
        
        sites.setParameter(1, sirka);
        sites.setParameter(2, delka);
        sites.setParameter(3, rangeMeters);
        
        sites.setParameter(4, siteStatus.getId());
        sites.setParameter(5, sort.getId()); 
        
        return sites.getResultList();
    }
   */
    
    /**
     * Implementace metody pro vyhledani CoffeeSites v danem geo. rangi s danym typem kavy - CoffeeSort
     */
    @Override
    public List<CoffeeSite> findSitesWithCoffeeSort(double sirka, double delka, long rangeMeters, CoffeeSort sort, CoffeeSiteRecordStatus csRecordStatus) {

        String selectQuery = "SELECT *, poloha_gps_sirka, poloha_gps_delka"
                + " FROM coffeecompass.coffee_site AS cs, coffeecompass.coffee_site_to_druhy_kavy AS cs_dk"
                + " JOIN coffeecompass.druhy_kavy AS dk ON dk.id=?4"
                + " WHERE cs_dk.druhy_kavy_id=?4 AND cs.id=cs_dk.coffee_site_id"
                + " AND status_zaznamu_id=?5"
                + " AND (distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
                      
        Query sites = em.createNativeQuery(selectQuery, CoffeeSite.class);
        
        sites.setParameter(1, sirka);
        sites.setParameter(2, delka);
        sites.setParameter(3, rangeMeters);
        
        sites.setParameter(4, sort.getId()); 
        sites.setParameter(5, csRecordStatus.getId()); 
        
        return sites.getResultList();
    }
    

    /**
     * Implementace metody pro vyhledani CoffeeSites v danem geo. rangi s danym statusem situ - CoffeeSiteStatus
     */
    @Override
    public List<CoffeeSite> findSitesWithStatus(double sirka, double delka, long rangeMeters, CoffeeSiteStatus siteStatus, CoffeeSiteRecordStatus csRecordStatus) {

        String selectQuery = "SELECT *, poloha_gps_sirka, poloha_gps_delka"
                                + " FROM coffeecompass.coffee_site AS cs"
                                + " WHERE status_zarizeni_id=?4 "
                                + " AND status_zaznamu_id=?5"
                                + " AND (distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
                      
        Query sites = em.createNativeQuery(selectQuery, CoffeeSite.class);
        
        sites.setParameter(1, sirka);
        sites.setParameter(2, delka);
        sites.setParameter(3, rangeMeters);
        
        sites.setParameter(4, siteStatus.getId());
        sites.setParameter(5, csRecordStatus.getId()); 
        
        return sites.getResultList();
    }

    //TODO - vsechny vyse uvedene metody by mely byt provedeny i ve tvaru s parametrem RecordStatus, protoze pro nap. neprihlaseneho usera
    //ma smysl hledat pouze ACTIVE sity. Prihlaseny user pouze ACTIVE a vsechny co sam vytvoril. Pouze pro ADMIN a DB usera se nahravaji 
    // vsechny Record statusy
    /**
     * Implementace metody pro vyhledani CoffeeSites v danem geo. rangi s danym statusem situ - CoffeeSiteStatus
     */
    @Override
    public List<CoffeeSite> findSitesWithRecordStatus(double sirka, double delka, long rangeMeters, CoffeeSiteRecordStatus csRecordStatus) {

        String selectQuery = "SELECT *, poloha_gps_sirka, poloha_gps_delka"
                                + " FROM coffeecompass.coffee_site AS cs"
                                + " WHERE status_zaznamu_id=?4 AND (distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
                      
        Query sites = em.createNativeQuery(selectQuery, CoffeeSite.class);
        
        sites.setParameter(1, sirka);
        sites.setParameter(2, delka);
        sites.setParameter(3, rangeMeters);
        
        sites.setParameter(4, csRecordStatus.getId());
        
        return sites.getResultList();
    }
    
}
