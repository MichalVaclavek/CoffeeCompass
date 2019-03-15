package cz.fungisoft.coffeecompass.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Obsahuje dalsi specificke konfiguracni parametry aplikace, ktere lze nacist z src/main/resources/configprops.properties souboru
 * 
 * @author Michal Vaclavek
 */
@Configuration
@Profile("prod")
@PropertySource("classpath:configprops-${spring.profiles.active}.properties")
public class ConfigPropertiesProd extends ConfigProperties
{}
