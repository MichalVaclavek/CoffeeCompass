package cz.fungisoft.coffeecompass.integrationtests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus.CoffeeSiteStatusEnum;
import cz.fungisoft.coffeecompass.entity.CupType.CupTypeEnum;
import cz.fungisoft.coffeecompass.entity.NextToMachineType.NextToMachineTypeEnum;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRecordStatusRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteStatusRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteTypeRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSortRepository;
import cz.fungisoft.coffeecompass.repository.CompanyRepository;
import cz.fungisoft.coffeecompass.repository.CupTypeRepository;
import cz.fungisoft.coffeecompass.repository.NextToMachineTypeRepository;
import cz.fungisoft.coffeecompass.repository.OfferRepository;
import cz.fungisoft.coffeecompass.repository.PriceRangeRepository;
import cz.fungisoft.coffeecompass.repository.SiteLocationTypeRepository;
import cz.fungisoft.coffeecompass.repository.StarsForCoffeeSiteAndUserRepository;
import cz.fungisoft.coffeecompass.repository.StarsQualityDescriptionRepository;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;


/**
 * Base class to setup configuration of the Integration tests.
 * Includes start of PosgreSQL DB in container and creation of
 * DB tables.
 * <p>
 * Database creation and connection is done via {@code PostgreSQLContainer}
 * which runs Postgres DB in Docker container. Therefore, the tests requier
 * running Docker on test machine.
 * <p>
 * The database structure/schema is created using {@code @Sql(scripts= {"/schema_integration_test_docker.sql"} }
 * annotation. If data are required for running the test, it has to be inserted using Repository
 * methods in  @Before public void setUp() method.
 * 
 * @author Michal Vaclavek
 *
 */
@ContextConfiguration(initializers = {IntegrationTestBaseConfig.Initializer.class})
//if runnig without testContainer
//@Sql(scripts= {"/schema_integration_test.sql", "/data_integration_tests.sql"},
//     config = @SqlConfig(encoding = "utf-8", transactionMode = TransactionMode.ISOLATED)) 
@Testcontainers // Junit5 support for DB in Docker container 
public class IntegrationTestBaseConfig {

    @Autowired
    protected UserProfileRepository userProfileRepo;

    protected static Set<UserProfile> userProfilesUser = new HashSet<>();

    protected static Set<UserProfile> userProfilesADMIN = new HashSet<>();

    protected static Set<UserProfile> userProfilesDBA = new HashSet<>();
    
    @Autowired
    private SiteLocationTypeRepository locationTypeRepo;
    
    @Autowired
    private  CupTypeRepository cupTypeRepo;
    
    @Autowired
    private OfferRepository otherOfferRepo;
    
    @Autowired
    private NextToMachineTypeRepository ntmTypeRepo;
    
    @Autowired
    private CompanyRepository companyRepo;
    
    @Autowired
    private CoffeeSortRepository coffeeSortRepo;
    
    @Autowired
    private CoffeeSiteStatusRepository coffeeSiteStatusRepo;
    
    @Autowired
    private StarsForCoffeeSiteAndUserRepository starsForCoffeeSiteAndUserRepo;
    
    @Autowired
    private StarsQualityDescriptionRepository starsQualityDescriptionRepo;
    
    @Autowired
    private CoffeeSiteRecordStatusRepository csRecordStatusRepo;
    
    @Autowired
    private CoffeeSiteTypeRepository csTypeRepo;
    
    @Autowired
    private PriceRangeRepository priceRangeRepo;
    
    
    protected CoffeeSiteRecordStatus ACTIVE;
    protected CoffeeSiteRecordStatus INACTIVE;
    protected CoffeeSiteRecordStatus CANCELED;
    protected CoffeeSiteRecordStatus CREATED;
    
    Company comp = new Company();
    
    @Before
    public void startDB() {
        postgres.start();
    }
    
    /**
     * Init of DB. Creates basic User PROFILEs
     */
    @BeforeEach
    public void setUp() {
        // USER profile
        UserProfile userProfUser = userProfileRepo.searchByType("ROLE_USER");
        // ADMIN profile
        UserProfile userProfADMIN = userProfileRepo.searchByType("ROLE_ADMIN");
        // DBA profile
        UserProfile userProfDBA = userProfileRepo.searchByType("ROLE_DBA");
        
        ACTIVE = csRecordStatusRepo.searchByName("ACTIVE");
        INACTIVE = csRecordStatusRepo.searchByName("INACTIVE");
        CANCELED = csRecordStatusRepo.searchByName("CANCELED");
        CREATED = csRecordStatusRepo.searchByName("CREATED");
        
        userProfilesUser.add(userProfUser);
        userProfilesADMIN.add(userProfADMIN);
        userProfilesDBA.add(userProfDBA);
        comp.setNameOfCompany("Kávička s.r.o");
        companyRepo.save(comp);
    }


    /**
     * Creates CoffeeSite with all CoffeeSites's attributes, which are also saved in DB,
     * are loaded from DB.<br>
     * Used for tests using real DB reading/writing, like Integration tests.
     * 
     * @param siteName
     * @param coffeeSiteType
     * @return
     */
    protected CoffeeSite getCoffeeSiteBasedOnDB(String siteName, String coffeeSiteType) {
        Set<CoffeeSort> csorts = new HashSet<>();
        Set<CupType> cups = new HashSet<>();
        
        //StarsQualityDescription stars = starsQualityDescriptionRepo.searchByName(StarsQualityEnum.TWO.toString());
        Set<NextToMachineType> ntmtSet = new HashSet<>(); 
        Set<OtherOffer> nabidka = new HashSet<>();
        
        CoffeeSiteType siteType = csTypeRepo.searchByName(coffeeSiteType);
        
        CoffeeSiteRecordStatus recordStatus = csRecordStatusRepo.searchByName(CoffeeSiteRecordStatusEnum.CREATED.getSiteRecordStatus());
        
        PriceRange pr = priceRangeRepo.searchByName("15 - 25 Kč");
          
        CoffeeSort cs = coffeeSortRepo.searchByName(CoffeeSort.CoffeeSortEnum.INSTANT.getCoffeeType());
        csorts.add(cs);        
          
        CupType paper = cupTypeRepo.searchByName(CupTypeEnum.PAPER.getCupType());
        CupType plastic = cupTypeRepo.searchByName(CupTypeEnum.PLASTIC.getCupType());
        
        cups.add(paper);
        cups.add(plastic);
          
        NextToMachineType mt = ntmTypeRepo.searchByName(NextToMachineTypeEnum.NAPOJE.getNexToMachineType());
        NextToMachineType mt2 = ntmTypeRepo.searchByName(NextToMachineTypeEnum.BAGETY.getNexToMachineType());
          
        ntmtSet.add(mt);
        ntmtSet.add(mt2);
         
        
        OtherOffer caj = otherOfferRepo.searchByName("čaj");
        OtherOffer kafe = otherOfferRepo.searchByName("káva");
        nabidka.add(caj);
        nabidka.add(kafe);
          
        SiteLocationType nadr = locationTypeRepo.searchByName("nádraží");
        
        CoffeeSiteStatus siteStatus = coffeeSiteStatusRepo.searchByName(CoffeeSiteStatusEnum.INSERVICE.getSiteStatus());
        
        CoffeeSite coffeeS = new CoffeeSite();        
        
        coffeeS.setSiteName(siteName);
        coffeeS.setStatusZarizeni(siteStatus);
        coffeeS.setCena(pr);
        coffeeS.setCoffeeSorts(csorts);
        coffeeS.setCupTypes(cups);
        coffeeS.setInitialComment("Ujde");
        coffeeS.setTypPodniku(siteType);
        coffeeS.setNextToMachineTypes(ntmtSet);
        coffeeS.setOtherOffers(nabidka);
        coffeeS.setDodavatelPodnik(comp);
        coffeeS.setTypLokality(nadr);
        
        coffeeS.setRecordStatus(recordStatus);
        coffeeS.setCreatedOn(LocalDateTime.now());
        
        coffeeS.setMesto("Tišnov");
        coffeeS.setUliceCP("Nádražní");
        
        coffeeS.setNumOfCoffeeAutomatyVedleSebe(1);
        
        coffeeS.setPristupnostDny("Po-Ne");      
        coffeeS.setPristupnostHod("00-24");
        
        coffeeS.setZemDelka(14.434331);
        coffeeS.setZemSirka(50.083484);
        
        return coffeeS;
    }
    
    /**
     * Vlozeni zakladnich parametru Postgres DB bezici v Dockeru.
     */
    @Container
    public static PostgreSQLContainer postgres = (PostgreSQLContainer) new PostgreSQLContainer("postgres")
                                                        .withDatabaseName("coffeecompass")
                                                        .withUsername("postgres")
                                                        .withInitScript("schema_integration_test_docker.sql")
                                                        .withPassword("postgres_test");

    
    /**
     * Provede nastaveni Spring/Hibernate promennych pro pripojeni do DB bezici v Docker containeru.
     * Provede se pred vytvorenim instance testovaci tridy.
     * 
     * @author Michal V.
     *
     */
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                "spring.jpa.show-sql=true",
                "spring.datasource.hikari.maximum-pool-size=10",
                "spring.jpa.properties.hibernate.format_sql=true",
                "spring.datasource.initialization-mode=always"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
    
    /**
     * Performs REST user login and returns access token for that user/login.
     * 
     * @param mockMvc
     * @param userDto
     * @return
     * @throws Exception
     */
    protected String loginUserAndGetAccessToken(MockMvc mockMvc, SignUpAndLoginRESTDto userDto) throws Exception {
        ResultActions result = mockMvc.perform(post("/rest/public/user/login").contentType(MediaType.APPLICATION_JSON)
                   .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(userDto)))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType("application/json"));
     
        String resultString = result.andReturn().getResponse().getContentAsString();
     
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("accessToken").toString();
    }
}
