package cz.fungisoft.coffeecompass.integrationtests.user.rest.publics;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.integrationtests.IntegrationTestBaseConfig;
import cz.fungisoft.coffeecompass.testutils.JsonUtil;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Integration test for all actions related to User object.
 * Whole context of Spring is loaded, using @SpringBootTest and
 * @ActiveProfiles({"dev"}) annotations.
 * <p>
 * Database is created in {@link IntegrationTestBaseConfig}
 * 
 * @author Michal Vaclavek
 *
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
class UsersRESTIntegrationTests extends IntegrationTestBaseConfig {

    @Autowired
    private MockMvc mockMvc;
    
    private static final String deviceID = "4545454545";
    
    private final User admin = new User();


    @BeforeEach
    @Override
    public void setUp() {
        
        super.setUp(); 
        
        // ADMIN user profile returned, when requesting UserDetails
        admin.setUserName("admin");
        admin.setPassword("adminpassword");
        admin.setEmail("admin@boss.com");
        admin.setLongId(1L);
        admin.setCreatedOn(LocalDateTime.now());
        admin.setUserProfiles(userProfilesADMIN);
    }
 
    /**
     * Integration test for registering a new User entity via REST {@code /rest/public/user/register} endpoint
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
        john.setLongId(1L);
        
        john.setCreatedOn(LocalDateTime.now());
        john.setUserProfiles(userProfilesUser);

        // Testovaci objekt slouzici pro zaregistrovani noveho User uctu
        SignUpAndLoginRESTDto signUpAndLoginRESTDto = new SignUpAndLoginRESTDto();
        signUpAndLoginRESTDto.setUserName(john.getUserName());
        signUpAndLoginRESTDto.setPassword(john.getPassword());
        signUpAndLoginRESTDto.setEmail(john.getEmail());
        signUpAndLoginRESTDto.setDeviceID(deviceID);

        // when and then
        mockMvc.perform(post("/api/v1/coffeesites/public/user/register").contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(signUpAndLoginRESTDto)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.*", hasSize(4)))
               .andExpect(jsonPath("$.tokenType", Matchers.is("Bearer")))
               .andExpect(jsonPath("$.accessToken", Matchers.isA(String.class)))
               .andExpect(jsonPath("$.accessToken").isNotEmpty())
               .andExpect(jsonPath("$.refreshToken", Matchers.isA(String.class)))
               .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }
}
