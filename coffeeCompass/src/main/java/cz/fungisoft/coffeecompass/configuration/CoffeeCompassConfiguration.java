package cz.fungisoft.coffeecompass.configuration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import cz.fungisoft.coffeecompass.domain.weather.WeatherData;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.dto.WeatherDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exceptions.GeneralErrorAttributes;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Místo pro defaultní Configuration Beans Springu.
 * 
 * @author Michal Václavek
 */
@Configuration
public class CoffeeCompassConfiguration implements WebMvcConfigurer {
    /**
     * Implementace rozhrani pro nastaveni Locale, zmenu jazyka
     * 
     * @return
     */
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
     * Implementace rozhrani pro "mapovani" mezi zakladnimi Entity objekty a prislusnymi DTO objekty,
     * ktere se posilaji ze Service do View/Controller vrstvy.
     * 
     * @return
     */
    @Bean
    public MapperFacade mapperFacade() {
        
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        
        // Uprava pro mapovano z User na UserDataDTO - pro prenaseni na clienta neni potreba prenaset heslo
        mapperFactory.classMap(User.class, UserDTO.class)
                     .mapNulls(false)
                     .exclude("password")
                     .byDefault()
                     .register();
        
        mapperFactory.classMap(UserDTO.class, User.class)
                     .mapNulls(false)
                     .byDefault()
                     .register();
        
        // Only userName is needed for CoffeeSiteDto object
        mapperFactory.classMap(CoffeeSite.class, CoffeeSiteDTO.class)
                     .field("originalUser.userName", "originalUserName")
                     .field("lastEditUser.userName", "lastEditUserName")
                     .byDefault()
                     .register();
        
        // Only userName and CoffeeSiteID is needed for CommentDTO object
        mapperFactory.classMap(Comment.class, CommentDTO.class)
                     .field("user.userName", "userName")
                     .field("coffeeSite.id", "coffeeSiteID")
                     .field("user.id", "userId")
                     .byDefault()
                     .register();
        
        // Mapping between WeatherData class obtained from openweather API to
        // WeatherDTO used to sent to client within CoffeeSiteDTO object
        WeatherCustomMapper customMapper = new WeatherCustomMapper();
        
        mapperFactory.classMap(WeatherData.class, WeatherDTO.class)
                     .customize(customMapper)
                     .byDefault()
                     .register();
        
        return mapperFactory.getMapperFacade();
    }    
    
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
    
    /**
     * Defines source of validation error messages. In this case is same like all other i18n messages, see @Bean above.
     * 
     * @param messageSource
     * @return
     */
    @Bean
    public LocalValidatorFactoryBean getValidatorMessages(MessageSource messageSource) {
        LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.setValidationMessageSource(messageSource);
        return validatorFactory;
    }
    
    /**
     * We override the default {@link DefaultErrorAttributes}
     *
     * @return A custom implementation of ErrorAttributes
     */
    @Bean
    public ErrorAttributes errorAttributes() {
        return new GeneralErrorAttributes();
    }
    
    /**
     * Inner class to define Custom Mapper between {@code WeatherData.sys.sunrise} and {@code WeatherData.sys.sunset}
     * into {@code WeatherDTO.sunRiseTime} and {@code WeatherDTO.sunSetTime}
     * 
     * @author Michal V.
     *
     */
    static class WeatherCustomMapper extends CustomMapper<WeatherData, WeatherDTO> {
        
        @Override
        public void mapAtoB(WeatherData a, WeatherDTO b, MappingContext context) {
            
            LocalDateTime sunriseTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(a.getSys().getSunrise() * 1000), 
                                            TimeZone.getDefault().toZoneId());  
            b.setSunRiseTime(sunriseTime.toLocalTime());
            
            LocalDateTime sunsetTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(a.getSys().getSunset() * 1000), 
                                            TimeZone.getDefault().toZoneId());  
            b.setSunSetTime(sunsetTime.toLocalTime());
        }
    }
    
}