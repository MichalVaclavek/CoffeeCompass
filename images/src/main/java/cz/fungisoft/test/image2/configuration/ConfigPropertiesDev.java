package cz.fungisoft.test.image2.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Obsahuje dalsi specificke konfiguracni parametry aplikace, ktere lze nacist z src/main/resources/configprops-dev.properties souboru
 * <p>
 * Tato a trida ConfigPropertiesProd jsou vytvoreny pouze za ucelem automatickeho nacteni konfiguracnich hodnot<br>
 * aplikace z prislusneho souboru, tedy z configprops-dev.properties resp. configprops-prod.properties.<br>
 * Nactou se tedy hodnoty atributu, ktere deklaruje trida {@code ConfigProperties}, ze spravneho souboru, podle aktualniho Profile.<br>
 *   
 * @author Michal Vaclavek
 */
@Configuration
@Profile("dev")
@PropertySource({"classpath:configprops-dev.properties"})
public class ConfigPropertiesDev extends ConfigProperties
{}
