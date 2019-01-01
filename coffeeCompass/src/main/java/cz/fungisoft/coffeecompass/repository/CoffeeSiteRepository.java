package cz.fungisoft.coffeecompass.repository;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Zakladni Repository trida pro CoffeeSite objekt.
 * 
 * Specialni verze vyhledavacich metod, pokud nejsou k dispozici zakladni
 * z org.springframework.data.jpa.repository.JpaRepositor, ale ktere lze zapsat
 * pomoci jednoduchych SELECTu s anotaci @Query
 * <br>
 * Tuto lze rozsirit pomoci dalsich Interfacu a jejich metod, ktere se pak implemetuji v konkretni tride
 * oznacene Repository anotaci. Zde se pak deklaruji a definuji metody pro slozitejsi dotazy napr.
 * pomoci CriteriaQuery.
 * <br>
 * Zde rozsireno o interface CoffeeSiteRepositoryCustom, ktery ma jmeno odvozene od jmena tohoto zakladniho interfacu
 * tj. CoffeeSiteRepository na CoffeeSiteRepositoryCustom
 * 
 */
public interface CoffeeSiteRepository extends JpaRepository<CoffeeSite, Long>, CoffeeSiteRepositoryCustom
{
    @Query("select cs from CoffeeSite cs where siteName=?1")
    public CoffeeSite searchByName(String name);

    @Query(nativeQuery = true, value = "select count(*) from coffee_site") // SQL
    public long countItems();
    
    @Query("select cs from CoffeeSite cs where originalUser.id=?1")
    public List<CoffeeSite> findSitesFromUserID(int userId);
    
    @Query("select cs from CoffeeSite cs where cs.recordStatus.status=?1 order by cs.siteName asc")
    public List<CoffeeSite> findSitesWithRecordStatus(String csRecordStatus);  
     
    @Query("select count(*) from CoffeeSite cs where cs.recordStatus.status='ACTIVE'")
    public Long getNumOfAllActiveSites();
    
    @Query("select count(id) from CoffeeSite cs where date(cs.createdOn) = current_date")
    public Long getNumOfSitesCreatedToday();
    
    @Query("select count(id) from CoffeeSite cs where date(cs.createdOn) > (current_date - 7)")
    public Long getNumOfSitesCreatedLast7Days();
    
    
    /*
     * Dotaz s vyuzitim Postgres FUNCTION, ktera pocita vzdalenost CoffeeSite od souradnic
     * double sirka, double delka a vraci takove CoffeeSite, ktere maji tuto vzdalenost menzi jako rangeMeters:
     * 
     * 1) HQL varianta, nefunguje, tato syntaxe asi neni spravna
     * @Query("SELECT cs FROM CoffeeSite cs WHERE distance(?1, ?2, cs.zemSirka, cs.zemDelka) < ?3") 
     * 
     * 2) Varianta "native" query - funguje     
     * @Query(nativeQuery = true, value = "SELECT *, poloha_gps_sirka, poloha_gps_delka"
     *                                + " FROM coffeecompass.coffee_site WHERE distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3")
     */
    @Query(nativeQuery = true, name = "getSitesWithinRange") // varianta, kdy je Query nadefinovano v jine tride pomoci @NamedNativeQuery anotace, v tomto pripade v CoffeeSite tride
    public List<CoffeeSite> findSitesWithinRange(double sirka, double delka, long rangeMeters);  
    
    /** 
     * Pomocna metoda pro otestovani, ze funguje volani Stored procedure v DB.
     * Stored procedure distance je nadefinovana ve tride CoffeeSite jako @NamedStoredProcedureQuery
     * Jde o variantu s pojmenovanymi parametry (je treba tedy uvadet i v teto metode @Param anotace, ktere odpovidaji)
     * jmenum v definici @NamedStoredProcedureQuery
     * 
     * @param sirkaFrom
     * @param delkaFrom
     * @param sirkaTo
     * @param delkaTo
     * 
     * @return vzdalenost v metrech mezi 2 zadanymi vstupnimi body {(lat1, lon1), (lat2, lon2)}
     */    
    @Procedure(name = "distance")
    public long callStoredProcedureCalculateDistance(@Param("lat1") double sirkaFrom, @Param("lon1") double delkaFrom,
                                                     @Param("lat2") double sirkaTo, @Param("lon2") double delkaTo);
}
