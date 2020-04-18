package cz.fungisoft.coffeecompass.unittest.coffeesite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

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

import cz.fungisoft.coffeecompass.controller.rest.CoffeeSiteControllerPublicREST;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteFactory;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Testovani Controller vrstvy pro praci s CoffeeSite.
 * 
 * @author Michal Vaclavek
 *
 */
@RunWith(SpringRunner.class)
public class CoffeeSiteControllerPublicMvcTests
{ 
    //@Autowired
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
            
            //mapperFactory.classMap(UserDTO.class, User.class).byDefault().register();

            return mapperFactory.getMapperFacade();
        }  
    }
       
    
    @Before
    public void setUp() {
        coffeeSiteControllerPublic = new CoffeeSiteControllerPublicREST(csService, starsForCoffeeSiteService);
        mvc = MockMvcBuilders.standaloneSetup(coffeeSiteControllerPublic).build();
    }
 
    
    @Test
    public void givenCoffeeSites_whenGetSites_thenReturnJsonArray() throws Exception {
        
        CoffeeSite cs1 = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite1", "automat");
        CoffeeSiteDTO cs1Dto = mapperFacade.map(cs1, CoffeeSiteDTO.class);
        
        CoffeeSite cs2 = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite2", "automat");
        CoffeeSiteDTO cs2Dto = mapperFacade.map(cs2, CoffeeSiteDTO.class);
            
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
