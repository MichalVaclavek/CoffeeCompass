package cz.fungisoft.coffeecompass.integrationtests.user.rest.secured;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.service.*;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;

import lombok.NonNull;
import ma.glasnost.orika.MapperFacade;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
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
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Timestamp;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@ActiveProfiles({"dev"})
@ContextConfiguration(initializers = {UsersRESTIntegrationTests.Initializer.class})
@Sql(scripts= {"/schema_integration_test_docker.sql"},
     config = @SqlConfig(encoding = "utf-8",
     transactionMode = TransactionMode.ISOLATED))
public class UsersRESTIntegrationTests
{
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    public UserService userService;
    
    @Autowired
    public MapperFacade mapperFacade;
    
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
        
        UserDTO adminDto = mapperFacade.map(admin, UserDTO.class);
        userService.save(adminDto);
       
        // Testovaci objekt slouzici pro zaregistrovani noveho User uctu
        signUpAndLoginRESTDtoAdmin = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTDtoAdmin.setUserName(admin.getUserName());
        signUpAndLoginRESTDtoAdmin.setPassword(admin.getPassword());
        signUpAndLoginRESTDtoAdmin.setEmail(admin.getEmail());
        signUpAndLoginRESTDtoAdmin.setDeviceID(deviceID);
        
    }
 
    /**
     * To test if all Users are returned when requested. Only ADMIN user can request that.
     * First, mock ADMIN user is logged in. 
     * 
     * @throws Exception
     */
    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {    
        
        // Create and save some normal Users - START
        User john = new User();
        john.setUserName("john");
        john.setEmail("john@vonneuman.com");
        john.setPassword("computer");
        UserDTO johnDto = mapperFacade.map(john, UserDTO.class);
        userService.save(johnDto);
        
        User mary = new User();
        mary.setUserName("mary");
        mary.setEmail("mary@gun.com");
        mary.setPassword("blood");
        UserDTO maryDto = mapperFacade.map(mary, UserDTO.class);
        userService.save(maryDto);
        
        User dick = new User();        
        dick.setUserName("dick");
        dick.setEmail("dick@feynman.com");
        dick.setPassword("qed");
        UserDTO dickDto = mapperFacade.map(dick, UserDTO.class);
        userService.save(dickDto);
        // Create and save some normal Users - END
        
        // When login ADMIN user
        String accessToken = obtainAccessToken(signUpAndLoginRESTDtoAdmin);
        
        // Then request all users will be returned
        mockMvc.perform(get("/rest/secured/user/all")
               .header("Authorization", "Bearer " + accessToken)
               .accept("application/json;charset=UTF-8"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(4)));
        
    }
    
    
    private String obtainAccessToken(SignUpAndLoginRESTDto userDto) throws Exception {
        
        ResultActions result = mockMvc.perform(post("/rest/public/user/login").contentType(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(userDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"));
     
        String resultString = result.andReturn().getResponse().getContentAsString();
     
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("accessToken").toString();
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
