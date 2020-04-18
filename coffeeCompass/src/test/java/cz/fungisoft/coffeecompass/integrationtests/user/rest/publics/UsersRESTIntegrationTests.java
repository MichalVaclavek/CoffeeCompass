package cz.fungisoft.coffeecompass.integrationtests.user.rest.publics;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.integrationtests.IntegrationTestBaseConfig;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRecordStatusRepository;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Timestamp;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;



/**
 * Integration test for all actions related to User object.
 * Whole context of Spring is loaded, using @SpringBootTest and
 * @ActiveProfiles({"dev,dev_https,test_postgres_docker"}) annotations.
 * <p>
 * Database is created in {@link IntegrationTestBaseConfig}
 * 
 * @author Michal Vaclavek
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
public class UsersRESTIntegrationTests extends IntegrationTestBaseConfig
{

    @Autowired
    private MockMvc mockMvc;
    
    //private static String token = "xy";
    private static String deviceID = "4545454545";
    
    private User admin = new User();
    private SignUpAndLoginRESTDto signUpAndLoginRESTDtoAdmin;
    
   
    
    @Before
    @Override
    public void setUp() {
        
        super.setUp(); 
        
        // ADMIN user profile returned, when requesting UserDetails
        admin.setUserName("admin");
        admin.setPassword("adminpassword");
        admin.setEmail("admin@boss.com");
        admin.setId(1L);
        admin.setCreatedOn(new Timestamp(new Date().getTime()));
        admin.setUserProfiles(userProfilesADMIN);
       

        // Testovaci objekt slouzici pro zaregistrovani noveho User uctu
        signUpAndLoginRESTDtoAdmin = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTDtoAdmin.setUserName(admin.getUserName());
        signUpAndLoginRESTDtoAdmin.setPassword(admin.getPassword());
        signUpAndLoginRESTDtoAdmin.setEmail(admin.getEmail());
        signUpAndLoginRESTDtoAdmin.setDeviceID(deviceID);
        
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
        john.setUserProfiles(userProfilesUser);

        // Testovaci objekt slouzici pro zaregistrovani noveho User uctu
        SignUpAndLoginRESTDto signUpAndLoginRESTDto = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTDto.setUserName(john.getUserName());
        signUpAndLoginRESTDto.setPassword(john.getPassword());
        signUpAndLoginRESTDto.setEmail(john.getEmail());
        signUpAndLoginRESTDto.setDeviceID(deviceID);

        // when and then
        mockMvc.perform(post("/rest/public/user/register").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(signUpAndLoginRESTDto)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.*", hasSize(3)))
               .andExpect(jsonPath("$.tokenType", Matchers.is("Bearer")))
               .andExpect(jsonPath("$.accessToken", Matchers.isA(String.class)))
               .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
    
    
    
}
