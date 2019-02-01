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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cz.fungisoft.coffeecompass.controller.rest.UserControllerREST;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.dto.UserDataDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.security.IAuthenticationFacade;
import cz.fungisoft.coffeecompass.security.SecurityConfiguration;
import cz.fungisoft.coffeecompass.service.UserProfileService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.serviceimpl.UserServiceImpl;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Testovani Controller vrstvy pro praci s User.
 * 
 * @author Michal Vaclavek
 *
 */
@RunWith(SpringRunner.class)
//@WebMvcTest(UserControllerREST.class)
public class UsersControllerTests
{ 
    private MockMvc mvc;
    
    private UserControllerREST userController;
 
    @Mock
    private UserService userService;
    
    
    @Autowired
    private MapperFacade mapperFacade;
    
    /** 
     * Needed for maping from created User into UserDTO object, which is returned by UserControllerREST
     * 
     * @return
     */
    @TestConfiguration
    static class UserControllerTestContextConfiguration {
        
        @Bean
        public MapperFacade mapperFacade() {
            MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
            
            // Uprava pro mapovano z User na UserDataDTO - pro prenaseni na clienta neni potreba prenaset heslo a confirm hesla
            mapperFactory.classMap(User.class, UserDataDTO.class).exclude("password")
                                                                 .exclude("confirmPassword")
                                                                 .byDefault()
                                                                 .register();
            
            mapperFactory.classMap(UserDataDTO.class, User.class).byDefault().register();

            return mapperFactory.getMapperFacade();
        }  
    }
    
    private UserProfile userProfUser;
    
    private Set<UserProfile> userProfiles;
    
    
    @Before
    public void setUp() {
        
        userController = new UserControllerREST(userService);
        
        mvc = MockMvcBuilders.standaloneSetup(userController).build();
        
        // Vytvori UserProfile        
        userProfUser = new UserProfile();
        userProfUser.setType("USER");
        
        userProfiles = new HashSet<UserProfile>();
        userProfiles.add(userProfUser);
    }
 
    @Test
    public void whenPostUser_thenCreateUser() throws Exception {
        
        // given
        User john = new User();
        john.setUserName("john");
        john.setId(1);
        
        john.setCreatedOn(new Timestamp(new Date().getTime()));
        john.setUserProfiles(userProfiles); 
                
        given(userService.saveUser(Mockito.any(User.class))).willReturn(john);
        given(userService.findByIdToTransfer(Mockito.anyInt())).willReturn(mapperFacade.map(john, UserDataDTO.class) );
        
        // when and then

        mvc.perform(post("/rest/user/").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(john)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.userName", is(john.getUserName())));
        
        // then
        
        verify(userService, VerificationModeFactory.times(1)).saveUser(Mockito.any(User.class));
        reset(userService);
    }
    
    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {       
        User john = new User();
        john.setUserName("john");
        UserDataDTO johnDto = mapperFacade.map(john, UserDataDTO.class);
        
        User mary = new User();
        mary.setUserName("mary");
        UserDataDTO maryDto = mapperFacade.map(mary, UserDataDTO.class);        
        
        User dick = new User();        
        dick.setUserName("dick");
        UserDataDTO dickDto = mapperFacade.map(dick, UserDataDTO.class);
            
        List<UserDataDTO> allUsers = Arrays.asList(dickDto, maryDto, johnDto);
     
        given(userService.findAllUsers()).willReturn(allUsers);
     
        mvc.perform(get("/rest/user/all")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(3)))
          .andExpect(jsonPath("$[0].userName", is(dick.getUserName())));
        
        verify(userService, VerificationModeFactory.times(1)).findAllUsers();
        reset(userService);
    }
    
}
