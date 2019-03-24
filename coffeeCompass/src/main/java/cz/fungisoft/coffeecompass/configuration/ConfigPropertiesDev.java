package cz.fungisoft.coffeecompass.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Obsahuje dalsi specificke konfiguracni parametry aplikace, ktere lze nacist z src/main/resources/configprops-dev.properties souboru
 * <br><br>
 * Tato a trida ConfigPropertiesProd jsou vytvoreny pouze za ucelem automatickeho naceteni konfiguracnich hodnot
 * aplikace z prislusneho souboru, tedy z configprops-dev.properties resp. configprops-prod.properties.
 * Nactou se tedy hodnoty atributu, ktere deklaruje trida {@code ConfigProperties}, ze spravneho souboru, podle aktualniho Profile.
 *   
 * @author Michal Vaclavek
 */
@Configuration
@Profile("dev")
@PropertySource("classpath:configprops-${spring.profiles.active}.properties")
public class ConfigPropertiesDev extends ConfigProperties
{}
