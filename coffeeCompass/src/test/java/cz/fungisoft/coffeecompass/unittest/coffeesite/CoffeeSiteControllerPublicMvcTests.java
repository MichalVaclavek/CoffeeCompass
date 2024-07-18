package cz.fungisoft.coffeecompass.unittest.coffeesite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import cz.fungisoft.coffeecompass.mappers.CoffeeSiteMapperImpl;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import cz.fungisoft.coffeecompass.unittest.MvcControllerUnitTestBaseSetup;
import cz.fungisoft.coffeecompass.unittest.user.UserControllerMvcTest;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cz.fungisoft.coffeecompass.controller.rest.CoffeeSiteControllerPublicREST;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.testutils.CoffeeSiteFactory;

/**
 * Testovani Public REST Controller vrstvy pro praci s CoffeeSite s vyuzitim Spring MVC.
 *
 * @author Michal Vaclavek
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CoffeeSiteMapperImpl.class})
@WebMvcTest
@Import(CoffeeSiteControllerPublicMvcTests.CoffeeSiteControllerTestContextConfiguration.class)
class CoffeeSiteControllerPublicMvcTests extends MvcControllerUnitTestBaseSetup {

    private static final String PUBLIC_REST_URLS = "/api/v1/coffeesites/**";

    private MockMvc mockMvc;

    /**
     * Controller to be tested here
     */
//    private CoffeeSiteControllerPublicREST coffeeSiteControllerPublic;

//    @Autowired
//    @Qualifier("customUserDetailsService")
//    protected UserDetailsService customUserDetailsService;

    @TestConfiguration
    protected static class CoffeeSiteControllerTestContextConfiguration {

        @MockBean //provided by Spring Context
        public static UserService userService;

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
            return web -> web.ignoring().requestMatchers(PUBLIC_REST_URLS);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers("/api/v1/coffeesites/**").permitAll()
                            .anyRequest().authenticated());

            return http.build();
        }

        @Bean("customUserDetailsService")
        public UserDetailsService customUserDetailsService() {
            return new CustomUserDetailsService(userService);
        }
    }


    @BeforeEach
    public void setUp() {
//        coffeeSiteControllerPublic = new CoffeeSiteControllerPublicREST(coffeeSiteService, starsForCoffeeSiteAndUserService);
//        mvc = MockMvcBuilders.standaloneSetup(coffeeSiteControllerPublic).build();
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()) // nacte nastaveni vnucene pomoci WithMockCustomUserSecurityContextFactory
                .build();

//        given(CoffeeSiteControllerPublicMvcTests.CoffeeSiteControllerTestContextConfiguration.userService.isEmailUnique(Mockito.any(), Mockito.any(String.class))).willReturn(true); //registerNewRESTUser(registerRequest);
    }

    /**
     * Tests if list of all CoffeeSites are returned after /api/v1/coffeesites/site/allSites REST request.
     *
     * @throws Exception
     */
    @Test
    void givenCoffeeSites_whenGetSites_thenReturnJsonArray() throws Exception {

        CoffeeSite cs1 = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite1", "automat");
        CoffeeSiteDTO cs1Dto = coffeeSiteMapper.coffeeSiteToCoffeeSiteDTO(cs1);

        CoffeeSite cs2 = CoffeeSiteFactory.getCoffeeSite("ControllerTestSite2", "automat");
        CoffeeSiteDTO cs2Dto = coffeeSiteMapper.coffeeSiteToCoffeeSiteDTO(cs2);

        List<CoffeeSiteDTO> allSites = Arrays.asList(cs1Dto, cs2Dto);

        given(coffeeSiteService.findAll(Mockito.anyString(), Mockito.anyString())).willReturn(allSites);

        mockMvc.perform(get("/api/v1/coffeesites/site/allSites")

                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].siteName", is(cs1.getSiteName())));

        verify(coffeeSiteService, VerificationModeFactory.times(1)).findAll("id", "asc");
    }
}
