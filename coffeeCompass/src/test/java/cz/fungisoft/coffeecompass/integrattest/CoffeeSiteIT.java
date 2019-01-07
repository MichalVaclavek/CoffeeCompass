package cz.fungisoft.coffeecompass.integrattest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteFactory;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteAttributesDBSaver;

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
    private CoffeeSiteRepository repository;
    
    @Autowired
    private CoffeeSiteAttributesDBSaver attribSaver;
 
    @Test
    public void givenCoffeeSites_whenGetCoffeeSites_thenStatus200() throws Exception {
     
        CoffeeSite cs = CoffeeSiteFactory.getCoffeeSite("Integration test site", "automat");
        // ulozit  do DB atributy na ktere se CoffeeSite odkazuje -> pomocna trida, ktera 
        // tyto atributy ulozi do DB, obdobne jako u CoffeeSiteRepositoryTests
        attribSaver.saveCoffeeSiteAtributesToDB(cs);
        
        repository.saveAndFlush(cs);
     
        mvc.perform(get("/rest/site/allSites")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content()
          .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$[0].name", is("Integration test site")));
    }
    
}
