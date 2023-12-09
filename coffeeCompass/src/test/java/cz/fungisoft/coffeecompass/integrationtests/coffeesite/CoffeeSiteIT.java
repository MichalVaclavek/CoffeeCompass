package cz.fungisoft.coffeecompass.integrationtests.coffeesite;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.integrationtests.IntegrationTestBaseConfig;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Class for Integration testing of Coffee site actions.
 * 
 * Create, modify, delete and retrieve operations for Coffee sites. 
 */
@ExtendWith(SpringExtension.class)
//@SpringBootTest(SpringBootTest.WebEnvironment.MOCK, classes = Application.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
class CoffeeSiteIT extends IntegrationTestBaseConfig {
    
    @Autowired
    private MockMvc mvc;
 
    @Autowired
    private CoffeeSiteRepository csRepository;
    
    @Autowired
    private UsersRepository userRepo;
    
    private User genius;
    
    /**
     * Prepares User object to be assigned as author of the newly created CoffeeSite used in tests.
     */
    @BeforeEach
    @Override
    public void setUp() {
        
       super.setUp(); 
       
       genius = new User();
       
       genius.setUserName("richardF3");
       genius.setFirstName("Richard");
       genius.setLastName("Feynman");
       genius.setCreatedOn(LocalDateTime.now());
       genius.setPassword("QED");
       genius.setUserProfiles(userProfilesUser);
       
       userRepo.save(genius);
    }
    
    @AfterEach
    public void tearDown() {
        //userRepo.delete(genius);
    }
    
    private static final String SITE_NAME = "Integration test site";
    private static final String SITE_TYPE = "automat";
 
    /**
     * Tests REST endpoint /rest/site/allSites/  which should return all saved CoffeeSites list.
     * @throws Exception
     */
    @Test
    void givenCoffeeSites_whenGetCoffeeSites_thenStatus200() throws Exception {
     
        CoffeeSite cs = getCoffeeSiteBasedOnDB(SITE_NAME, SITE_TYPE);
        
        cs.setZemDelka(14.51122233d);
        cs.setZemSirka(50.456566d);
        cs.setRecordStatus(CREATED);
        cs.setOriginalUser(genius);
        
        csRepository.saveAndFlush(cs);
     
        mvc.perform(get("/rest/site/allSites/")
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].siteName", is(SITE_NAME)))
           .andExpect(jsonPath("$[0].typPodniku.coffeeSiteType", is(SITE_TYPE)));
    }

}
