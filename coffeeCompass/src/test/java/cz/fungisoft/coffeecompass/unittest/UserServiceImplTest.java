package cz.fungisoft.coffeecompass.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepositoryCustom;
import cz.fungisoft.coffeecompass.repository.UsersRepositoryCustomImpl;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.security.IAuthenticationFacade;
import cz.fungisoft.coffeecompass.security.SecurityConfiguration;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteServiceImpl;
import cz.fungisoft.coffeecompass.serviceimpl.UserServiceImpl;
import ma.glasnost.orika.MapperFacade;

/**
 * Testuje Service vrstvu pro praci s objekty User.
 * 
 * @author Michal Vaclavek
 *
 */
@RunWith(SpringRunner.class)
//@SpringBootTest
public class UserServiceImplTest
{
    
    @MockBean
    public static UserProfileRepository userProfileRepository;

    @MockBean
    public static IAuthenticationFacade authenticationFacade;
    
    @MockBean
    CustomUserDetailsService userDetService;
    
    @MockBean
    SecurityConfiguration securityConfig;
    
    @TestConfiguration
    static class UserSiteServiceImplTestContextConfiguration {
 
        private PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        
        @MockBean
        private MapperFacade mapperFacade;
        
        @MockBean
        public static UsersRepository usersRepository;
        
        @Bean
        public UserService userService() {
            return new UserServiceImpl(usersRepository, passwordEncoder, mapperFacade);
        }
    }
    
    @Autowired
    private UserService userService;
 
    
       
    private UserProfile userProfUser;
    
    
    @Before
    public void setUp() {
        userProfUser = new UserProfile();
        userProfUser.setType("USER");
        
        User genius = new User();
        
        genius.setUserName("bert");
        genius.setFirstName("Albert");
        genius.setLastName("Einstein");
        
        String emailAddr = "bert@princeton.edu";
        genius.setEmail(emailAddr);
        genius.setPassword("gravity");
        
        
        Set<UserProfile> userProfiles = new HashSet<UserProfile>();
        userProfiles.add(userProfUser);
        genius.setUserProfiles(userProfiles);      
               
        Mockito.when(UserSiteServiceImplTestContextConfiguration.usersRepository.searchByUsername(genius.getUserName()))
               .thenReturn(genius);

    }
    
    @Test
    public void whenValidName_thenUserShouldBeFound() {
        String name = "bert";
        String passwd = "gravity";
        User found = userService.findByUserName(name);
      
        assertThat(found.getUserName()).isEqualTo(name);
        assertThat(found.getPassword()).isEqualTo(passwd);
    } 

}
