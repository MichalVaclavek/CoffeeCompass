package cz.fungisoft.coffeecompass.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

import cz.fungisoft.coffeecompass.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import cz.fungisoft.coffeecompass.security.oauth2.OAuth2AuthenticationFailureHandler;
import cz.fungisoft.coffeecompass.security.oauth2.OAuth2AuthenticationSuccessHandler;
import cz.fungisoft.coffeecompass.serviceimpl.CustomOAuth2UserService;

/**
 * Zakladni nastaveni zabezpeceni pristupu na stranky, pristup k datum uzivatelu apod.
 * 
 * @author Michal Vaclavek
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
    private UserDetailsService userDetailsService;
    
    private CustomOAuth2UserService customOAuth2UserService;
    
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    
    private AccessDeniedHandler accessDeniedHandler;
 
    /**
     * Dependency Injection pomoci konstruktoru. Preferovany zpusob i ve Spring.
     *    
     * @param userDetailsService
     * @param accessDeniedHandler
     */
    public SecurityConfiguration(@Qualifier("customUserDetailsService") UserDetailsService userDetailsService,
                                                                        CustomOAuth2UserService customOAuth2UserService,
                                                                        @Lazy
                                                                        OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                                                                        @Lazy
                                                                        OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                                                                        AccessDeniedHandler accessDeniedHandler) {
        super();
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
        auth.authenticationProvider(authenticationProvider());
    }
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {    

        http.authorizeRequests()
            .antMatchers("/","/home", "/about").permitAll()
            .antMatchers("/oauth2/**").permitAll()
            .antMatchers("/createModifySite/**", "/createSite", "/modifySite/**").hasAnyRole("ADMIN", "DBA", "USER")
            .antMatchers("/cancelStatusSite/**", "/deactivateSite/**", "/activateSite/**").hasAnyRole("ADMIN", "DBA", "USER")
            .antMatchers("/saveStarsAndComment/**", "/mySites").hasAnyRole("ADMIN", "DBA", "USER")
            .antMatchers("/finalDeleteSite/**").access("hasRole('ADMIN')") // real deletition of the CoffeeSite record
            .antMatchers("/user/all", "/user/show/**", "/rest/user/all").access("hasRole('ADMIN')")
            .antMatchers("/allSites").access("hasRole('ADMIN') OR hasRole('DBA')")
            .antMatchers("/user/delete/**").hasRole("ADMIN")
            .antMatchers("/user/edit-put", "/user/edit/**").hasAnyRole("ADMIN", "USER") // only USER itself or ADMIN can modify user account
            .antMatchers("/imageUpload", "/deleteImage/**").hasAnyRole("ADMIN", "DBA", "USER")
            .antMatchers("/user/updatePassword**", "/updatePassword**").hasAuthority("CHANGE_PASSWORD_PRIVILEGE")
            .and()
            .formLogin().loginPage("/login").defaultSuccessUrl("/home", false).permitAll()
            .and()
            .logout().permitAll().logoutUrl("/logout")
            .logoutSuccessUrl("/home")
            .and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
            .and()
            .oauth2Login() // OAuth2 login parameters
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
    
    /*
    By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
    the authorization request. But, since our service is stateless, we can't save it in
    the session. We'll save the request in a Base64 encoded cookie instead.
    */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }
    
}