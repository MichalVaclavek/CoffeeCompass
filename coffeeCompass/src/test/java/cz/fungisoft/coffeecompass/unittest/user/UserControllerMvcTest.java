package cz.fungisoft.coffeecompass.unittest.user;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.RefreshToken;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;
import cz.fungisoft.coffeecompass.testutils.WithMockCustomAdminUser;
import cz.fungisoft.coffeecompass.testutils.WithMockCustomUserSecurityContextFactory;
import cz.fungisoft.coffeecompass.unittest.MvcControllerUnitTestBaseSetup;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
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
@ExtendWith(SpringExtension.class)
@WebMvcTest
public class UserControllerMvcTest extends MvcControllerUnitTestBaseSetup {

    private static final String PUBLIC_REST_URLS = "/rest/public/**";

    private MockMvc mockMvc;
    
    @Autowired
    @Qualifier("customUserDetailsService")
    protected UserDetailsService customUserDetailsService; 
  
    @TestConfiguration
    protected static class UserControllerTestContextConfiguration {
      
        @MockBean //provided by Spring Context
        public static UserService userService;

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
            return web -> web.ignoring().requestMatchers(PUBLIC_REST_URLS);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//            http
//                    .httpBasic().disable()
//                    .cors().and().csrf().disable()
//                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                    .and()
//                    .authorizeRequests()
//                    .antMatchers("/rest/public/user/register").permitAll()
//                    .anyRequest().authenticated();

            http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authz) -> authz
                .requestMatchers("/rest/public/user/register").permitAll()
                .anyRequest().authenticated());

            return http.build();
        }

        @Bean("customUserDetailsService")
        public UserDetailsService customUserDetailsService() {
          return new CustomUserDetailsService(userService);
        }
    }

    private static final String token = "xy";
    private static final String deviceID = "4545454545";

    private static final String refreshTokenString = "xdfdfdfdf";
    private static final RefreshToken refreshToken = new RefreshToken();
    
    private static final User admin = new User();
    private SignUpAndLoginRESTDto signUpAndLoginRESTDtoAdmin;


    @BeforeAll
    public static void setUpClass() {
        // Vytvori testovaci UserProfile typu USER
        // USER profile
        UserProfile userProfUser = new UserProfile();
        userProfUser.setType("USER");

        Set<UserProfile> userProfiles = new HashSet<>();
        userProfiles.add(userProfUser);
        
        // Vytvori testovaci UserProfile typu ADMIN
        // ADMIN profile
        UserProfile userProfADMIN = new UserProfile();
        userProfADMIN.setType("ADMIN");

        Set<UserProfile> userProfilesADMIN = new HashSet<>();
        userProfilesADMIN.add(userProfADMIN);
        
        // ADMIN user used when login, before other REST request, which requires loged-in user with ADMIN
        admin.setUserName("admin");
        admin.setPassword("adminpassword");
        admin.setEmail("admin@boss.com");
        admin.setId(1L);
        admin.setCreatedOn(LocalDateTime.now());
        admin.setUserProfiles(userProfilesADMIN);
    }
    
    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                                 .apply(springSecurity()) // nacte nastaveni vnucene pomoci WithMockCustomUserSecurityContextFactory
                                 .build();
        
        given(UserControllerTestContextConfiguration.userService.isEmailUnique(Mockito.any(), Mockito.any(String.class))).willReturn(true); //registerNewRESTUser(registerRequest);
        
        Map<String, String> tokenMap = ImmutableMap.of("deviceID", deviceID, "userName", admin.getUserName(), "exp", "10");
        
        given(tokenServiceMock.verify(Mockito.anyString())).willReturn(tokenMap);
        given(tokenServiceMock.expiring(Mockito.anyMap())).willReturn(token);
        
        given(passwordEncoder.matches(Mockito.any(String.class), Mockito.any(String.class))).willReturn(true);
        
        given(authenticationService.login(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class))).willReturn(Optional.of(token));
    }
 
    @Test
    void whenPostUser_thenCreateUser() throws Exception {
        // given
        User john = new User();
        john.setUserName("john");
        john.setPassword("johnpassword");
        john.setEmail("john@vonneuman.com");

        refreshToken.setUser(john);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(99));
        refreshToken.setToken(refreshTokenString);

        // Testovaci objekt slouzici pro zaregistrovani noveho User uctu
        SignUpAndLoginRESTDto signUpAndLoginRESTDto = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTDto.setUserName(john.getUserName());
        signUpAndLoginRESTDto.setPassword(john.getPassword());
        signUpAndLoginRESTDto.setEmail(john.getEmail());
        signUpAndLoginRESTDto.setDeviceID(deviceID);

        given(UserControllerTestContextConfiguration.userService.registerNewRESTUser(Mockito.any(SignUpAndLoginRESTDto.class))).willReturn(john); //registerNewRESTUser(registerRequest);
        
        given(tokenCreateAndSendEmailService.createAndSendVerificationTokenEmail(Mockito.any(User.class), Mockito.any(Locale.class))).willReturn(token);

        given(refreshTokenService.createRefreshToken(Mockito.matches("john"))).willReturn(refreshToken);

        // when and then
        mockMvc.perform(post("/rest/public/user/register").locale(Locale.ENGLISH).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(signUpAndLoginRESTDto)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.*", hasSize(4)))
               .andExpect(jsonPath("$.tokenType", Matchers.is("Bearer")))
               .andExpect(jsonPath("$.accessToken", Matchers.is(token)))
               .andExpect(jsonPath("$.refreshToken", Matchers.is(refreshTokenString)));
        
        // then
        verify(UserControllerTestContextConfiguration.userService, VerificationModeFactory.times(1)).registerNewRESTUser(signUpAndLoginRESTDto);
    }
    
    /**
     * To test if all Users are returned when requested. Only ADMIN user can request that.
     * First, mock ADMIN user is logged in.
     * <p>
     * @WithMockCustomAdminUser inserts securityContext by WithMockCustomUserSecurityContextFactory
     * 
     * @throws Exception
     */
    @Test
    @WithMockCustomAdminUser // mock ADMIN user is logged in.
    void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
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
        
        // userSecurityService
        given(userSecurityService.authWithToken(Mockito.anyString())).willReturn(WithMockCustomUserSecurityContextFactory.getTestAuthentication());
        

        // and then Get all users
        mockMvc.perform(get("/rest/secured/user/all")
                          .header("Authorization", "Bearer " + token)
                          .accept("application/json;charset=UTF-8"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(3)));
        
        verify(UserControllerTestContextConfiguration.userService, VerificationModeFactory.times(1)).findAllUsers();
    }
}
