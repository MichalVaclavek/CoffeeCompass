package cz.fungisoft.coffeecompass.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hlavni model objekt aplikace. Obsahuje vsechny potrebne informace o "Coffee situ".<br>
 * Atributy obsahuji anotace pro Hibernate.<br>
 * <p>
 * Prvni Stored procedure je jen pro otestovani, ze funguje volani stored procedure v DB.<br>
 * Druha Stored procedure se zatim nepouziva. Pro realne pripady je pouzita implementace metody<br>
 * {@link cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository#findSitesWithSortAndSiteStatusAndRangeAndCity(double, double , long ,CoffeeSort, CoffeeSiteStatus, CoffeeSiteRecordStatus, String)},
 * ktera pracuje i s dalsimi vstup. parametry, jako napr. coffeesite_record_status 
 * 
 * @author Michal Vaclavek
 */
@Data
@Entity
@javax.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="coffee_site", schema="coffeecompass")
@NamedStoredProcedureQueries({   
    @NamedStoredProcedureQuery(
            name = "distance", 
            procedureName = "distance", 
            parameters = { 
                @StoredProcedureParameter(mode = ParameterMode.IN, type = Double.class, name = "lat1"), 
                @StoredProcedureParameter(mode = ParameterMode.IN, type = Double.class, name = "lon1"),
                @StoredProcedureParameter(mode = ParameterMode.IN, type = Double.class, name = "lat2"), 
                @StoredProcedureParameter(mode = ParameterMode.IN, type = Double.class, name = "lon2"),
                @StoredProcedureParameter(mode = ParameterMode.OUT, type = Double.class, name = "dist")
            }
        ),
    @NamedStoredProcedureQuery(
            name = "sitesInRangeProcedure", 
            procedureName = "sitesWithinRange", 
            resultClasses = CoffeeSite.class, 
            parameters = { 
                @StoredProcedureParameter(mode = ParameterMode.REF_CURSOR, type = void.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, type = Double.class), 
                @StoredProcedureParameter(mode = ParameterMode.IN, type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, type = Long.class), 
            }
        )
 })

/** 
 * Jde o variantu definice SQL dotazu pro zpracovani JPA Springem. Na jmeno "getSitesWithinRange" esp. "numberOfSitesWithinRange" se pak lze odkazovat 
 * pomoci @Query(nativeQuery = true, name = "getSitesWithinRange") v napr. Repository tride.<br>
 * Vysledek tohoto Query by melo byt mozne pouzit jako vstup do dalsich dotazu, Repository metod, ktere filtruji podle dalsich kriterii
 * jako napr. oteviraci doba nebo nabidka kavy, celkova nabidka apod.
 * <br>
 * Mela by ale jit pouzit varianta CriteriaQuery a "function" neboli ulozena procedura viz
 * https://vladmihalcea.com/hibernate-sql-function-jpql-criteria-api-query/ 
 */
@NamedNativeQueries({
    @NamedNativeQuery(
            name = "getSitesWithinRange",
            query = "SELECT *, poloha_gps_sirka, poloha_gps_delka" + 
                              " FROM coffeecompass.coffee_site" +
                              " WHERE distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3",
            resultClass = CoffeeSite.class
    ),
    @NamedNativeQuery( // Counts number of already created CoffeeSites on selected location within defined meters range from the location  
            name = "numberOfSitesWithinRange",
            query = "SELECT COUNT(*) AS cnt FROM (SELECT id, poloha_gps_sirka, poloha_gps_delka FROM coffeecompass.coffee_site WHERE distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3) AS items", 
            resultSetMapping = "LongResult"
    ),
    @NamedNativeQuery( // Counts number of already created CoffeeSites in given status on selected location within defined meters range from the location. 
            name = "numberOfSitesWithinRangeInGivenStatus",
            query = "SELECT COUNT(*) AS cnt FROM (SELECT id, status_zaznamu_id, poloha_gps_sirka, poloha_gps_delka FROM coffeecompass.coffee_site WHERE distance(?1, ?2, poloha_gps_sirka, poloha_gps_delka) < ?3) AS items WHERE status_zaznamu_id=?4", 
            resultSetMapping = "LongResult"
    )
})
/**
 * Result mapping for "numberOfSitesWithinRange" named query.
 */
@SqlResultSetMapping(name="LongResult", columns={@ColumnResult(name="cnt", type = Long.class)})
public class CoffeeSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 3, max = 50)
    @Column(name = "site_name")
    private String siteName;

    @NotNull
    @Column(name="created_on", nullable = false)
    private Timestamp createdOn;
    
    @Column(name="updated_on")
    private Timestamp updatedOn;
    
    @Column(name="canceled_on")
    private Timestamp canceledOn;
    
    @Column(name = "poloha_gps_delka")
    private double zemDelka;

    @Column(name = "poloha_gps_sirka")
    private double zemSirka;
           
    @Column(name = "poloha_mesto")
    private String mesto;

    @Column(name = "poloha_ulice_a_cp")
    private String uliceCP;
    
    @Column(name = "casova_pristupnost_dny")
    private String pristupnostDny;
    
    @Column(name = "casova_pristupnost_hodiny")
    private String pristupnostHod;
    
    @Column(name = "komentar_autora")
    @Size(max=240)
    private String initialComment;
    
    @Column(name = "pocet_kavovych_automatu_vedle_sebe")
    private int numOfCoffeeAutomatyVedleSebe;
    
    /**
     * To indicate, that push notification about this CoffeeSite first activation was already sent.
     * To avoid repeating notifications, when the CoffeeSite is deactivated and activated again. 
     */
    @Column(name = "create_activate_notification_sent")
    private boolean newSitePushNotificationSent = false;
    
    /* **** MANY TO ONE relations **** */
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "status_zaznamu_id")
    private CoffeeSiteRecordStatus recordStatus;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "zadal_user_id")
    private User originalUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "naposledy_upravil_user_id")
    private User lastEditUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "smazal_user_id")
    private User canceledUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "typ_podniku_id")
    private CoffeeSiteType typPodniku;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "status_zarizeni_id")
    private CoffeeSiteStatus statusZarizeni;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "dodavatel_jmeno_podniku_id")
    private Company dodavatelPodnik;  
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "cena_id")
    private PriceRange cena;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "typ_lokality_id")
    private SiteLocationType typLokality;
    
    /* **** MANY TO MANY relations **** */

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(fetch= FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(name = "coffee_site_to_typ_kelimku", schema="coffeecompass",
                   joinColumns = { @JoinColumn(name = "coffee_site_id") }, 
                      inverseJoinColumns = { @JoinColumn(name = "typ_kelimku_id") })
    private Set<CupType> cupTypes = new HashSet<>();

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(fetch= FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(name = "coffee_site_to_nabidka", schema="coffeecompass",
                   joinColumns = { @JoinColumn(name = "id_mainsitetab") }, 
                      inverseJoinColumns = { @JoinColumn(name = "id_nabidka") })
    private Set<OtherOffer> otherOffers = new HashSet<>();

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(fetch= FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(name = "coffee_site_to_dalsi_automat_vedle", schema="coffeecompass",
                    joinColumns = { @JoinColumn(name = "id_mainsitetab") }, 
                       inverseJoinColumns = { @JoinColumn(name = "id_dalsi_automat") })
    private Set<NextToMachineType> nextToMachineTypes = new HashSet<>();

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(fetch= FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(name = "coffee_site_to_druhy_kavy", schema="coffeecompass",
                    joinColumns = { @JoinColumn(name = "coffee_site_id") }, 
                       inverseJoinColumns = { @JoinColumn(name = "druhy_kavy_id") })
    private Set<CoffeeSort> coffeeSorts = new HashSet<>();      
    
    /* **** ONE TO MANY relations **** */
    /**
     * Odkaz do tabulky Entity Commnent, spojeno pomoci coffeeSite atributu v tab./entite Comment
     */
    /* Pravdepodobne neni potreba uvadet zde, zkusime doplnit servisni vrstvou Comment a najit, jen kdyz
     * si uzivatel zobrazi informace o jednom CoffeeSite.
     * Nebude se tedy ziskavat cely seznam commentu pri kazdem dotazu na CoffeeSite.???
     * */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy="coffeeSite", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy="coffeeSite", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<StarsForCoffeeSiteAndUser> ratings = new ArrayList<>();
}
