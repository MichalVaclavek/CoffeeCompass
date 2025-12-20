package cz.fungisoft.coffeecompass.repository;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Zakladni Repository trida pro CoffeeSite objekt.
 * 
 * Specialni verze vyhledavacich metod, pokud nejsou k dispozici zakladni<br>
 * z {@code org.springframework.data.jpa.repository.JpaRepository}, ale ktere lze zapsat<br>
 * pomoci jednoduchych SELECTu s anotaci @Query.
 * <p>
 * Tato trida muze rozsirovat dalsi Interface, ktery deklaruje dalsi specialni metody<br>
 * (implementovaner v konkretni tride oznacene Repository anotaci). V tomto interfacu<br>
 * resp. v jeho implementaci se pak deklaruji a definuji metody pro slozitejsi dotazy<br>
 * napr. pomoci CriteriaQuery.
 * <p>
 * Zde rozsiren interface {@code CoffeeSiteRepositoryCustom}, ktery ma jmeno odvozene od jmena tohoto zakladniho interfacu
 * tj. {@code CoffeeSiteRepository} na {@code CoffeeSiteRepositoryCustom}
 * 
 */
public interface CoffeeSiteRepository extends JpaRepository<CoffeeSite, UUID>, CoffeeSiteRepositoryCustom {

    @Query("select cs from CoffeeSite cs where siteName=?1")
    CoffeeSite searchByName(String name);

    @Override
    @NotNull
    List<CoffeeSite> findAll();

    @Override
    @NotNull
    List<CoffeeSite> findAll(@NotNull Sort sort);

    @Query("select cs from CoffeeSite cs where originalUser.id=?1 order by cs.createdOn desc")
    List<CoffeeSite> findSitesFromUserID(UUID userId);

    @Query("select cs from CoffeeSite cs where originalUser.id=?1 and NOT cs.recordStatus.status='CANCELED' order by cs.createdOn desc")
    List<CoffeeSite> findSitesNotCanceledFromUserID(UUID userId);
    
    @Query("select count(id) from CoffeeSite cs where originalUser.id=?1")
    Integer getNumberOfSitesFromUserID(UUID userId);
    
    @Query("select count(id) from CoffeeSite cs where originalUser.id=?1 and NOT cs.recordStatus.status='CANCELED'")
    Integer getNumberOfSitesNotCanceledFromUserID(UUID userId);

    @Query("select cs from CoffeeSite cs where cs.recordStatus.status=?1 order by cs.createdOn desc")
    List<CoffeeSite> findSitesWithRecordStatus(String csRecordStatus);

    /**
     * Returns number of CoffeeSites which will be returned in case of 'Offline mode' download request
     * or when requesting statistics data.
     * @return
     */
    @Query("select count(id) from CoffeeSite cs where cs.recordStatus.status='ACTIVE'")
    Long getNumOfAllActiveSites();

    @Query("select count(id) from CoffeeSite cs where date(cs.createdOn) = current_date")
    Long getNumOfSitesCreatedToday();
    
    @Query("select count(id) from CoffeeSite cs where date(cs.createdOn) = current_date AND cs.recordStatus.status='ACTIVE'")
    Long getNumOfSitesCreatedAndActiveToday();
    
    @Query("select count(id) from CoffeeSite cs where date(cs.createdOn) > (current_date - 7)")
    Long getNumOfSitesCreatedLast7Days();
    
    @Query("select count(id) from CoffeeSite cs where (date(cs.createdOn) > (current_date - 7)) AND cs.recordStatus.status='ACTIVE'")
    Long getNumOfSitesCreatedAndActiveInLast7Days();
    
    /**
     * Retrieves all CoffeeSites with ACTIVE record staus in city which starts with cityName parameter value
     *
     * @param cityName - the name (of city) which is in the beginning of the CoffeeSite.mesto field
     * @return
     */
    @Query("select cs from CoffeeSite cs where lower(cs.mesto) like lower(CONCAT(?1,'%')) AND cs.recordStatus.status='ACTIVE'")
    List<CoffeeSite> getAllActiveSitesInCity(String cityName);
    
    /**
     * Retrieves all CoffeeSites with ACTIVE record staus in given city name.
     *
     * @param cityName - the name of city which is equal to CoffeeSite.mesto field
     * @return
     */
    @Query("select cs from CoffeeSite cs where cs.mesto=?1 AND cs.recordStatus.status='ACTIVE'")
    List<CoffeeSite> getAllSitesInCityExactly(String cityName);
    
    /**
     * 
     * @param maxNumOfLatestSites - max. number of CoffeeSites to be returned by this request
     * @param daysAgo - how many days ago from now is the latest day of CoffeeSite creation 
     * @return
     */
    @Query(nativeQuery = true, value = "SELECT * FROM coffeecompass.coffee_site AS cs WHERE cs.status_zaznamu_uuid=(SELECT external_id FROM coffeecompass.status_coffee_site_zaznamu WHERE status_zaznamu='ACTIVE') AND cs.created_on BETWEEN LOCALTIMESTAMP - ?2 * INTERVAL '1 day' AND LOCALTIMESTAMP ORDER BY cs.created_on DESC LIMIT ?1")
    List<CoffeeSite> getLatestSites(int maxNumOfLatestSites, int daysAgo);
    
    
    /**
     * Dotaz s vyuzitim Postgres FUNCTION, ktera pocita vzdalenost CoffeeSite od souradnic
     * double sirka, double delka a vraci takove CoffeeSite, ktere maji tuto vzdalenost menzi jako rangeMeters:
     * <p>
     * 1) HQL varianta, nefunguje, tato syntaxe asi neni spravna
     * @Query("SELECT cs FROM CoffeeSite cs WHERE public.distance(?1, ?2, cs.zemSirka, cs.zemDelka) < ?3")
     * <p>
     * 2) Varianta "native" query - funguje     
     * @Query(nativeQuery = true, value = "SELECT *, poloha_gps_sirka, poloha_gps_delka"
     *                                + " FROM coffeecompass.coffee_site WHERE public.distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3")
     */
    @Query(nativeQuery = true, name = "getSitesWithinRange") // varianta, kdy je Query nadefinovano v jine tride pomoci @NamedNativeQuery anotace, v tomto pripade v CoffeeSite tride
    List<CoffeeSite> findSitesWithinRange(double sirka, double delka, long rangeMeters);
    
    /**
     * Vrati pocet CoffeeSites v danem okruhu "rangeMeters" od zadanych souradnic "sirka" a "delka".<br>
     * Vyuziva se predevsim pro urceni, zda dana lokace/souradnice neni uz obsazena jinym CoffeeSite.
     *   
     * @param sirka - zemepisna sirka bodu od ktereho se vyhledava
     * @param delka - zemepisna delka od ktereho se vyhledava
     * @param rangeMeters - vzdalenost/okruh v metrech od bodu s polohou  "sirka" a "delka", kde se maji spocitat jiz vytvorene CoffeeSite. Defaultne cca 5m.
     * 
     * @return pocet CoffeeSites v danem okruhu "rangeMeters" od zadanych souradnic.
     */
    @Query(nativeQuery = true, name = "numberOfSitesWithinRange") // varianta, kdy je Query, se jmenem "numberOfSitesWithinRange", nadefinovano v jine tride pomoci @NamedNativeQuery anotace, v tomto pripade v CoffeeSite tride
    Long getNumberOfSitesWithinRange(double sirka, double delka, long rangeMeters);
    
    /**
     * Vrati pocet CoffeeSites v danem okruhu "rangeMeters" od zadanych souradnic "sirka" a "delka".<br>
     * a s danym statusem zaznamu.
     * Vyuziva se predevsim pro urceni, zda dana lokace/souradnice neni uz obsazena jinym CoffeeSite v ACTIVE
     * stavu.
     *   
     * @param sirka - zemepisna sirka bodu od ktereho se vyhledava
     * @param delka - zemepisna delka od ktereho se vyhledava
     * @param rangeMeters - vzdalenost/okruh v metrech od bodu s polohou  "sirka" a "delka", kde se maji spocitat jiz vytvorene CoffeeSite. Defaultne cca 5m.
     * @param recordStatusId - id pozadovaneho statusu, napr. ACTIVE
     * 
     * @return pocet CoffeeSites v danem okruhu "rangeMeters" od zadanych souradnic.
     */
    @Query(nativeQuery = true, name = "numberOfSitesWithinRangeInGivenStatus") // varianta, kdy je Query, se jmenem "numberOfSitesWithinRange", nadefinovano v jine tride pomoci @NamedNativeQuery anotace, v tomto pripade v CoffeeSite tride
    Long getNumberOfSitesWithinRangeInGivenStatus(double sirka, double delka, long rangeMeters, UUID recordStatusId);
    
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
    long callStoredProcedureCalculateDistance(@Param("lat1") double sirkaFrom, @Param("lon1") double delkaFrom,
                                              @Param("lat2") double sirkaTo, @Param("lon2") double delkaTo);

    /**
     * Deletes all CoffeeSites created by User id.
     * 
     * @param userId
     */
    @Modifying // required by Hibernate, otherwise there is an exception ' ... Illegal state ...'
    @Query("delete FROM CoffeeSite cs WHERE originalUser.id=?1")
    void deleteAllFromUser(UUID userId);

    @Modifying
    @Query("delete FROM CoffeeSite cs WHERE id=?1")
    void cancelById(Long Id);
}
