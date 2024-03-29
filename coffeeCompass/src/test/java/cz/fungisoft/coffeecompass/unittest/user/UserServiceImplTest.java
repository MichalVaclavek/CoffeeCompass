package cz.fungisoft.coffeecompass.unittest.user;

import cz.fungisoft.coffeecompass.configuration.ConfigProperties;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.mappers.UserMapper;
import cz.fungisoft.coffeecompass.repository.PasswordResetTokenRepository;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.repository.UserEmailVerificationTokenRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.security.IAuthenticationFacade;
import cz.fungisoft.coffeecompass.security.SecurityConfiguration;
import cz.fungisoft.coffeecompass.service.user.UserSecurityService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import cz.fungisoft.coffeecompass.serviceimpl.user.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testuje Service vrstvu pro praci s objekty User.
 * 
 * @author Michal Vaclavek
 *
 */
@ExtendWith(SpringExtension.class)
public class UserServiceImplTest {

    @MockBean
    private static UserProfileRepository userProfileRepository;

    @MockBean
    private static IAuthenticationFacade authenticationFacade;
    
    @MockBean
    private CustomUserDetailsService userDetService;

    @MockBean
    private static UserSecurityService userSecurityService;

    @MockBean
    private static UserEmailVerificationTokenRepository userEmailVerificationTokenRepository;

    @MockBean
    private static PasswordResetTokenRepository passwordResetTokenRepository;
    
    @MockBean
    private static ConfigProperties configProps;
    
    @MockBean
    private SecurityConfiguration securityConfig;
    
    @TestConfiguration
    static class UserSiteServiceImplTestContextConfiguration {
 
        private PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        
        @MockBean
        private UserMapper userMapper;
        
        @MockBean
        public static UsersRepository usersRepository;
        
        @Bean
        public UserService userService() {
            return new UserServiceImpl(usersRepository, passwordEncoder, userMapper, null, userEmailVerificationTokenRepository, passwordResetTokenRepository, userSecurityService, configProps);
        }
    }
    
    @Autowired
    private UserService userService;
 
    private UserProfile userProfUser;

    private static String userName = "bert";
    
    private Set<UserProfile> userProfiles;
    
    
    @BeforeEach
    public void setUp() {
        userProfUser = new UserProfile();
        userProfUser.setType("USER");
        
        User genius = new User();
        
        genius.setUserName(userName);
        genius.setFirstName("Albert");
        genius.setLastName("Einstein");
        
        String emailAddr = "bert@princeton.edu";
        genius.setEmail(emailAddr);
        genius.setPassword("gravity");

        userProfiles = new HashSet<>();
        userProfiles.add(userProfUser);
        genius.setUserProfiles(userProfiles);      
               
        Mockito.when(UserSiteServiceImplTestContextConfiguration.usersRepository.searchByUsername(Mockito.contains(userName)))
               .thenReturn(Optional.of(genius));
    }
    
    @Test
    void whenValidName_thenUserShouldBeFound() {

        String passwd = "gravity";
        Optional<User> found = userService.findByUserName(userName);
      
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getUserName()).isEqualTo(userName);
        assertThat(found.get().getPassword()).isEqualTo(passwd);
        assertThat(found.get().getUserProfiles()).isEqualTo(userProfiles);
    } 

}
