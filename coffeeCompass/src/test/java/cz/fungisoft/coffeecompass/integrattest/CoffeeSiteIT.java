package cz.fungisoft.coffeecompass.integrattest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit4.SpringRunner;

import cz.fungisoft.coffeecompass.CoffeeCompassApplication;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepositoryCustom;
import cz.fungisoft.coffeecompass.service.CSRecordStatusService;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteAttributesDBSaver;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteFactory;

/**
 * Class for Integration testing of Coffee site actions.
 * 
 * Create, modify, delete and retrieve operations for Coffee sites. 
 */
@RunWith(SpringRunner.class)
//@SpringBootTest(SpringBootTest.WebEnvironment.MOCK, classes = Application.class)
@SpringBootTest(classes = {CoffeeCompassApplication.class, CoffeeSiteAttributesDBSaver.class})
@AutoConfigureMockMvc
//@TestPropertySource(
//  locations = "classpath:application-integrationtest.properties")
public class CoffeeSiteIT {
 
    @Autowired
    private MockMvc mvc;
 
    @Autowired
    private CoffeeSiteRepository csRepository;
    
    @Autowired
    private CSRecordStatusService csRecordStatusService;
    
    @Autowired
    private UsersRepository userRepo;
    
    @Autowired
    private CoffeeSiteAttributesDBSaver attribSaver;
    
    private UserProfile userProfUser;
    
    private Set<UserProfile> userProfiles;
    
    private User genius;
    
    @Before
    public void setUp() {
      //TODO User to save and assign to CoffeeSite 
        // Vytvori UserProfile        
       userProfUser = new UserProfile();
       userProfUser.setType("USER");
       
       userProfiles = new HashSet<UserProfile>();
       userProfiles.add(userProfUser);
       
       genius = new User();
       
       genius.setUserName("richardF3");
       genius.setFirstName("Richard");
       genius.setLastName("Feynman");
       genius.setCreatedOn(new Timestamp(new Date().getTime()));
       genius.setPassword("QED");
       
       userRepo.save(genius);
    }
    
    @After
    public void tearDown() {
        userRepo.delete(genius);
    }
 
    @Test
    public void givenCoffeeSites_whenGetCoffeeSites_thenStatus200() throws Exception {
     
        CoffeeSite cs = CoffeeSiteFactory.getCoffeeSite("Integration test site", "automat");
        cs.setZemDelka(14.51122233d);
        cs.setZemSirka(50.456566d);
//        cs.setRecordStatus(new CoffeeSiteRecordStatus());
        
        CoffeeSiteRecordStatus coffeeSiteRecordStatus = csRecordStatusService.findCSRecordStatus(CoffeeSiteRecordStatusEnum.CREATED);
        cs.setRecordStatus(coffeeSiteRecordStatus);
        
        cs.setOriginalUser(genius);
        
        // ulozit  do DB atributy na ktere se CoffeeSite odkazuje -> pomocna trida, ktera 
        // tyto atributy ulozi do DB, obdobne jako u CoffeeSiteRepositoryTests
//        attribSaver.saveCoffeeSiteAtributesToDB(cs);
        
        csRepository.saveAndFlush(cs);
     
        mvc.perform(get("/rest/site/allSites/")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content()
          .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$[0].name", is("Integration test site")));
        
    }
    
}
