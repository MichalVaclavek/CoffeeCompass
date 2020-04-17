package cz.fungisoft.coffeecompass.integrationtests.user.rest.publics;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
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
 * Database creation and connection is done via {@code PostgreSQLContainer}
 * which runs Postgres DB in Docker container. Therefore, the tests requier
 * running Docker on test machine.
 * The database structure/schema s created using {@code @Sql(scripts= {"/schema_integration_test_docker.sql"} }
 * annotation. If data are required for running the test, it has to be inserted using Repository
 * methods in  @Before public void setUp() method.
 * 
 * @author Michal Vaclavek
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev,dev_https"})
@ContextConfiguration(initializers = {UsersRESTIntegrationTests.Initializer.class})
@Sql(scripts= {"/schema_integration_test_docker.sql"},
     config = @SqlConfig(encoding = "utf-8",
     transactionMode = TransactionMode.ISOLATED))
public class UsersRESTIntegrationTests
{
    @Autowired
    private MockMvc mockMvc;
    
    
    // USER profile
    private static UserProfile userProfUser;
    private static Set<UserProfile> userProfiles = new HashSet<>();
    
    // ADMIN profile
    private static UserProfile userProfADMIN;
    private static Set<UserProfile> userProfilesADMIN = new HashSet<>();
    
    // DBA profile
    private static UserProfile userProfDBA;
    private static Set<UserProfile> userProfilesDBA = new HashSet<>();
    
    //private static String token = "xy";
    private static String deviceID = "4545454545";
    
    @Autowired
    private UserProfileRepository userProfileRepo;
    
    private User admin = new User();
    private SignUpAndLoginRESTDto signUpAndLoginRESTDtoAdmin;
    
    
    /**
     * Vlozeni zakladnich parametru Postgres DB bezici v Dockeru.
     */
    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres")
                                    .withDatabaseName("coffeecompass")
                                    .withUsername("postgres")
                                    .withPassword("postgres_test");

    
    @Before
    public void setUp() {
        
        userProfUser = new UserProfile();
        userProfUser.setType("USER");
        userProfileRepo.save(userProfUser);
        
        userProfiles.add(userProfUser);
               
        userProfADMIN = new UserProfile();
        userProfADMIN.setType("ADMIN");
        userProfileRepo.save(userProfADMIN);
        userProfilesADMIN.add(userProfADMIN);
        
        userProfDBA = new UserProfile();
        userProfDBA.setType("DBA");
        userProfileRepo.save(userProfDBA);
        userProfilesDBA.add(userProfDBA);

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
        john.setUserProfiles(userProfiles);

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
    
    /**
     * Provede nastaveni Spring/Hibernate promennych pro pripojeni do DB bezici v Docker containeru.
     * Provede se pred vytvorenim instance testovaci tridy.
     * 
     * @author Michal
     *
     */
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                "spring.jpa.show-sql=true",
                "spring.jpa.properties.hibernate.format_sql=true"
                //"spring.datasource.initialization-mode=always",
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
    
}
