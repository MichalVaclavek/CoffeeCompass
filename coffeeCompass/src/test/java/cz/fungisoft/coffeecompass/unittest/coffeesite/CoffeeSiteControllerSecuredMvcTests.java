package cz.fungisoft.coffeecompass.unittest.coffeesite;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.fungisoft.coffeecompass.mappers.CoffeeSiteMapper;
import cz.fungisoft.coffeecompass.mappers.CoffeeSiteMapperImpl;
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

import cz.fungisoft.coffeecompass.controller.rest.secured.CoffeeSiteControllerSecuredREST;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
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
class CoffeeSiteControllerSecuredMvcTests { 

    private MockMvc mvc;
    
    /**
     * Controller to be tested here
     */
    private CoffeeSiteControllerSecuredREST coffeeSiteControllerSecured;
 
    @MockBean
    private CoffeeSiteService csService;
    
    @MockBean
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    @MockBean
    private MessageSource messageSource;
     
    @Autowired
    private CoffeeSiteMapper coffeeSiteMapper;
    
    /** 
     * Needed for maping from created User into UserDTO object, which is returned by UserControllerREST
     * 
     * @return
     */
//    @TestConfiguration
//    static class CoffeeSiteControllerTestContextConfiguration {
//
//        @Bean
//        @Primary
//        public CoffeeSiteMapper mapperFacade() {
//            CoffeeSiteMapper mapperFactory = new DefaultMapperFactory.Builder().build();
//
//            // Only userName is needed for CoffeeSiteDto object
//            mapperFactory.classMap(CoffeeSite.class, CoffeeSiteDTO.class)
//                         .field("originalUser.userName", "originalUserName")
//                         .field("lastEditUser.userName", "lastEditUserName")
//                         .byDefault()
//                         .register();
//
//            return mapperFactory.getMapperFacade();
//        }
//    }
       
    
    @BeforeEach
    public void setUp() {
        coffeeSiteControllerSecured = new CoffeeSiteControllerSecuredREST(csService, messageSource);
        mvc = MockMvcBuilders.standaloneSetup(coffeeSiteControllerSecured).build();
    }
    
    private static final String COFFEE_SITE_NAME = "ControllerTestSite";
 
    /**
     * Tests if user is saved in service layer after calling post /rest/secured/site/create  REST request.
     * @throws Exception
     */
    @Test
    void whenPostSite_thenCreateCoffeeSite() throws Exception {
        
        CoffeeSite cs = CoffeeSiteFactory.getCoffeeSite(COFFEE_SITE_NAME, "automat");
                
        given(csService.save(Mockito.any(CoffeeSiteDTO.class))).willReturn(cs);
        given(csService.findOneToTransfer(Mockito.eq(cs.getId()))).willReturn(Optional.of(coffeeSiteMapper.coffeeSiteToCoffeeSiteDTO(cs)));

        mvc.perform(post("/rest/secured/site/create").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(cs)))
                                       .andExpect(status().isCreated())
                                       .andExpect(jsonPath("$.siteName", is(COFFEE_SITE_NAME)));
        
        verify(csService, VerificationModeFactory.times(1)).save(Mockito.any(CoffeeSiteDTO.class));
    }
    
}
