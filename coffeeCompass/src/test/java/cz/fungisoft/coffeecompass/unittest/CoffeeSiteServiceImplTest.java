package cz.fungisoft.coffeecompass.unittest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription.StarsQualityEnum;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus.CoffeeSiteStatusEnum;
import cz.fungisoft.coffeecompass.entity.CupType.CupTypeEnum;
import cz.fungisoft.coffeecompass.entity.NextToMachineType.NextToMachineTypeEnum;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteBuilder;
import cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteServiceImpl;
import cz.fungisoft.coffeecompass.serviceimpl.UserServiceImpl;
import ma.glasnost.orika.MapperFacade;

/**
 * Testuje Service vrstvu pro praci s objekty CoffeeSite.
 * 
 * @author Michal
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CoffeeSiteServiceImplTest
{
    /*
    @TestConfiguration
    static class CoffeeSiteServiceImplTestContextConfiguration
    { 
        @Bean
        public CoffeeSiteService coffeeSiteService() {
            return new CoffeeSiteServiceImpl();
        }
    }
 */
    @Autowired
    private CoffeeSiteService coffeeSiteService;
    
    // Nutne pro kontrolu CoffeeSiteDto, ktere obvykle vraci CoffeeSiteService
    // pro porovnani s pripravenym testovacim CoffeeSite coffeeS
    @Autowired
    private MapperFacade mapperFacade;
 
    @MockBean
    private CoffeeSiteRepository coffeeSiteRepository;
    
    private CoffeeSite coffeeS;
    
    private String testSiteName = "Test5";
    private String siteTypeName = "bistro";
    
    private CoffeeSiteStatusEnum siteStatE = CoffeeSiteStatusEnum.INSERVICE;
    private CoffeeSiteRecordStatusEnum recStatE = CoffeeSiteRecordStatusEnum.ACTIVE;
    
    private String hodnoceni = "Ujde";
    private double stars = 2.6d;
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

   
    @Before
    public void setUp()
    {
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
          .isEqualTo(mapperFacade.map(coffeeS, CoffeeSiteDTO.class).getOriginalUserName() );
        
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
