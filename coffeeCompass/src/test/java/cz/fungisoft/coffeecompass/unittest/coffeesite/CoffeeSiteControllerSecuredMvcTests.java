package cz.fungisoft.coffeecompass.unittest.coffeesite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cz.fungisoft.coffeecompass.controller.rest.secured.CoffeeSiteControllerSecuredREST;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteFactory;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Testovani REST Controller vrstvy pro praci s CoffeeSite s vyuzitim Spring MVC.
 * 
 * @author Michal Vaclavek
 *
 */
@RunWith(SpringRunner.class)
public class CoffeeSiteControllerSecuredMvcTests
{ 
    //@Autowired
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
    private MapperFacade mapperFacade;
    
    /** 
     * Needed for maping from created User into UserDTO object, which is returned by UserControllerREST
     * 
     * @return
     */
    @TestConfiguration
    static class CoffeeSiteControllerTestContextConfiguration {
        
        @Bean
        @Primary
        public MapperFacade mapperFacade() {
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
       
    
    @Before
    public void setUp() {
        coffeeSiteControllerSecured = new CoffeeSiteControllerSecuredREST(csService, messageSource);
        mvc = MockMvcBuilders.standaloneSetup(coffeeSiteControllerSecured).build();
    }
 
    /**
     * Tests if user is saved in service layer after calling post /rest/secured/site/create  REST request.
     * @throws Exception
     */
    @Test
    public void whenPostSite_thenCreateCoffeeSite() throws Exception {
        
        CoffeeSite cs = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite", "automat");
                
        given(csService.save(Mockito.any(CoffeeSiteDTO.class))).willReturn(cs);
        given(csService.findOneToTransfer(Mockito.eq(cs.getId()))).willReturn(mapperFacade.map(cs, CoffeeSiteDTO.class));

        mvc.perform(post("/rest/secured/site/create").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(cs)))
                                       .andExpect(status().isCreated())
                                       .andExpect(jsonPath("$.siteName", is("ControllerTestSite")));
        
        verify(csService, VerificationModeFactory.times(1)).save(Mockito.any(CoffeeSiteDTO.class));
    }
    
}
