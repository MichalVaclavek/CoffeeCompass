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

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cz.fungisoft.coffeecompass.configuration.ConfigProperties;
import cz.fungisoft.coffeecompass.controller.CoffeeSiteController;
import cz.fungisoft.coffeecompass.controller.rest.CoffeeSiteControllerPublicREST;
import cz.fungisoft.coffeecompass.controller.rest.secured.CoffeeSiteControllerSecuredREST;
import cz.fungisoft.coffeecompass.controller.rest.secured.UserControllerSecuredREST;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.security.IAuthenticationFacade;
import cz.fungisoft.coffeecompass.security.SecurityConfiguration;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.UserProfileService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteFactory;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;
import cz.fungisoft.coffeecompass.unittest.MvcControllerUnitTestBaseSetup;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Testovani Controller vrstvy pro praci s CoffeeSite.
 * 
 * @author Michal
 *
 */
@RunWith(SpringRunner.class)
//@AutoConfigureMockMvc
//@WebMvcTest({CoffeeSiteControllerPublicREST.class, CoffeeSiteControllerSecuredREST.class})
//@SpringBootTest(classes = {UsersControllerPublicREST.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("dev")
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
            
            //mapperFactory.classMap(UserDTO.class, User.class).byDefault().register();

            return mapperFactory.getMapperFacade();
        }  
    }
       
    
    @Before
    public void setUp() {
        coffeeSiteControllerSecured = new CoffeeSiteControllerSecuredREST(csService, messageSource);
        mvc = MockMvcBuilders.standaloneSetup(coffeeSiteControllerSecured).build();
    }
 
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
