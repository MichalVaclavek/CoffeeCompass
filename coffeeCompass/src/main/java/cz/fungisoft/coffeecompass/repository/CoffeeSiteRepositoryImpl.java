package cz.fungisoft.coffeecompass.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Repository;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;
import lombok.extern.slf4j.Slf4j;

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
 * https://vladmihalcea.com/hibernate-sql-function-jpql-criteria-api-query/ 
 * 
 * @author Michal Vaclavek
 *
 */
@Repository
@Transactional
@Slf4j
public class CoffeeSiteRepositoryImpl implements CoffeeSiteRepositoryCustom {
    /*
     * Skoro vSechny vyhledavaci SQL query v teto tride budou mit stejny tvar. Na zacatku SELECT vsech polozek z coffeecompass.coffee_site tab. a na konci
     * omezeni podle geograficke polohy pomoci podminky s ulozenou procedurou. Proto se tento dotaz bude rozdelovat na 3 casti,
     * kde se bude menit akorat stred dotazu s dalsimi podminkami.
     * Zatim nepouzito.
     */
    private static final String SITES_IN_RANGE_QUERY = "SELECT *, poloha_gps_sirka AS gps_sirka, poloha_gps_delka AS gps_delka FROM coffeecompass.coffee_site WHERE public.distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3";
    private static final String QUERY_START_WHERE = "SELECT *, poloha_gps_sirka AS gps_sirka, poloha_gps_delka AS gps_delka FROM coffeecompass.coffee_site AS cs WHERE ";
    private static final String QUERY_START_JOIN = "SELECT *, poloha_gps_sirka AS gps_sirka, poloha_gps_delka AS gps_delka FROM coffeecompass.coffee_site AS cs JOIN ";
    private static final String QUERY_END = " AND (public.distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
        
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
    
    /**
     * Zakladni varianta vyhledavaciho dotazu pro ziskani seznamu CoffeeSites podle<br>
     * polohy,<br>
     * druhu kavy {@code CoffeeSort},<br>
     * statusu situ {@code CoffeeSiteStatus}<br>
     * a statusu DB zaznam {@code CoffeeSiteRecordStatus}.
     */
    @Override
    public List<CoffeeSite> findSitesWithCoffeeSortAndSiteStatus(double sirka, double delka, long rangeMeters, CoffeeSort sort, CoffeeSiteStatus siteStatus, CoffeeSiteRecordStatus csRecordStatus) {
        String selectQuery = "SELECT *, poloha_gps_sirka AS gps_sirka, poloha_gps_delka AS gps_delka "
                            + "FROM coffeecompass.coffee_site AS cs, coffeecompass.coffee_site_to_druhy_kavy AS cs_dk "
                            + "JOIN coffeecompass.druhy_kavy AS dk ON dk.id=?5 "
                            + "WHERE cs_dk.druhy_kavy_id=?5 "
                            + "AND cs.id=cs_dk.coffee_site_id "
                            + "AND status_zarizeni_id=?4 "
                            + "AND status_zaznamu_id=?6 "
                            + "AND (public.distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
                      
        Query sites = em.createNativeQuery(selectQuery, CoffeeSite.class);
        
        sites.setParameter(1, sirka);
        sites.setParameter(2, delka);
        sites.setParameter(3, rangeMeters);
        
        sites.setParameter(4, siteStatus.getId());
        sites.setParameter(5, sort.getId()); 
        sites.setParameter(6, csRecordStatus.getId()); 
        
        return sites.getResultList();
    }
    
    /**
     * Varianta PREDCHOZIHO ZAKLADNIHO vyhledavaciho dotazu pro ziskani seznamu CoffeeSites podle<br>
     * polohy,<br>
     * druhu kavy {@code CoffeeSort},<br>
     * statusu situ {@code CoffeeSiteStatus}<br>
     * a statusu DB zaznam {@code CoffeeSiteRecordStatus}<br>
     * navic podle jmena mesta. Staci, ze jmeno mesta v tab. obsahuje 'cityName'
     * 
     * @param sirka
     * @param delka
     * @param rangeMeters
//     * @param sort - muze byt prazde nebo null
     * @param siteStatus - muze byt prazde nebo null
     * @param csRecordStatus - muze byt prazde nebo null
     * @param cityName - muze byt prazde nebo null
     * 
     * @return - seznam CoffeeSites vyhovujici zadanym vyhledavacim podminkam
     */ 
    @Override
    public List<CoffeeSite> findSitesWithSortAndSiteStatusAndRangeAndCity(double sirka, double delka, long rangeMeters,
                                                                          CoffeeSiteStatus siteStatus,
                                                                          CoffeeSiteRecordStatus csRecordStatus,
                                                                          String cityName) {
        StringBuilder selectQuery = new StringBuilder();
        
        //TODO This query assembly is not optimal, check!!!
        // POZOR - Nelze vynechavat vstupni parametry!!!
        
        selectQuery.append("SELECT *, poloha_gps_sirka AS gps_sirka, poloha_gps_delka AS gps_delka ");
        
        selectQuery.append("FROM coffeecompass.coffee_site AS cs WHERE ");

        if (csRecordStatus != null && !csRecordStatus.getStatus().isEmpty())
            selectQuery.append("status_zaznamu_id=?1 ");

        if (cityName != null && cityName.length() > 1) {
            selectQuery.append("AND ( (public.distance(?2, ?3, poloha_gps_sirka, poloha_gps_delka) < ?4) OR (LOWER(cs.poloha_mesto) like LOWER(CONCAT(?5,'%')) ))");
            if (siteStatus != null && !siteStatus.getStatus().isEmpty()) {
                selectQuery.append(" AND status_zarizeni_id=?6");
            }
        } else {
            selectQuery.append("AND (public.distance(?2, ?3, poloha_gps_sirka, poloha_gps_delka) < ?4)");
            if (siteStatus != null && !siteStatus.getStatus().isEmpty()) {
                selectQuery.append(" AND status_zarizeni_id=?5");
            }
        }

        Query sites = em.createNativeQuery(selectQuery.toString(), CoffeeSite.class);
        
        if (csRecordStatus != null) {
            sites.setParameter(1, csRecordStatus.getId());
        }
        
        sites.setParameter(2, sirka);
        sites.setParameter(3, delka);
        sites.setParameter(4, rangeMeters);
        
        if (cityName != null && cityName.length() > 1) {
            sites.setParameter(5, cityName);
            if (siteStatus != null) {
                sites.setParameter(6, siteStatus.getId());
            }
        } else if (siteStatus != null) {
                sites.setParameter(5, siteStatus.getId());
        }

        
        return sites.getResultList();
    }
    
    
    /**
     * Implementace metody pro vyhledani CoffeeSites v danem geo. rangi s danym typem kavy - CoffeeSort
     */
    @Override
    public List<CoffeeSite> findSitesWithCoffeeSort(double sirka, double delka, long rangeMeters, CoffeeSort sort, CoffeeSiteRecordStatus csRecordStatus) {

        String selectQuery = "SELECT *, poloha_gps_sirka AS gps_sirka, poloha_gps_delka AS gps_delka "
                          + "FROM coffeecompass.coffee_site AS cs, coffeecompass.coffee_site_to_druhy_kavy AS cs_dk "
                          + "JOIN coffeecompass.druhy_kavy AS dk ON dk.id=?4 "
                          + "WHERE cs_dk.druhy_kavy_id=?4 AND cs.id=cs_dk.coffee_site_id "
                          + "AND status_zaznamu_id=?5 "
                          + "AND (public.distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
                      
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

        String selectQuery = "SELECT *, poloha_gps_sirka AS gps_sirka, poloha_gps_delka AS gps_delka "
                          + "FROM coffeecompass.coffee_site AS cs "
                          + "WHERE status_zarizeni_id=?4 "
                          + "AND status_zaznamu_id=?5 "
                          + "AND (public.distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";
                      
        Query sites = em.createNativeQuery(selectQuery, CoffeeSite.class);
        
        sites.setParameter(1, sirka);
        sites.setParameter(2, delka);
        sites.setParameter(3, rangeMeters);
        
        sites.setParameter(4, siteStatus.getId());
        sites.setParameter(5, csRecordStatus.getId()); 
        
        return sites.getResultList();
    }


    /**
     * Implementace metody pro vyhledani CoffeeSites v danem geo. rangi s danym statusem DB zaznamu {@code CoffeeSiteRecordStatus}
     * @return
     */
    @Override
    public List<CoffeeSite> findSitesWithRecordStatus(double sirka, double delka, long rangeMeters, CoffeeSiteRecordStatus csRecordStatus) {

        String selectQuery = "SELECT *, poloha_gps_sirka AS gps_sirka, poloha_gps_delka AS gps_delka "
                          + "FROM coffeecompass.coffee_site AS cs "
                          + "WHERE status_zaznamu_id=?4 AND (public.distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3)";

        Query sites = em.createNativeQuery(selectQuery, CoffeeSite.class);
        
        sites.setParameter(1, sirka);
        sites.setParameter(2, delka);
        sites.setParameter(3, rangeMeters);
        
        sites.setParameter(4, csRecordStatus.getId());
        
        return sites.getResultList();
    }

    /**
     * Implementace metody pro vyhledani poctu CoffeeSites v seznamu vdalenosti od vyhledavaciho bodu s danym statusem DB zaznamu {@code CoffeeSiteRecordStatus}
     */
    @Override
    public Map<String, Integer> findNumbersOfSitesInGivenDistances(double sirka, double delka, List<Integer> distances, CoffeeSiteStatus siteStatus, CoffeeSiteRecordStatus csRecordStatus) {
        Map<String, Integer> retVal = new HashMap<>();
        for (Integer distance : distances) {
            retVal.put(distance.toString(), findSitesWithStatus(sirka, delka, distance, siteStatus, csRecordStatus).size());
        }
        return retVal;
    }

    @Override
    public Long countNumOfSitesInGivenState(CoffeeSiteRecordStatus csRecordStatus) {
        String selectQuery = "SELECT COUNT(*)"
                          + " FROM coffeecompass.coffee_site AS cs"
                          + " WHERE status_zaznamu_id=?1";
      
        Query sites = em.createNativeQuery(selectQuery, Long.class);
        
        sites.setParameter(1, csRecordStatus.getId());
        
        return (long) sites.getFirstResult();
    }

    
    /**
     * Returns 5 city names with the heighest number of CoffeeSites in ACTIVE state (cs.status_zaznamu_id=1)
     */
    @Override
    public List<DBReturnPair> getTop5CityNames() {
        
        String selectQuery = "SELECT DISTINCT poloha_mesto, COUNT(*) AS NumOfSites"
                          + " FROM coffeecompass.coffee_site AS cs WHERE cs.status_zaznamu_id=1"
                          + " GROUP BY poloha_mesto"
                          + " ORDER BY NumOfSites"
                          + " DESC LIMIT 5";
        
        Query sites = em.createNativeQuery(selectQuery);
        
        List<Object[]> results = sites.getResultList();
        
        log.info("Top 5 cities statistics retrieved.");
        
        return results.stream().filter(rec -> !((String)rec[0]).isEmpty())
                      .map(rec -> new DBReturnPair((String)rec[0], (Long)rec[1]))
                      .toList();
    }
}
