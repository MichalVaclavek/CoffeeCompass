package cz.fungisoft.coffeecompass.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Obsahuje dalsi specificke konfiguracni parametry aplikace, ktere lze nacist z src/main/resources/configprops-prod_docker.properties souboru
 * <p>
 * Tato a trida ConfigPropertiesDev jsou vytvoreny pouze za ucelem automatickeho naceteni konfiguracnich hodnot
 * aplikace z prislusneho souboru, tedy z configprops-prod_docker.properties resp. configprops-dev.properties.
 * Nactou se tedy hodnoty atributu, ktere deklaruje trida {@code ConfigProperties}, ze spravneho souboru, podle aktualniho Profile.
 *  
 * @author Michal Vaclavek
 */
@Configuration
@Profile("prod_docker")
@PropertySource({"classpath:configprops-prod_docker.properties"})
public class ConfigPropertiesProd extends ConfigProperties
{}
