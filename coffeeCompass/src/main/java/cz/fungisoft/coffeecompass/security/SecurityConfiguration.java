package cz.fungisoft.coffeecompass.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import cz.fungisoft.coffeecompass.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import cz.fungisoft.coffeecompass.security.oauth2.OAuth2AuthenticationFailureHandler;
import cz.fungisoft.coffeecompass.security.oauth2.OAuth2AuthenticationSuccessHandler;
import cz.fungisoft.coffeecompass.security.rest.TokenAuthenticationFilter;
import cz.fungisoft.coffeecompass.service.user.UserSecurityService;
import cz.fungisoft.coffeecompass.serviceimpl.user.CustomOAuth2UserService;

import jakarta.servlet.Filter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;


/**
 * Zakladni nastaveni zabezpeceni pristupu na stranky, pristup k datum uzivatelu apod.
 * 
 * @author Michal Vaclavek
 *
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {

    /** REST endpoints security config **/
    private static final RequestMatcher PROTECTED_REST_URLS = new OrRequestMatcher(new AntPathRequestMatcher("/rest/secured/**"));
    
    private static final String PUBLIC_REST_URLS = "/rest/public/**";

//    private static final RequestMatcher PUBLIC_REST_URLS_MATCHERS = new OrRequestMatcher(new AntPathRequestMatcher(PUBLIC_REST_URLS));


    private final UserSecurityService userSecurityService;
          
    private final UserDetailsService userDetailsService;
    
    private final CustomOAuth2UserService customOAuth2UserService;
    
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    
    private final AccessDeniedHandler accessDeniedHandler;

    private final AuthenticationConfiguration authenticationConfiguration;


    public SecurityConfiguration(@Qualifier("customUserDetailsService")
                                 UserDetailsService userDetailsService,
                                 CustomOAuth2UserService customOAuth2UserService,
                                 @Lazy
                                 OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                                 @Lazy
                                 OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                                 AccessDeniedHandler accessDeniedHandler,
                                 UserSecurityService userSecurityService,
                                 AuthenticationConfiguration authenticationConfiguration) {
        super();
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.userSecurityService = userSecurityService;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    /**
     * Mobile app config.
     */
//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return web -> web.ignoring().requestMatchers(PUBLIC_REST_URLS);
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Swithes off CSRF protection for REST i.e. for URL path with /rest/ at the begining
        http.csrf(csrfConfigurer -> csrfConfigurer.requireCsrfProtectionMatcher(new AndRequestMatcher(CsrfFilter.DEFAULT_CSRF_MATCHER, new RegexRequestMatcher("^(?!/rest/)", null))));
//            .requireCsrfProtectionMatcher(new AndRequestMatcher(CsrfFilter.DEFAULT_CSRF_MATCHER, new RegexRequestMatcher("^(?!/rest/)", null)));
        
        http.authorizeHttpRequests(authz -> authz
            .requestMatchers("/**","/home", "/about").permitAll()
            .requestMatchers("/oauth2/**").permitAll()
            .requestMatchers(PUBLIC_REST_URLS).permitAll()
            .requestMatchers("/createModifySite/**", "/createSite", "/modifySite/**").hasAnyRole("ADMIN", "DBA", "USER")
            .requestMatchers("/cancelStatusSite/**", "/deactivateSite/**", "/activateSite/**").hasAnyRole("ADMIN", "DBA", "USER")
            .requestMatchers("/saveStarsAndComment/**", "/mySites").hasAnyRole("ADMIN", "DBA", "USER")
            .requestMatchers("/finalDeleteSite/**").hasRole("ADMIN") // real deletition of the CoffeeSite record
            .requestMatchers("/user/all", "/user/show/**", "/rest/user/all").hasRole("ADMIN")
            .requestMatchers("/allSites**").hasAnyRole("ADMIN", "DBA")
            .requestMatchers("/user/delete/**").hasAnyRole("ADMIN", "DBA", "USER")
            .requestMatchers("/user/edit-put", "/user/edit/**").hasAnyRole("ADMIN", "USER") // only USER itself or ADMIN can modify user account
            .requestMatchers("/imageUpload", "/deleteImage/**").hasAnyRole("ADMIN", "DBA", "USER")
            .requestMatchers("/user/updatePassword**", "/updatePassword**").hasAuthority("CHANGE_PASSWORD_PRIVILEGE")

        );

        http.authenticationManager(authenticationConfiguration.getAuthenticationManager())
            .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler));

        http.formLogin(customizer -> customizer.loginPage("/login")
                                               .defaultSuccessUrl("/home", false)
                                               .permitAll())
            .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer.logoutUrl("/logout")
                                                                                .logoutSuccessUrl("/home").permitAll()
            );

        // REST security login params
        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), PROTECTED_REST_URLS))
            .addFilterBefore(restAuthenticationFilter(), AnonymousAuthenticationFilter.class)
            .authenticationManager(authenticationConfiguration.getAuthenticationManager())
//            .authorizeRequests()
            .authorizeHttpRequests(authz -> authz.requestMatchers(PROTECTED_REST_URLS).authenticated());
//            .securityMatcher(PROTECTED_REST_URLS);
//            .authenticated();
        
        // OAuth2 login parameters
//        http.oauth2Login()
//            .loginPage("/login")
//            .defaultSuccessUrl("/oauth2/loginSuccess")
//            .authorizationEndpoint()
//            .baseUri("/oauth2/authorize")
//            .authorizationRequestRepository(cookieAuthorizationRequestRepository())
//            .and()
//            .redirectionEndpoint()
//            .baseUri("/oauth2/callback/*")
//            .and()
//            .userInfoEndpoint()
//            .userService(customOAuth2UserService)
//            .and()
//            .successHandler(oAuth2AuthenticationSuccessHandler)
//            .failureHandler(oAuth2AuthenticationFailureHandler);

//        http.oauth2Login(withDefaults());
        http.oauth2Login(httpSecurityOAuth2LoginConfigurer -> httpSecurityOAuth2LoginConfigurer
                .loginPage("/login")
                .defaultSuccessUrl("/oauth2/loginSuccess")
                .authorizationEndpoint(authorization -> authorization.baseUri("/oauth2/authorize"))
                .redirectionEndpoint(redirection -> redirection.baseUri("/oauth2/callback/**"))
//                .tokenEndpoint(token -> token.accessTokenResponseClient()			    )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService))
                );

        http.oauth2Client(oauth2 -> oauth2
                        .authorizationCodeGrant(codeGrant -> codeGrant
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                        )
                );

        return http.build();
    }


//    @Bean
//    GrantedAuthoritiesMapper userAuthoritiesMapper() {
//        return authorities -> {
//            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
//
//            authorities.forEach(authority -> {
//                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
////                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority)authority;
//
//                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
//                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();
//
//                    // Map the claims found in idToken and/or userInfo
//                    // to one or more GrantedAuthority's and add it to mappedAuthorities
//
//                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
////                    OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority)authority;
//
//                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();
//
//                    // Map the attributes found in userAttributes
//                    // to one or more GrantedAuthority's and add it to mappedAuthorities
//
//                }
//            });
//
//            return mappedAuthorities;
//        };
//    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                 OAuth2AuthorizedClientRepository authorizedClientRepository) {
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
 
//    @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
//        authenticationProvider.setUserDetailsService(userDetailsService);
//        authenticationProvider.setPasswordEncoder(passwordEncoder());
//        return authenticationProvider;
//    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }
 
//    @Bean
//    public AuthenticationTrustResolver getAuthenticationTrustResolver() {
//        return new AuthenticationTrustResolverImpl();
//    }
    
    /**
     * By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
     * the authorization request. But, since our service is stateless, we can't save it in
     * the session. We'll save the request in a Base64 encoded cookie instead.
    */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }
    
    /** Mobile app. REST config. **/
    
    @Bean
    TokenAuthenticationFilter restAuthenticationFilter() throws Exception {
        final TokenAuthenticationFilter filter = new TokenAuthenticationFilter(PROTECTED_REST_URLS, userSecurityService);
        filter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
        filter.setAuthenticationSuccessHandler(successHandler());
        return filter;
    }

    @Bean
    SimpleUrlAuthenticationSuccessHandler successHandler() {
        final SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setRedirectStrategy(new NoRedirectStrategy());
        return successHandler;
    }

    /**
     * Disable Spring boot automatic filter registration.
     */
    @Bean
    FilterRegistrationBean<Filter> disableAutoRegistration(final TokenAuthenticationFilter filter) {
        final FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    AuthenticationEntryPoint forbiddenEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }
}