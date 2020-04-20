package cz.fungisoft.coffeecompass.unittest.user;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;
import cz.fungisoft.coffeecompass.testutils.WithMockCustomAdminUser;
import cz.fungisoft.coffeecompass.testutils.WithMockCustomUserSecurityContextFactory;
import cz.fungisoft.coffeecompass.unittest.MvcControllerUnitTestBaseSetup;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.collect.ImmutableMap;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit testy pro Controllery pro User objekt s vyuzitim Spring MVC contextu,
 * tj. lze pouzit MockMVC tridu pro zajisteni spravneho "invoke" controller
 * vrstvy poslanim http requestu.
 * Ostatni vrstvy jsou "mockovany"
 * 
 * @author Michal Vaclavek
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest
public class UserControllerMvcTest extends MvcControllerUnitTestBaseSetup
{
    private MockMvc mockMvc;
    
    @MockBean
    @Autowired
    @Qualifier("customUserDetailsService")
    protected UserDetailsService customUserDetailsService; 
  
    @TestConfiguration
    protected static class UserControllerTestContextConfiguration {
      
        @MockBean //provided by Spring Context
        public static UserService userService;
        
        @Bean("customUserDetailsService")
        public UserDetailsService customUserDetailsService() {
          
          return new CustomUserDetailsService(userService);
        }
    }
    
    // USER profile
    private static UserProfile userProfUser;
    private static Set<UserProfile> userProfiles;
    
    // ADMIN profile
    private static UserProfile userProfADMIN;
    private static Set<UserProfile> userProfilesADMIN;
    
    private static String token = "xy";
    private static String deviceID = "4545454545";
    
    private static User admin = new User();
    private SignUpAndLoginRESTDto signUpAndLoginRESTDtoAdmin;
    
    
    @BeforeClass
    public static void setUpClass() {
        
        // Vytvori testovaci UserProfile typu USER
        userProfUser = new UserProfile();
        userProfUser.setType("USER");
        
        userProfiles = new HashSet<UserProfile>();
        userProfiles.add(userProfUser);
        
        // Vytvori testovaci UserProfile typu ADMIN
        userProfADMIN = new UserProfile();
        userProfADMIN.setType("ADMIN");
        
        userProfilesADMIN = new HashSet<UserProfile>();
        userProfilesADMIN.add(userProfADMIN);
        
        // ADMIN user used when login, before other REST request, which requires loged-in user with ADMIN
        admin.setUserName("admin");
        admin.setPassword("adminpassword");
        admin.setEmail("admin@boss.com");
        admin.setId(1L);
        admin.setCreatedOn(new Timestamp(new Date().getTime()));
        admin.setUserProfiles(userProfilesADMIN);
        
    }
    
    @Before
    public void setUp() {
        
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                                 .apply(springSecurity()) // nacte nastaveni vnucene pomoci WithMockCustomUserSecurityContextFactory
                                 .build();
        
        //given(authenticationService.findByToken(Mockito.anyString())).willReturn(userAdminDetailsOpt);
        //given(UserControllerTestContextConfiguration.userService.findByUserName(Mockito.anyString())).willReturn(Optional.of(admin));
        //given(UserControllerTestContextConfiguration.userService.findByEmail(Mockito.anyString())).willReturn(Optional.of(admin));
        
        given(UserControllerTestContextConfiguration.userService.isEmailUnique(Mockito.any(), Mockito.any(String.class))).willReturn(true); //registerNewRESTUser(registerRequest);
        
        Map<String, String> tokenMap = ImmutableMap.of("deviceID", deviceID, "userName", admin.getUserName(), "exp", "10");
        //token = UserControllerTestContextConfiguration.tokenService.expiring(tokenMap);
        
        given(tokenServiceMock.verify(Mockito.anyString())).willReturn(tokenMap);
        given(tokenServiceMock.expiring(Mockito.anyMap())).willReturn(token);
        
        //given(authenticationService.login(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class))).willReturn(Optional.of(token));
        given(passwordEncoder.matches(Mockito.any(String.class), Mockito.any(String.class))).willReturn(true);
        
        given(authenticationService.login(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class))).willReturn(Optional.of(token));
    }
 
    @Test
    public void whenPostUser_thenCreateUser() throws Exception {
        
        // given
        User john = new User();
        john.setUserName("john");
        john.setPassword("johnpassword");
        john.setEmail("john@vonneuman4.com");
        john.setId(1L);
        
        john.setCreatedOn(new Timestamp(new Date().getTime()));
        john.setUserProfiles(userProfiles);

        // Testovaci objekt slouzici pro zaregistrovani noveho User uctu
        SignUpAndLoginRESTDto signUpAndLoginRESTDto = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTDto.setUserName(john.getUserName());
        signUpAndLoginRESTDto.setPassword(john.getPassword());
        signUpAndLoginRESTDto.setEmail(john.getEmail());
        signUpAndLoginRESTDto.setDeviceID(deviceID);

        given(UserControllerTestContextConfiguration.userService.registerNewRESTUser(Mockito.any(SignUpAndLoginRESTDto.class))).willReturn(john); //registerNewRESTUser(registerRequest);
        
        //given(authenticationService.login(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class))).willReturn(Optional.of(token));

        ArgumentCaptor<User> valueCapture = ArgumentCaptor.forClass(User.class);
        doNothing().when( tokenCreateAndSendEmailService).setUserVerificationData(valueCapture.capture(), Mockito.any(Locale.class));
        given(tokenCreateAndSendEmailService.createAndSendVerificationTokenEmail()).willReturn(token);
       
        // when and then
        mockMvc.perform(post("/rest/public/user/register").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(signUpAndLoginRESTDto)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.*", hasSize(3)))
               .andExpect(jsonPath("$.tokenType", Matchers.is("Bearer")))
               .andExpect(jsonPath("$.accessToken", Matchers.isA(String.class)))
               .andExpect(jsonPath("$.accessToken").isNotEmpty());
        
        // then
        verify(UserControllerTestContextConfiguration.userService, VerificationModeFactory.times(1)).registerNewRESTUser(Mockito.eq(signUpAndLoginRESTDto));
    }
    
    /**
     * To test if all Users are returned when requested. Only ADMIN user can request that.
     * First, mock ADMIN user is logged in. 
     * 
     * @throws Exception
     */
    @Test
    @WithMockCustomAdminUser // inserts securityContext by WithMockCustomUserSecurityContextFactory
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {    
        
        UserDTO johnDto = new UserDTO();
        johnDto.setUserName("john");
        johnDto.setEmail("john@vonneuman.com");
        
        UserDTO maryDto = new UserDTO();
        maryDto.setUserName("mary");
        maryDto.setEmail("mary@gun.com");
        
        UserDTO dickDto = new UserDTO();        
        dickDto.setUserName("dick");
        dickDto.setEmail("dick@feynman.com");
        
        List<UserDTO> allUsers = Arrays.asList(dickDto, maryDto, johnDto);
        
        // given
        given(UserControllerTestContextConfiguration.userService.findAllUsers()).willReturn(allUsers);
        
        //userSecurityService
        given(userSecurityService.authWithToken(Mockito.anyString())).willReturn(WithMockCustomUserSecurityContextFactory.getTestAuthentication());
        
        // Login ADMIN user
        signUpAndLoginRESTDtoAdmin = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTDtoAdmin.setUserName(admin.getUserName());
        signUpAndLoginRESTDtoAdmin.setPassword(admin.getPassword());
        signUpAndLoginRESTDtoAdmin.setEmail(admin.getEmail());
        signUpAndLoginRESTDtoAdmin.setDeviceID(deviceID);
        
        // when and then
        mockMvc.perform(post("/rest/public/user/login").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(signUpAndLoginRESTDtoAdmin)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.accessToken", is(token)));
        
        // and then Get all users
        mockMvc.perform(get("/rest/secured/user/all")
               .header("Authorization", "Bearer " + token)
               .accept("application/json;charset=UTF-8"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(3)));
        
        verify(UserControllerTestContextConfiguration.userService, VerificationModeFactory.times(1)).findAllUsers();
    }

}
