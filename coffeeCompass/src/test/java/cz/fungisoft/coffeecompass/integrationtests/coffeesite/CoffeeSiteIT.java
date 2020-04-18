package cz.fungisoft.coffeecompass.integrationtests.coffeesite;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.integrationtests.IntegrationTestBaseConfig;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Class for Integration testing of Coffee site actions.
 * 
 * Create, modify, delete and retrieve operations for Coffee sites. 
 */
@RunWith(SpringRunner.class)
//@SpringBootTest(SpringBootTest.WebEnvironment.MOCK, classes = Application.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
public class CoffeeSiteIT extends IntegrationTestBaseConfig 
{
 
    @Autowired
    private MockMvc mvc;
 
    @Autowired
    private CoffeeSiteRepository csRepository;
    
    @Autowired
    private UsersRepository userRepo;
    
    private User genius;
    
    
    @Before
    @Override
    public void setUp() {
        
       super.setUp(); 
       
       genius = new User();
       
       genius.setUserName("richardF3");
       genius.setFirstName("Richard");
       genius.setLastName("Feynman");
       genius.setCreatedOn(new Timestamp(new Date().getTime()));
       genius.setPassword("QED");
       genius.setUserProfiles(userProfilesUser);
       
       userRepo.save(genius);
    }
    
    @After
    public void tearDown() {
        //userRepo.delete(genius);
    }
 
    @Test
    public void givenCoffeeSites_whenGetCoffeeSites_thenStatus200() throws Exception {
     
        CoffeeSite cs = getCoffeeSiteBasedOnDB("Integration test site", "automat");
        
        cs.setZemDelka(14.51122233d);
        cs.setZemSirka(50.456566d);
        cs.setRecordStatus(CREATED);
        cs.setOriginalUser(genius);
        
        csRepository.saveAndFlush(cs);
     
        mvc.perform(get("/rest/site/allSites/")
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].siteName", is("Integration test site")));
        
    }

}
