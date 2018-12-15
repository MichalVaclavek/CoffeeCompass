package cz.fungisoft.coffeecompass.configuration;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDto;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.dto.UserDataDto;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.User;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;


/**
 * Místo pro defaultní Configuration Beans Springu.
 * 
 * @author Michal Václavek
 */
@Configuration
public class CoffeeCompassConfiguration implements WebMvcConfigurer
{  
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        Locale csLocale = new Locale("cs", "CS");
        sessionLocaleResolver.setDefaultLocale(csLocale);
        return sessionLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry ir) {
        ir.addInterceptor(localeChangeInterceptor());
    }   
    
    /**
     * Interface a jeho implementace pro object, ktery se pouziva pro "mapovani" mezi zakladnimi Entity objekty
     * a prislusnymi DTO objekty, ktere se posilaji z Repository do Serice a View/Controller vrstvy.
     * 
     * @return
     */
    @Bean
    public MapperFacade mapperFacade() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        
        // Uprava pro mapovano z User na UserDataDTO - pro prenaseni na clienta neni potreba prenaset heslo a confirm hesla
        mapperFactory.classMap(User.class, UserDataDto.class).exclude("password")
                                                             .exclude("confirmPassword")
                                                             .byDefault()
                                                             .register();
        
        mapperFactory.classMap(UserDataDto.class, User.class).byDefault().register();

        // Only userName is needed for CoffeeSiteDto object
        mapperFactory.classMap(CoffeeSite.class, CoffeeSiteDto.class)
                                        .field("originalUser.userName", "originalUserName")
                                        .field("lastEditUser.userName", "lastEditUserName")
                                        .byDefault()
                                        .register();
        
        // Only userName and CoffeeSiteID is needed for CommentDTO object
        mapperFactory.classMap(Comment.class, CommentDTO.class)
                                        .field("user.userName", "userName")
                                        .field("coffeeSite.id", "coffeeSiteID")
                                        .byDefault()
                                        .register();
     
        return mapperFactory.getMapperFacade();
    }      
    
}