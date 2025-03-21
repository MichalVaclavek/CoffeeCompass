package cz.fungisoft.coffeecompass.unittest.coffeesite;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus.CoffeeSiteStatusEnum;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CupType.CupTypeEnum;
import cz.fungisoft.coffeecompass.entity.NextToMachineType.NextToMachineTypeEnum;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteAttributesDBSaver;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testy pro CRUD/Repository operace s CoffeeSite objekty.
 * 
 * Lze spustit pomoci aktivni anotace @DataJpaTest a atributu TestEntityManager - automaticky nakonfigureuje vytvoreni a pripojeni DB H2 in-mem DB
 * nebo pomoci aktivni anotace @ActiveProfiles("test") pro aktivaci konfigurace v souboru src/test/resources/application-test.properties,
 * kde je nadefinovane pripojeni do H2 in-mem DB. souboru, ktera se spusti automaticky?. Dale je potreba mit aktivni
 * anotaci @Transactional a atribut EntityManager s anotaci @PersistenceContext.<br>
 * V properties souboru je nadefinovano i "default schema" cimz se mysli soubor s priponou .sql, ktery se ma spustit
 * pri aktivaci JPA.<br>
 * Takto lze nadefinovat i pripojeni do jine DB, zmenou @ActiveProfiles("name") a jinymi hodnotami v application-name.properties
 * a jinym "defaultni" sql script souborem .sql<br>   
 * V mem pripade mam pripravenu i konfiguraci pro HSQLDB v application-testhsql.properties resp. v schema_procedure.sql
 * ale tato varianta nezafunguje, protoze Spring si stezuje na to, ze v HQSLDB scriptu (soubor schema_procedure.sql)
 * je nejaka chyba "unexpected end of statement - required: ;" Bohuzel ani podle navodu na netu se mi nepodarilo tuto variantu
 * (se strored procedure vytvorenou scriptem v HSQLDB) rozchodit. Volani procedury tedy bude otestovano jinak,
 * podle navodu na netu ... <br>
 * HSQLDB musi byt spustena manualne prislusnym scriptem, u mne v E:\Programming\Spring\skoleni-spring\hsqldb-eshop\bin\runDatabaseCoffeeCompass.bat
 *  
 * @author Michal Vaclavek
 */

// Automaticky vytvori propojeni na H2 in-memory DB, ktera je uvedena v pom.xml dependency a nakonfigurovana v /src/test/resources/application.properties
// to vse asi pomoci TestEntityManager
@DataJpaTest
@ActiveProfiles("test") // pro HSQL db pouzit @ActiveProfiles("testhsql")
//@SqlConfig(separator=org.springframework.jdbc.datasource.init.ScriptUtils.EOF_STATEMENT_SEPARATOR)
@ExtendWith(SpringExtension.class)
class CoffeeSiteRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;
    
    /*
    @PersistenceContext
    private EntityManager entityManager;
    */
    @Autowired
    private CoffeeSiteRepository coffeeRepos;
    
    public CoffeeSiteAttributesDBSaver attribSaver;
    
    /**
     * Tyto atributy se musi vytvorit a ulozit do DB pred praci s CoffeeSite, ktery na tyto atributy 
     * odkazuje a pouziva, ale ktere nejsou zatim v testovaci DB ulozeny.
     */
    private final PriceRange pr = new PriceRange();
    private final Set<CoffeeSort> csorts = new HashSet<>();
    private final Set<CupType> cups = new HashSet<>();
    private final Company comp = new Company();
//    private StarsQualityDescription stars = new StarsQualityDescription();
    private double stars;
    private final Set<NextToMachineType> ntmtSet = new HashSet<>();
    private final Set<OtherOffer> nabidka = new HashSet<>();
    private final SiteLocationType nadr = new SiteLocationType();
    private final User origUser = new User();
    private final CoffeeSiteStatus siteStatus = new CoffeeSiteStatus();
    private CoffeeSiteType siteType;
    private final CoffeeSiteRecordStatus recordStatus = new CoffeeSiteRecordStatus();
    
    /**
     * Trida CoffeeSite obsahuje odkazy na nekolik dalsich trid/objektu ktere jsou taky
     * ulozeny v DB. Jak se spravne pripravuje prostredi pro tyto pripady? Vytvorenim novych
     * objektu v @Before setUp() pomoci EntityManageru.persist() a flush()
     * Vsechno se vytvari v testovaci in-memory DB, takze se neovlivni "ziva" DB.
     */
    @BeforeEach
    public void setUp() {
        attribSaver = new CoffeeSiteAttributesDBSaver(entityManager);
        
        // Inicializace objektů, na které se odkazuje CoffeeSite
        pr.setPriceRange("15 - 25 Kč"); 
        
        siteType = new CoffeeSiteType();
        siteType.setCoffeeSiteType("automat");
               
        CoffeeSort cs = new CoffeeSort();
        cs.setCoffeeSort("Instantní");       
        csorts.add(cs);        
        
        CupType paper = new CupType();
        paper.setCupType(CupTypeEnum.PAPER.getCupType());       
        CupType plastic = new CupType();
        plastic.setCupType(CupTypeEnum.PLASTIC.getCupType());       
        cups.add(paper);
        cups.add(plastic);
        
        comp.setNameOfCompany("Kávička s.r.o");
        
        NextToMachineType mt = new NextToMachineType();
        NextToMachineType mt2 = new NextToMachineType();
        mt.setType(NextToMachineTypeEnum.NAPOJE.getNexToMachineType());
        mt2.setType(NextToMachineTypeEnum.BAGETY.getNexToMachineType());
        
        ntmtSet.add(mt);
        ntmtSet.add(mt2);
       
        OtherOffer caj = new OtherOffer();
        caj.setOffer("čaj");
        OtherOffer kafe = new OtherOffer();
        kafe.setOffer("káva");
                      
        nabidka.add(caj);
        nabidka.add(kafe);
        
        nadr.setLocationType("nádraží");
        
        UserProfile userProfUser = new UserProfile();
        userProfUser.setType("USER");
        
        origUser.setUserName("kava");
        origUser.setFirstName("Pan");
        origUser.setLastName("Tchibo");
        
        String emailAddr = "kava@tchibo.de";
        origUser.setEmail(emailAddr);
        origUser.setPassword("kofein");
        origUser.setCreatedOn(LocalDateTime.now());
        
        Set<UserProfile> userProfiles = new HashSet<UserProfile>();
        userProfiles.add(userProfUser);
        origUser.setUserProfiles(userProfiles);   
        
        siteStatus.setStatus(CoffeeSiteStatusEnum.INSERVICE.getSiteStatus());

    }
 
    @Transactional
	@Test
	void whenFindByName_thenReturnCoffeeSite() {
	    
	    CoffeeSite newCS = new CoffeeSite();
	    
	    newCS.setSiteName("tišnov1");
	    newCS.setStatusZarizeni(siteStatus);
	    newCS.setCena(pr);
	    newCS.setCoffeeSorts(csorts);
	    newCS.setCupTypes(cups);
	    newCS.setInitialComment("Ujde");
	    
	    newCS.setTypPodniku(siteType);
	    newCS.setNextToMachineTypes(ntmtSet);
	    newCS.setOtherOffers(nabidka);
	    newCS.setOriginalUser(origUser);
	    newCS.setDodavatelPodnik(comp);
	    newCS.setTypLokality(nadr);
	    
	    newCS.setRecordStatus(recordStatus);
	    newCS.setCreatedOn(LocalDateTime.now());
        
	    newCS.setMesto("Tišnov");
	    newCS.setUliceCP("U nádraží");
        
	    newCS.setNumOfCoffeeAutomatyVedleSebe(2);
        
	    newCS.setPristupnostDny("Po-Ne");      
	    newCS.setPristupnostHod("00-24");
        
	    newCS.setZemDelka(14.434331);
	    newCS.setZemSirka(50.083484);
        // Ulozeni objektu, na ktere se odkazuje CoffeeSite a ktere maji vlastni tab. v DB        
	    attribSaver.saveCoffeeSiteAtributesToDB(newCS);
	    
        entityManager.persist(newCS);
        entityManager.flush();
     
        // when
        CoffeeSite found = coffeeRepos.searchByName("tišnov1");
     
        // then
        assertThat(found.getSiteName(), is(newCS.getSiteName()));
        
        assertThat(found.getTypPodniku(), is(newCS.getTypPodniku()));
        
        assertThat(found.getZemDelka(), is(newCS.getZemDelka()));
        
        assertThat(found.getZemSirka(), is(newCS.getZemSirka()));
        
        assertThat(found.getMesto(), is(newCS.getMesto()));
        assertThat(found.getUliceCP(), is(newCS.getUliceCP()));
        
        assertThat(found.getPristupnostDny(), is(newCS.getPristupnostDny()));
        assertThat(found.getPristupnostHod(), is(newCS.getPristupnostHod()));
        
        assertThat(found.getTypLokality(), is(newCS.getTypLokality()));
        
        assertThat(found.getNumOfCoffeeAutomatyVedleSebe(), is(newCS.getNumOfCoffeeAutomatyVedleSebe()));
        
        assertThat(found.getDodavatelPodnik(), is(newCS.getDodavatelPodnik()));
        
        assertThat(found.getOriginalUser(), is(newCS.getOriginalUser()));
        assertThat(found.getOriginalUser().getUserName(), is(newCS.getOriginalUser().getUserName()));
        
        assertThat(found.getCena().getPriceRange(), is(newCS.getCena().getPriceRange()));
        
        assertThat(found.getCreatedOn(), is(newCS.getCreatedOn()));
        
        assertThat(found.getRecordStatus(), is(newCS.getRecordStatus()));

        assertThat(found.getStatusZarizeni().getStatus(), is(newCS.getStatusZarizeni().getStatus()));
       
        assertThat(found.getInitialComment(), is(newCS.getInitialComment()));
        
        // Only Apache Commons CollectionUtils is able to compare my sets properly 
        assertThat(CollectionUtils.isEqualCollection(found.getCoffeeSorts(), newCS.getCoffeeSorts()), is(true));
        
        assertThat(CollectionUtils.isEqualCollection(found.getOtherOffers(), newCS.getOtherOffers()), is(true));
        
        assertThat(CollectionUtils.isEqualCollection(found.getNextToMachineTypes(), newCS.getNextToMachineTypes()), is(true));

        assertThat(CollectionUtils.isEqualCollection(found.getCupTypes(), newCS.getCupTypes()), is(true));
	}
    
    
    @Disabled
    @Test
    void whenUpdated_thenReturnUpdatedCoffeeSite() {
        
    }
    
    @Disabled
    @Test
    void whenDeleted_thenCoffeeSiteNotReturned() {
        
    }
    
    
    	
    @Disabled
    @Test
    void test_Stored_Procedure_Call() {
	    double distance = 0;
	    distance = coffeeRepos.callStoredProcedureCalculateDistance(50.1256, 14.123, 50.2356, 14.236);
	    
	    assertTrue(distance > 0);
    }
    
    //TODO update of the CoffeeSite in DB
    
    //TODO delete CoffeeSite in DB
}
