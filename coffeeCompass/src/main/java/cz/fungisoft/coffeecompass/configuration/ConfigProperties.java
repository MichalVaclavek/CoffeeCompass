package cz.fungisoft.coffeecompass.configuration;

import javax.validation.constraints.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

/**
 * Obsahuje dalsi specificke konfiguracni parametry aplikace, ktere lze nacist z src/main/resources/configprops.properties souboru
 * Pokud trida obsahovala anotaci @PropertySource("classpath:configprops.properties")
 * tak bohuzel hodnoty v tomto souboru (configprops.properties), "prebily" hodnoty napr. v souboru configprops-dev.properties
 * , ktere odkazuje trida ConfigPropertiesDev ve svem @PropertySource v pripade, ze byl aktivni Profile=dev (ktery anotuje prave 
 * i tridu ConfigPropertiesDev )
 * 
 * @author Michal Vaclavek
 */
@Profile("default")
@Configuration
//@PropertySource("classpath:configprops.properties")
@Data
public class ConfigProperties
{   
    @Email
    @Value("${contactme.mail.to}")
    private String contactMeEmailTo = "sadlokan@email.cz";
    
    @Value("${site.image.baseurl.rest}")
    private String baseURLforImages = "http://coffeecompass.cz/rest/image/bytes/";
    
    /**
     * number of days back from now for the statistics overview of newest CoffeeSites
     */
    @Value("${site.statistics.newest.days.back}")
    private int daysBackForNewestSites = 60; 
}
