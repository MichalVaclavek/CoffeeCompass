package cz.fungisoft.coffeecompass.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
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

import javax.servlet.Filter;


/**
 * Zakladni nastaveni zabezpeceni pristupu na stranky, pristup k datum uzivatelu apod.
 * 
 * @author Michal Vaclavek
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /** REST endpoints security config **/
    private static final RequestMatcher PROTECTED_REST_URLS = new OrRequestMatcher(new AntPathRequestMatcher("/rest/secured/**"));
    
    private static final RequestMatcher PUBLIC_REST_URLS_MATCHERS = new OrRequestMatcher(new AntPathRequestMatcher("/rest/public/**"));

    private static final String PUBLIC_REST_URLS = "/rest/public/**";
    
    
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
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers(PUBLIC_REST_URLS);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Swithes off CSRF protection for REST i.e. for URL path with /rest/ at the begining
        http.csrf()
            .requireCsrfProtectionMatcher(new AndRequestMatcher(CsrfFilter.DEFAULT_CSRF_MATCHER, new RegexRequestMatcher("^(?!/rest/)", null)));
        
        http.authorizeRequests()
            .antMatchers("/","/home", "/about").permitAll()
            .antMatchers("/oauth2/**").permitAll()
            .antMatchers("/rest/public/**").permitAll()
            .antMatchers("/createModifySite/**", "/createSite", "/modifySite/**").hasAnyRole("ADMIN", "DBA", "USER")
            .antMatchers("/cancelStatusSite/**", "/deactivateSite/**", "/activateSite/**").hasAnyRole("ADMIN", "DBA", "USER")
            .antMatchers("/saveStarsAndComment/**", "/mySites").hasAnyRole("ADMIN", "DBA", "USER")
            .antMatchers("/finalDeleteSite/**").access("hasRole('ADMIN')") // real deletition of the CoffeeSite record
            .antMatchers("/user/all", "/user/show/**", "/rest/user/all").access("hasRole('ADMIN')")
            .antMatchers("/allSites/**").access("hasRole('ADMIN') OR hasRole('DBA')")
            .antMatchers("/user/delete/**").hasAnyRole("ADMIN", "DBA", "USER")
            .antMatchers("/user/edit-put", "/user/edit/**").hasAnyRole("ADMIN", "USER") // only USER itself or ADMIN can modify user account
            .antMatchers("/imageUpload", "/deleteImage/**").hasAnyRole("ADMIN", "DBA", "USER")
            .antMatchers("/user/updatePassword**", "/updatePassword**").hasAuthority("CHANGE_PASSWORD_PRIVILEGE")
            .and()
            .authenticationManager(authenticationConfiguration.getAuthenticationManager())
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
            .and()
            .formLogin().loginPage("/login").defaultSuccessUrl("/home", false).permitAll()
            .and()
            .logout().permitAll().logoutUrl("/logout")
            .logoutSuccessUrl("/home");
        
        // REST security login params
        http.exceptionHandling().defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), PROTECTED_REST_URLS)
            .and() 
            .addFilterBefore(restAuthenticationFilter(), AnonymousAuthenticationFilter.class)
            .authenticationManager(authenticationConfiguration.getAuthenticationManager())
            .authorizeRequests()
            .requestMatchers(PROTECTED_REST_URLS)
            .authenticated();
        
        // OAuth2 login parameters
        http.oauth2Login()
            .loginPage("/login")
            .defaultSuccessUrl("/oauth2/loginSuccess")
            .authorizationEndpoint()
            .baseUri("/oauth2/authorize")
            .authorizationRequestRepository(cookieAuthorizationRequestRepository())
            .and()
            .redirectionEndpoint()
            .baseUri("/oauth2/callback/*")
            .and()
            .userInfoEndpoint()
            .userService(customOAuth2UserService)
            .and()
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .failureHandler(oAuth2AuthenticationFailureHandler);

        return http.build();
    }
 
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
 
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
 
    @Bean
    public AuthenticationTrustResolver getAuthenticationTrustResolver() {
        return new AuthenticationTrustResolverImpl();
    }
    
    /**
     *   By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
     *   the authorization request. But, since our service is stateless, we can't save it in
     *   the session. We'll save the request in a Base64 encoded cookie instead.
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