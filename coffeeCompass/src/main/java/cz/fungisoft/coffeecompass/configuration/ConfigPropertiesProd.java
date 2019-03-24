package cz.fungisoft.coffeecompass.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Obsahuje dalsi specificke konfiguracni parametry aplikace, ktere lze nacist z src/main/resources/configprops-prod.properties souboru
 * <br><br>
 * Tato a trida ConfigPropertiesDev jsou vytvoreny pouze za ucelem automatickeho naceteni konfiguracnich hodnot
 * aplikace z prislusneho souboru, tedy z configprops-prod.properties resp. configprops-dev.properties.
 * Nactou se tedy hodnoty atributu, ktere deklaruje trida {@code ConfigProperties}, ze spravneho souboru, podle aktualniho Profile.
 *  
 * @author Michal Vaclavek
 */
@Configuration
@Profile("prod")
@PropertySource("classpath:configprops-${spring.profiles.active}.properties")
public class ConfigPropertiesProd extends ConfigProperties
{}
