package cz.fungisoft.coffeecompass.configuration;

import java.util.Locale;

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

import cz.fungisoft.coffeecompass.exceptions.GeneralErrorAttributes;
import org.thymeleaf.spring5.SpringTemplateEngine;

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

//    @Bean
//    public SpringTemplateEngine springTemplateEngine() {
//        SpringTemplateEngine engine = new SpringTemplateEngine();
//        engine.addDialect(new LayoutDialect());
//        return engine;
//    }
    
}