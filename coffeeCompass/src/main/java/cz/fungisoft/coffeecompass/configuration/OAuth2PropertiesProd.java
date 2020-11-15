package cz.fungisoft.coffeecompass.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Obsahuje konfiguracni parametry pro OAuth2 autentikaci, ktere se nacitaji z application-prod.properties souboru<br>
 * a tedy se aktivuji pro aktivnim Profile=prod
 * <p>
 * Tato (a trida OAuth2PropertiesDev) jsou vytvoreny pouze za ucelem automatickeho nacteni konfiguracnich hodnot<br>
 * aplikace z prislusneho souboru, tedy z application-prod.properties resp. application-dev.properties.<br>
 * Nactou se tedy hodnoty atributu, ktere deklaruje trida {@code OAuth2Properties}, ale ze spravneho souboru, podle aktualniho Profile.<br>
 *   
 * @author Michal Vaclavek
 */
@Configuration
@Profile("prod")
@PropertySource("classpath:application-prod.properties")
public class OAuth2PropertiesProd extends JwtAndOAuth2Properties
{}
