package cz.fungisoft.coffeecompass.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
    private UserDetailsService userDetailsService;
    
    private AccessDeniedHandler accessDeniedHandler;
 
    /**
     * Dependency Injection pomoci konstruktoru. Preferovany zpusob i ve Spring.
     *    
     * @param userDetailsService
     * @param accessDeniedHandler
     */
    @Autowired
    public SecurityConfiguration(@Qualifier("customUserDetailsService") UserDetailsService userDetailsService, AccessDeniedHandler accessDeniedHandler) {
        super();
        this.userDetailsService = userDetailsService;
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
                .antMatchers("/createModifySite/**", "/createSite", "/modifySite/**").authenticated()
                .antMatchers("/cancelStatusSite/**", "/deactivateSite/**", "/activateSite/**").authenticated()
                .antMatchers("/saveStarsAndComment/**", "/mySites").authenticated()
                .antMatchers("/finalDeleteSite/**").access("hasRole('ADMIN')") // real deletition of the CoffeeSite record
                .antMatchers("/user/all", "/user/show/**").access("hasRole('ADMIN')")
                .antMatchers("/allSites").access("hasRole('ADMIN') or hasRole('DBA')")
                .antMatchers("/user/delete/**").hasAnyRole("ADMIN")
                .antMatchers("/user/edit-put", "/user/edit/**").authenticated()
                .antMatchers("/imageUpload", "/deleteImage/**").authenticated()
                .and()
                .formLogin().loginPage("/login").defaultSuccessUrl("/home", false).permitAll()
                .and()
                .logout().permitAll().logoutUrl("/logout")
                .logoutSuccessUrl("/home")
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler);        
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
    
}