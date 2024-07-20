package cz.fungisoft.coffeecompass.configuration;

import java.util.Locale;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
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

import cz.fungisoft.coffeecompass.exceptions.GeneralErrorAttributes;

/**
 * Místo pro defaultní Configuration Beans Springu.
 * 
 * @author Michal Václavek
 */
@Configuration
@EnableCaching
public class CoffeeCompassConfiguration implements WebMvcConfigurer {

    /**
     * Caching config
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "coffeeSitesCache",
                "coffeeSiteTypesCache",
                "coffeeSortsCache",
                "coffeeSiteCommentsCache",
                "companiesCache",
                "csRecordStatusesCache",
                "csStatusesCache",
                "csTypesCache",
                "csNextToMachineTypesCache",
                "otherOffersCache",
                "priceRangesCache",
                "siteLocationTypesCache",
                "starsQualityRatingsCache",
                "starsQualityRatingsCache",
                "cupTypesCache",
                "usersCache",
                "userProfilesCache");
    }

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
     * We override the default {@code DefaultErrorAttributes} (see Spring doc.)
     *
     * @return A custom implementation of ErrorAttributes
     */
    @Bean
    public ErrorAttributes errorAttributes() {
        return new GeneralErrorAttributes();
    }

}