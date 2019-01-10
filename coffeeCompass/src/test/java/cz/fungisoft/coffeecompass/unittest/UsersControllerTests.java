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

import cz.fungisoft.coffeecompass.controller.rest.UserControllerREST;
import cz.fungisoft.coffeecompass.dto.UserDataDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.security.SecurityConfiguration;
import cz.fungisoft.coffeecompass.service.UserProfileService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;
import ma.glasnost.orika.MapperFacade;

/**
 * Testovani Controller vrstvy pro praci s User.
 * 
 * @author Michal
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(UserControllerREST.class)
public class UsersControllerTests
{ 
    @Autowired
    private MockMvc mvc;
 
    @MockBean
    private UserService userService;
    
    @MockBean
    private UserProfileService userProfservice;
    
    @Autowired
    private MapperFacade mapperFacade;
       
    
    private UserProfile userProfUser;
    
    private Set<UserProfile> userProfiles;
    
    @Before
    public void setUp() {
        // Vytvori UserProfile        
        userProfUser = new UserProfile();
        userProfUser.setType("USER");
        
        userProfiles = new HashSet<UserProfile>();
        userProfiles.add(userProfUser);
    }
 
    @Test
    public void whenPostUser_thenCreateUser() throws Exception {
        User john = new User();
        john.setUserName("john");
        
        john.setCreatedOn(new Timestamp(new Date().getTime()));
        john.setUserProfiles(userProfiles); 
                
        given(userService.saveUser(Mockito.anyObject())).willReturn(john);

        mvc.perform(post("/rest/user/").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(john)))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.userName", is("john")));
        
        verify(userService, VerificationModeFactory.times(1)).saveUser(Mockito.anyObject());
        reset(userService);
    }
    
    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {       
        User john = new User();
        john.setUserName("john");
        UserDataDTO johnDto = mapperFacade.map(john, UserDataDTO.class);
        
        User mary = new User();
        mary.setUserName("mary");
        UserDataDTO maryDto = mapperFacade.map(john, UserDataDTO.class);        
        
        User dick = new User();        
        dick.setUserName("dick");
        UserDataDTO dickDto = mapperFacade.map(john, UserDataDTO.class);
            
        List<UserDataDTO> allUsers = Arrays.asList(dickDto, maryDto, johnDto);
     
        given(userService.findAllUsers()).willReturn(allUsers);
     
        mvc.perform(get("/rest/user/all")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].userName", is(dick.getUserName())));
        
        verify(userService, VerificationModeFactory.times(1)).findAllUsers();
        reset(userService);
    }
    
}
