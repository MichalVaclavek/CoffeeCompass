package cz.fungisoft.coffeecompass.unittest;

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
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import cz.fungisoft.coffeecompass.controller.CoffeeSiteController;
import cz.fungisoft.coffeecompass.controller.rest.CoffeeSiteControllerREST;
import cz.fungisoft.coffeecompass.controller.rest.UserControllerREST;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.UserDataDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.security.SecurityConfiguration;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.UserProfileService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteFactory;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;
import ma.glasnost.orika.MapperFacade;

/**
 * Testovani Controller vrstvy pro praci s CoffeeSite.
 * 
 * @author Michal
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(CoffeeSiteControllerREST.class)
public class CoffeeSiteControllerTests
{ 
    @Autowired
    private MockMvc mvc;
 
    @MockBean
    private CoffeeSiteService csService;
     
    @Autowired
    private MapperFacade mapperFacade;
    
    
    @Before
    public void setUp() {
    }
 
    @Test
    public void whenPostSite_thenCreateCoffeeSite() throws Exception {
        CoffeeSite cs = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite", "automat");
                
        given(csService.save(Mockito.any(CoffeeSiteDTO.class))).willReturn(cs);

        mvc.perform(post("/rest/site/").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(cs)))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.siteName", is("ControllerTestSite")));
        
        verify(csService, VerificationModeFactory.times(1)).save(Mockito.any(CoffeeSiteDTO.class));
        reset(csService);
    }
    
    @Test
    public void givenUsers_whenGetSites_thenReturnJsonArray() throws Exception {       
        CoffeeSite cs1 = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite1", "automat");
        CoffeeSiteDTO cs1Dto = mapperFacade.map(cs1, CoffeeSiteDTO.class);
        
        CoffeeSite cs2 = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite2", "automat");
        CoffeeSiteDTO cs2Dto = mapperFacade.map(cs2, CoffeeSiteDTO.class);
            
        List<CoffeeSiteDTO> allSites = Arrays.asList(cs1Dto, cs2Dto);
     
        given(csService.findAll("siteName", "ASC")).willReturn(allSites);
     
        mvc.perform(get("/rest/site/allSites")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].siteName", is(cs1.getSiteName())));
        
        verify(csService, VerificationModeFactory.times(1)).findAll("siteName", "ASC");
        reset(csService);
    }
    
}
