package cz.fungisoft.coffeecompass.integrationtests.user.rest.secured;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.integrationtests.IntegrationTestBaseConfig;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.service.user.UserService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for all actions related to User object.
 * <p>
 * Whole context of Spring is loaded, using @SpringBootTest and @ActiveProfiles({"dev,dev_https,test_postgres_docker"}) annotations.
 * Database creation and connection is done via {@code PostgreSQLContainer} which runs Postgres DB in Docker container.<br>
 * Therefore, the tests requier running Docker on test machine.
 * <p>
 * The database structure/schema is created using {@code @Sql(scripts= {"/schema_integration_test_docker.sql"} }
 * annotation, if not used with Docker.
 * The database structure/schema is created using /schema_integration_test_docker.sql script by TestContainer objects
 * <p>
 * If data are required for running the test, it has to be inserted using Repository
 * methods in  @Before public void setUp() method.
 * 
 * @author Michal Vaclavek
 *
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Slf4j
class UsersRESTSecuredIntegrationTests extends IntegrationTestBaseConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    public UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsersRepository userRepo; // must be used repository to save User with encrypted password

    private final User admin = new User();

    private static final String ADMIN_PASSWORD = "adminpassword";
    

    /**
     * Creates User object withe ADMIN privileges/profile crearted earlier
     * and User registering DTO object to be used later in test as input 
     * REST JSON request body.
     */
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        
        // Save ADMIN user first
        admin.setUserName("admin");
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setEmail("admin@boss.com");
        admin.setCreatedOn(LocalDateTime.now());
        admin.setUserProfiles(userProfilesADMIN);
        userRepo.saveAndFlush(admin);
    }
 
    /**
     * To test if all Users are returned when requested. Only ADMIN user can request that.
     * First, mock ADMIN user is logged in. 
     * 
     * @throws Exception
     */
    @Test
    void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        // When login ADMIN user
        SignUpAndLoginRESTDto signUpAndLoginRESTuser = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTuser.setUserName(admin.getUserName());
        signUpAndLoginRESTuser.setPassword(ADMIN_PASSWORD); // real passwd.
        signUpAndLoginRESTuser.setEmail(admin.getEmail());
        String deviceID = "4545454545";
        signUpAndLoginRESTuser.setDeviceID(deviceID);

        String accessToken = loginUserAndGetAccessToken(mockMvc, signUpAndLoginRESTuser);

        // Create and save some normal Users - START
        User john = new User();
        john.setUserName("john");
        john.setEmail("john@vonneuman.com");
        john.setPassword("computer");
        john.setCreatedOn(LocalDateTime.now());
        userRepo.saveAndFlush(john);

        User mary = new User();
        mary.setUserName("mary");
        mary.setEmail("mary@gun.com");
        mary.setPassword("blood");
        mary.setCreatedOn(LocalDateTime.now());
        userRepo.saveAndFlush(mary);

        User dick = new User();        
        dick.setUserName("dick");
        dick.setEmail("dick@feynman.com");
        dick.setPassword("qed");
        dick.setCreatedOn(LocalDateTime.now());
        userRepo.saveAndFlush(dick);
        // Create and save some normal Users - END -
        
        // Then request all users will be returned
        mockMvc.perform(get("/api/v1/coffeesites/secured/user/all")
               .header("Authorization", "Bearer " + accessToken)
               .accept("application/json"))
               .andDo((result) -> log.info(result.getResponse().getContentAsString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(4)));
    }
}
