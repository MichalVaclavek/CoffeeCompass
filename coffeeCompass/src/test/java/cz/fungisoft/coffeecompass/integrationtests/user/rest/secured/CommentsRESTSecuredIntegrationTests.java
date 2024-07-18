package cz.fungisoft.coffeecompass.integrationtests.user.rest.secured;

import cz.fungisoft.coffeecompass.controller.models.StarAndCommentForSiteModel;
import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.integrationtests.IntegrationTestBaseConfig;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.comment.ICommentService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for all actions related to Comments and Star rating object.
 * <p>
 * Whole context of Spring is loaded, using @SpringBootTest and  @ActiveProfiles({"dev,dev_https,test_postgres_docker"}) annotations.
 * Database creation and connection is done via {@code PostgreSQLContainer} which runs Postgres DB in Docker container.<br>
 * Therefore, the tests requier running Docker on test machine.
 * <p>
 * The database structure/schema is created using /schema_integration_test_docker.sql script by TestContainer objects.
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
class CommentsRESTSecuredIntegrationTests extends IntegrationTestBaseConfig {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    public UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsersRepository userRepo; // must be used repository to save User, who created CoffeeSite, immediately

    @Autowired
    public CoffeeSiteService coffeeSiteService;
    
    @Autowired
    private ICommentService commentsService;
    
    private static String deviceID = "4545454545";
    
    private User testUser;
    private SignUpAndLoginRESTDto signUpAndLoginRESTuser;


    private static final String SITE_1_NAME = "TestSite_1";
    private static final String SITE_2_NAME = "TestSite_2";
    
    // Test CoffeeSites
    private CoffeeSite cs1;
    private CoffeeSite cs2;
    
    private final String USER_PASSWORD = "computer";


    /**
     * Creates User object withe USER privileges/profile and it's User registering DTO object
     * to be used later in test as input.<br>
     * Creates 2 test CoffeeSites, too.
     */
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        // Create and save some normal User
        testUser = new User();
        testUser.setUserName("john");
        testUser.setEmail("john@vonneuman.com");
        testUser.setPassword(passwordEncoder.encode(USER_PASSWORD)); // encoded password
        testUser.setUserProfiles(userProfilesUser);
        testUser.setCreatedOn(LocalDateTime.now());
        
        userRepo.saveAndFlush(testUser);
       
        // Testovaci objekt slouzici pro login User uctu
        signUpAndLoginRESTuser = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTuser.setUserName(testUser.getUserName());
        signUpAndLoginRESTuser.setPassword(USER_PASSWORD); // real passwd.
        signUpAndLoginRESTuser.setEmail(testUser.getEmail());
        signUpAndLoginRESTuser.setDeviceID(deviceID);
        
        // Create 2 CoffeeSites
        cs1 = getCoffeeSiteBasedOnDB(SITE_1_NAME, "automat");
        
        cs1.setZemDelka(14.51122233d);
        cs1.setZemSirka(50.456566d);
        cs1.setRecordStatus(CREATED);
        cs1.setOriginalUser(testUser);
        
        coffeeSiteService.save(cs1);
        
        cs2 = getCoffeeSiteBasedOnDB(SITE_2_NAME, "automat");
        
        cs2.setZemDelka(14.12121212d);
        cs2.setZemSirka(50.87878787d);
        cs2.setRecordStatus(CREATED);
        cs2.setOriginalUser(testUser);
        
        coffeeSiteService.save(cs2);
    }
 
    /**
     * To test if REST request for saving more comments performs correctly - i.e. Comments
     * are saved in DB. 
     * 
     * @throws Exception
     */
    @Test
    void givenUser_and_CoffeeSites_whenMoreCommentsToBeSavedByREST_thenCommentsAreSavedInDB() throws Exception {
        // Get current numberOf Comments for CoffeeSite1 and CoffeeSite2
        int origNumOfCommentsSite1 = commentsService.getNumberOfCommentsForSiteId(cs1.getId());
        int origNumOfCommentsSite2 = commentsService.getNumberOfCommentsForSiteId(cs2.getId());
        
        // Create test JSON request with 2 Comments from 1 user to 2 CoffeeSites to be saved
        List<StarAndCommentForSiteModel> starsAndComments = new ArrayList<>();
        StarAndCommentForSiteModel commentStars1 = new StarAndCommentForSiteModel();
        commentStars1.setCoffeeSiteExtId(cs1.getExternalId().toString());
        commentStars1.setComment("Docela dobra kávička");
        commentStars1.setStars(4);
        starsAndComments.add(commentStars1);
        
        StarAndCommentForSiteModel commentStars2 = new StarAndCommentForSiteModel();
        commentStars2.setCoffeeSiteExtId(cs2.getExternalId().toString());
        commentStars2.setComment("Káva nic moc.");
        commentStars2.setStars(2);
        starsAndComments.add(commentStars2);
        
        // Get users login access token
        String accessToken = loginUserAndGetAccessToken(mockMvc, signUpAndLoginRESTuser);
        
        mockMvc.perform(post("/api/v1/coffeesites/secured/starsAndComments/saveStarsAndComments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(starsAndComments)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // Validates that there is one more Comment in DB for for CoffeeSite1 and CoffeeSite2
        assertEquals(origNumOfCommentsSite1 + 1, commentsService.getNumberOfCommentsForSiteId(cs1.getId()), "Number of comments not as expected for CoffeeSite 1");
        assertEquals(origNumOfCommentsSite2 + 1, commentsService.getNumberOfCommentsForSiteId(cs2.getId()), "Number of comments not as expected for CoffeeSite 2");
    }
}
