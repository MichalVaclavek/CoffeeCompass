package cz.fungisoft.coffeecompass.unittest.coffeesite;


import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import cz.fungisoft.coffeecompass.configuration.ConfigProperties;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus.CoffeeSiteStatusEnum;
import cz.fungisoft.coffeecompass.entity.CupType.CupTypeEnum;
import cz.fungisoft.coffeecompass.entity.NextToMachineType.NextToMachineTypeEnum;
import cz.fungisoft.coffeecompass.repository.CoffeeSitePageableRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRecordStatusRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteStatusRepository;
import cz.fungisoft.coffeecompass.repository.CoffeeSortRepository;
import cz.fungisoft.coffeecompass.repository.PasswordResetTokenRepository;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.repository.UserEmailVerificationTokenRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.security.IAuthenticationFacade;
import cz.fungisoft.coffeecompass.service.CSRecordStatusService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CompanyService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;
import cz.fungisoft.coffeecompass.service.user.UserSecurityService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteServiceImpl;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteBuilder;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Testuje Service vrstvu pro praci s objekty CoffeeSite.
 * 
 * @author Michal Vaclavek
 *
 */
@ExtendWith(SpringExtension.class)
class CoffeeSiteServiceImplTest
{
    
    @MockBean
    private CoffeeSiteRepository coffeeSiteRepository;
    
    @MockBean
    private static CoffeeSiteRecordStatusRepository coffeeSiteRecordStatusRepository;
    
    @MockBean
    private static CoffeeSitePageableRepository coffeeSitePageableRepository;
    
    @MockBean
    private static CompanyService compService;
    
    @MockBean
    private static CSRecordStatusService csRecordStatusService;
    
    @MockBean
    private static IStarsForCoffeeSiteAndUserService starsForCoffeeSiteAndUserService;
    
    @MockBean
    private static ImageStorageService imageStorageService; 
    
    @MockBean
    private static UserSecurityService userSecurityService;

    @MockBean
    private static UserEmailVerificationTokenRepository userEmailVerificationTokenRepository;

    @MockBean
    private static PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private MapperFacade mapperFacade;
    
    
    @TestConfiguration
    static class UserSiteServiceImplTestContextConfiguration {
        
        @MockBean
        public static UserProfileRepository userProfileRepository;

        @MockBean
        public static IAuthenticationFacade authenticationFacade;
        
        @MockBean
        public static ConfigProperties configProps;
 
        @MockBean
        private CustomUserDetailsService userDetailService;
        
        private PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        
        @MockBean
        public static UsersRepository usersRepository;
        
        
        @Bean
        public MapperFacade getMapperFacade() {
            MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
            
            // Only userName is needed for CoffeeSiteDto object
            mapperFactory.classMap(CoffeeSite.class, CoffeeSiteDTO.class)
                         .field("originalUser.userName", "originalUserName")
                         .field("lastEditUser.userName", "lastEditUserName")
                         .byDefault()
                         .register();
            
            return mapperFactory.getMapperFacade();
        }
    }
    
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private CoffeeSiteStatusRepository coffeeSiteStatusRepository;
    
    @MockBean
    private CoffeeSortRepository coffeeSortRepository;
    
    // Object to be tested
    private CoffeeSiteService coffeeSiteService;
    
    
    // Nutne pro kontrolu CoffeeSiteDto, ktere obvykle vraci CoffeeSiteService
    // pro porovnani s pripravenym testovacim CoffeeSite coffeeS
    private CoffeeSite coffeeS;
    
    private String testSiteName = "Test5";
    private String siteTypeName = "bistro";
    
    private CoffeeSiteStatusEnum siteStatE = CoffeeSiteStatusEnum.INSERVICE;
    private CoffeeSiteRecordStatusEnum recStatE = CoffeeSiteRecordStatusEnum.ACTIVE;
    
    private String hodnoceni = "Ujde";
    //private double stars = 2.6d;
    private String cena = "15 - 20 Kč";
    
    private NextToMachineTypeEnum ntmt1 = NextToMachineTypeEnum.NAPOJE;
    private NextToMachineTypeEnum ntmt2 = NextToMachineTypeEnum.BAGETY;
            
    private CupTypeEnum cup1 = CupTypeEnum.PAPER;
    private CupTypeEnum cup2 = CupTypeEnum.PLASTIC;
    
    private String sort1 = "Instantní";
    private String sort2 = "Espresso";

    private String compName = "Kávička s.r.o";
    
    private String offer1 = "čaj";
    private String offer2 = "káva";
    
    private String locType = "nádraží";
    private String mesto = "Praha";
    private String ulice = "Krásná";
    private String dny = "Po-Ne";
    private String hod = "0-24";
    private double zemDelka = 14.434331;
    private double zemSirka = 50.434331;
    
    private int numOfCoffeeMachines = 2;
       
    private User origUser = new User();
    
    
    /**
     * Setup of test. Creates CoffeeSite fixture prepared to be returned when mocking Service layer,
     * including User object as an author of the CoffeeSite.
     */
    @BeforeEach
    public void setUp()
    {
        coffeeSiteService = new CoffeeSiteServiceImpl(coffeeSiteRepository, coffeeSortRepository, coffeeSiteStatusRepository, mapperFacade);

        
        // Priprava uzivatele, ktery CoffeeSite zalozil - origUser
        UserProfile userProfUser = new UserProfile();
        userProfUser.setType("USER");
          
        origUser.setUserName("kava");
        origUser.setFirstName("Pan");
        origUser.setLastName("Tchibo");
          
        String emailAddr = "kava@tchibo.de";
        origUser.setEmail(emailAddr);
        origUser.setPassword("kofein");
        origUser.setCreatedOn(new Timestamp(new Date().getTime()));
          
        Set<UserProfile> userProfiles = new HashSet<UserProfile>();
        userProfiles.add(userProfUser);
        origUser.setUserProfiles(userProfiles);   
        
        CoffeeSiteBuilder csB = new CoffeeSiteBuilder();
        
        coffeeS = csB.setName(testSiteName)
                        .setSiteType(siteTypeName)
                        .setCoffeeSort(sort1, sort2)
                        .setCompany(compName)
                        .setCups(cup1, cup2)
                        .setLocationType(locType)
                        .setHodnoceni(hodnoceni)
                        .setNextToMachineTypes(ntmt1, ntmt2)
                        .setLocationType(locType)
                        .setOffer(offer1, offer2)
                        .setOriginalUser(origUser)
                        .setPriceRange(cena)
                        .setRecordStatus(recStatE)
                        .setStatusSitu(siteStatE)
                        .build();

        coffeeS.setCreatedOn(new Timestamp(new Date().getTime()));            
        
        coffeeS.setMesto(mesto);
        coffeeS.setUliceCP(ulice);
        
        coffeeS.setNumOfCoffeeAutomatyVedleSebe(numOfCoffeeMachines);
        
        coffeeS.setPristupnostDny(dny);      
        coffeeS.setPristupnostHod(hod);
        
        coffeeS.setZemDelka(zemDelka);
        coffeeS.setZemSirka(zemSirka);
        
        coffeeS.setInitialComment(hodnoceni);
        
        Mockito.when(coffeeSiteRepository.searchByName(coffeeS.getSiteName()))
            .thenReturn(coffeeS);
    }
    
    /**
     * Tests if correct CoffeeSite is returned when requested by name.
     */
    @Test
    public void whenValidName_thenSiteShouldBeFound()
    {
        CoffeeSiteDTO found = coffeeSiteService.findByName(testSiteName);
      
        assertThat(found.getSiteName())
          .isEqualTo(testSiteName);
        
        assertThat(found.getTypPodniku().getCoffeeSiteType())
          .isEqualTo(siteTypeName);
        
        assertThat(found.getOtherOffers())
          .isEqualTo(coffeeS.getOtherOffers());
        
        assertThat(found.getCena().getPriceRange())
          .isEqualTo(cena);
        
        assertThat(found.getNextToMachineTypes())
          .isEqualTo(coffeeS.getNextToMachineTypes());
        
        assertThat(found.getNumOfCoffeeAutomatyVedleSebe())
          .isEqualTo(numOfCoffeeMachines);
               
        assertThat(found.getOriginalUserName())
          .isEqualTo(coffeeS.getOriginalUser().getUserName() );
        
        assertThat(found.getStatusZarizeni().getStatus())
          .isEqualTo(siteStatE.getSiteStatus());
        
        assertThat(found.getCoffeeSorts())
          .isEqualTo(coffeeS.getCoffeeSorts());
        
        assertThat(found.getTypLokality().getLocationType())
          .isEqualTo(locType);
        
        assertThat(found.getCupTypes())
          .isEqualTo(coffeeS.getCupTypes());
        
        assertThat(found.getDodavatelPodnik().getNameOfCompany())
          .isEqualTo(compName);
        
        assertThat(found.getCena().getPriceRange())
          .isEqualTo(cena);
        
        assertThat(found.getMesto())
          .isEqualTo(mesto);
        
        assertThat(found.getUliceCP())
          .isEqualTo(ulice);
        
        assertThat(found.getPristupnostDny())
          .isEqualTo(dny);
        
        assertThat(found.getPristupnostHod())
          .isEqualTo(hod);
        
        assertThat(found.getZemDelka())
          .isEqualTo(zemDelka);
        
        assertThat(found.getZemSirka())
          .isEqualTo(zemSirka);
        
    } 
    
    //TODO - dalsi testy CoffeeSiteService metody
}
