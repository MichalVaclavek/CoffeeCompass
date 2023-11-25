package cz.fungisoft.coffeecompass.unittest.coffeesite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import cz.fungisoft.coffeecompass.mappers.CoffeeSiteMapper;
import cz.fungisoft.coffeecompass.mappers.CoffeeSiteMapperImpl;
import cz.fungisoft.coffeecompass.mappers.UserMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cz.fungisoft.coffeecompass.controller.rest.CoffeeSiteControllerPublicREST;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteFactory;

/**
 * Testovani Public REST Controller vrstvy pro praci s CoffeeSite s vyuzitim Spring MVC.
 * 
 * @author Michal Vaclavek
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CoffeeSiteMapperImpl.class
})
class CoffeeSiteControllerPublicMvcTests { 
    
    private MockMvc mvc;
    
    /**
     * Controller to be tested here
     */
    private CoffeeSiteControllerPublicREST coffeeSiteControllerPublic;
    
    @MockBean
    private CoffeeSiteService csService;
    
    @MockBean
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    @MockBean
    private MessageSource messageSource;
     
    @Autowired
    private CoffeeSiteMapper coffeeSiteMapper;
    

    @BeforeEach
    public void setUp() {
        coffeeSiteControllerPublic = new CoffeeSiteControllerPublicREST(csService, starsForCoffeeSiteService);
        mvc = MockMvcBuilders.standaloneSetup(coffeeSiteControllerPublic).build();
    }
 
    /**
     * Tests if list of all CoffeeSites are returned after /rest/site/allSites/ REST request.
     * 
     * @throws Exception
     */
    @Test
    void givenCoffeeSites_whenGetSites_thenReturnJsonArray() throws Exception {
        
        CoffeeSite cs1 = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite1", "automat");
        CoffeeSiteDTO cs1Dto = coffeeSiteMapper.coffeeSiteToCoffeeSiteDTO(cs1);
        
        CoffeeSite cs2 = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite2", "automat");
        CoffeeSiteDTO cs2Dto = coffeeSiteMapper.coffeeSiteToCoffeeSiteDTO(cs2);
            
        List<CoffeeSiteDTO> allSites = Arrays.asList(cs1Dto, cs2Dto);
     
        given(csService.findAll(Mockito.anyString(), Mockito.anyString())).willReturn(allSites);
     
        mvc.perform(get("/rest/site/allSites/")
           .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(2)))
           .andExpect(jsonPath("$[0].siteName", is(cs1.getSiteName())));
        
        verify(csService, VerificationModeFactory.times(1)).findAll("id", "asc");
    }
}
