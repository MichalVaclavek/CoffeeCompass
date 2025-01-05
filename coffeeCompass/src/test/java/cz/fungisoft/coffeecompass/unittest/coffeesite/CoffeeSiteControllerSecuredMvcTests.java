package cz.fungisoft.coffeecompass.unittest.coffeesite;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.fungisoft.coffeecompass.mappers.CoffeeSiteMapperImpl;
import cz.fungisoft.coffeecompass.unittest.MvcControllerUnitTestBaseSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cz.fungisoft.coffeecompass.controller.rest.secured.CoffeeSiteControllerSecuredREST;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteFactory;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;

import java.util.Optional;

/**
 * Testovani REST Controller vrstvy pro praci s CoffeeSite s vyuzitim Spring MVC.
 * 
 * @author Michal Vaclavek
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CoffeeSiteMapperImpl.class
})
class CoffeeSiteControllerSecuredMvcTests extends MvcControllerUnitTestBaseSetup {

    private MockMvc mockMvc;
    
    /**
     * Controller to be tested here
     */
    private CoffeeSiteControllerSecuredREST coffeeSiteControllerSecured;
 

    @BeforeEach
    public void setUp() {
        coffeeSiteControllerSecured = new CoffeeSiteControllerSecuredREST(coffeeSiteService, messages);
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
//                .apply(springSecurity()) // nacte nastaveni vnucene pomoci WithMockCustomUserSecurityContextFactory
                .build();
    }
    
    private static final String COFFEE_SITE_NAME = "ControllerTestSite";
 
    /**
     * Tests if user is saved in service layer after calling post /api/v1/coffeesites/secured/site/create  REST request.
     * @throws Exception
     */
    @Test
    void whenPostSite_thenCreateCoffeeSite() throws Exception {
        
        CoffeeSite cs = CoffeeSiteFactory.getCoffeeSite(COFFEE_SITE_NAME, "automat");
                
        given(coffeeSiteService.save(Mockito.any(CoffeeSiteDTO.class))).willReturn(cs);
        given(coffeeSiteService.findOneToTransfer(Mockito.eq(cs.getId()))).willReturn(Optional.of(coffeeSiteMapper.coffeeSiteToCoffeeSiteDTO(cs)));

        mockMvc.perform(post("/api/v1/coffeesites/secured/site/create").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(cs)))
                                       .andExpect(status().isCreated())
                                       .andExpect(jsonPath("$.siteName", is(COFFEE_SITE_NAME)));
        
        verify(coffeeSiteService, VerificationModeFactory.times(1)).save(Mockito.any(CoffeeSiteDTO.class));
    }
    
}
