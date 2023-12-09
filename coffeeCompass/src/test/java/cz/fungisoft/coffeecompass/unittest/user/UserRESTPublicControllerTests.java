package cz.fungisoft.coffeecompass.unittest.user;

import cz.fungisoft.coffeecompass.controller.models.rest.AuthRESTResponse;
import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.controller.rest.UsersControllerPublicREST;
import cz.fungisoft.coffeecompass.entity.RefreshToken;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.listeners.OnRegistrationCompleteEvent;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.service.*;
import cz.fungisoft.coffeecompass.service.tokens.RefreshTokenService;
import cz.fungisoft.coffeecompass.service.tokens.TokenCreateAndSendEmailService;
import cz.fungisoft.coffeecompass.service.tokens.TokenService;
import cz.fungisoft.coffeecompass.service.user.CustomRESTUserAuthenticationService;
import cz.fungisoft.coffeecompass.service.user.UserSecurityService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import cz.fungisoft.coffeecompass.serviceimpl.user.CustomOAuth2UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.ImmutableMap;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.*;

/**
 * Unit Testovani Controller vrstvy pro praci s User entitou.
 * <p>
 * Service layer is mocked, Spring MVC is not used.
 * 
 * @author Michal Vaclavek
 *
 */
@ExtendWith(SpringExtension.class)
class UserRESTPublicControllerTests {

    @MockBean
    private CustomRESTUserAuthenticationService authenticationService;
    
    @MockBean
    private static TokenService tokenService;

    @MockBean
    private static RefreshTokenService refreshTokenService;

//    @TestConfiguration
//    protected static class UserRESTPublicControllerTestsConfiguration {
//
//        @Bean("customUserDetailsService")
//        public RefreshTokenService refreshTokenService() {
//            return new RefreshTokenService(userService, ) {
//                @Override
//                public Optional<RefreshToken> findByToken(String token) {
//                    return Optional.empty();
//                }
//
//                @Override
//                public RefreshToken createRefreshToken(String userName) {
//                    return null;
//                }
//
//                @Override
//                public RefreshToken verifyExpiration(RefreshToken token) {
//                    return null;
//                }
//
//                @Override
//                public void deleteByUserId(Long userId) {
//
//                }
//            };
//        }
//    }
  
    @MockBean //provided by Spring Context
    private static UserService userService;
  
    @MockBean
    private UserSecurityService userSecurityService;
    
    @MockBean
    private TokenCreateAndSendEmailService tokenCreateAndSendEmailService;


    @MockBean
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private  MessageSource messages;

    @MockBean
    private ApplicationEventPublisher eventPublisher;
    
    
    private UsersControllerPublicREST usersControllerPublicREST;
    
    // USER profile
    private static UserProfile userProfUser;
    private static Set<UserProfile> userProfiles;
    
    // ADMIN profile
    private static UserProfile userProfADMIN;
    private static Set<UserProfile> userProfilesADMIN;
    
    private static String token = "xy";
    private static String refreshTokenString = "refreshXy";
    private static RefreshToken refreshToken;
    private static String deviceID = "4545454545";
    
    private User admin = new User();
    private SignUpAndLoginRESTDto signUpAndLoginRESTDtoAdmin;
    
    
    @BeforeEach
    public void setUp() {
        
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
        
        given(userService.isEmailUnique(Mockito.any(), Mockito.any(String.class))).willReturn(true); //registerNewRESTUser(registerRequest);
        
        // ADMIN user profile returned, when requesting UserDetails
        admin.setUserName("admin");
        admin.setPassword("adminpassword");
        admin.setEmail("admin@boss.com");
        admin.setId(1L);
        admin.setCreatedOn(LocalDateTime.now());
        admin.setUserProfiles(userProfilesADMIN);
       
        // Testovaci objekt slouzici pro zaregistrovani noveho User uctu typu ADMIN
        signUpAndLoginRESTDtoAdmin = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTDtoAdmin.setUserName(admin.getUserName());
        signUpAndLoginRESTDtoAdmin.setPassword(admin.getPassword());
        signUpAndLoginRESTDtoAdmin.setEmail(admin.getEmail());
        signUpAndLoginRESTDtoAdmin.setDeviceID(deviceID);
        
        Map<String, String> tokenMap = ImmutableMap.of("deviceID", deviceID, "userName", admin.getUserName(), "exp", "10");
        
        given(tokenService.verify(Mockito.anyString())).willReturn(tokenMap);
        given(tokenService.expiring(Mockito.anyMap())).willReturn(token);
        refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenString);
        refreshToken.setUser(admin);
        given(refreshTokenService.createRefreshToken(Mockito.anyString())).willReturn(refreshToken);
        
        usersControllerPublicREST = new UsersControllerPublicREST(authenticationService, userService,
                tokenService, refreshTokenService, eventPublisher, messages);
    }
 
    /**
     * Tests creating of a new User object using UsersControllerPublicREST class method register().
     * 
     * @throws Exception
     */
    @Test
    void whenPostUser_thenCreateUser() throws Exception {
        
        // given
        User john = new User();
        john.setUserName("john");
        john.setPassword("johnpassword");
        john.setEmail("john@vonneuman4.com");
        john.setId(1L);
        
        john.setCreatedOn(LocalDateTime.now());
        john.setUserProfiles(userProfiles);

        // Testovaci objekt slouzici pro zaregistrovani noveho User uctu
        SignUpAndLoginRESTDto signUpAndLoginRESTDto = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTDto.setUserName(john.getUserName());
        signUpAndLoginRESTDto.setPassword(john.getPassword());
        signUpAndLoginRESTDto.setEmail(john.getEmail());
        signUpAndLoginRESTDto.setDeviceID(deviceID);

        given(userService.registerNewRESTUser(Mockito.any(SignUpAndLoginRESTDto.class))).willReturn(john); //registerNewRESTUser(registerRequest);
        given(authenticationService.login(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class))).willReturn(Optional.of(token));

        given(tokenCreateAndSendEmailService.createAndSendVerificationTokenEmail(Mockito.any(User.class), Mockito.any(Locale.class))).willReturn(token);

        // when
        ResponseEntity<AuthRESTResponse> authResponse = usersControllerPublicREST.register(signUpAndLoginRESTDto, Locale.getDefault()); 
        
        // then
        assertThat(authResponse.getStatusCodeValue()).isEqualTo((HttpStatus.OK.value()));
        assertThat(authResponse.getBody().getTokenType()).isEqualTo("Bearer");
        assertThat(authResponse.getBody().getAccessToken()).isEqualTo(token);
        verify(userService, VerificationModeFactory.times(1)).registerNewRESTUser(Mockito.eq(signUpAndLoginRESTDto));

        ArgumentCaptor<OnRegistrationCompleteEvent> sendEmailEventCapture = ArgumentCaptor.forClass(OnRegistrationCompleteEvent.class);
        verify(eventPublisher, VerificationModeFactory.times(1)).publishEvent(sendEmailEventCapture.capture());

        assertThat(john).isEqualTo(sendEmailEventCapture.getValue().getUser());
        assertThat(john.getUserName()).isEqualTo(sendEmailEventCapture.getValue().getUser().getUserName());
    }
    
}
