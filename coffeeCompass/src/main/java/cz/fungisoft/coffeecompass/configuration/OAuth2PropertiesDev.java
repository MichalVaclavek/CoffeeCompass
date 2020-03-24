package cz.fungisoft.coffeecompass.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Obsahuje konfiguracni parametry pro OAuth2 autentikaci, ktere se nacitaji z application-dev.properties souboru<br>
 * a tedy se aktivuji pro aktivnim Profile=dev
 * <p>
 * Tato (a trida OAuth2PropertiesProd) jsou vytvoreny pouze za ucelem automatickeho naceteni konfiguracnich hodnot<br>
 * aplikace z prislusneho souboru, tedy z application-prod.properties resp. application-dev.properties.<br>
 * Nactou se tedy hodnoty atributu, ktere deklaruje trida {@code OAuth2Properties}, ale ze spravneho souboru, podle aktualniho Profile.<br>
 *   
 * @author Michal Vaclavek
 */
@Configuration
@Profile("dev")
@PropertySource("classpath:application-dev.properties")
public class OAuth2PropertiesDev extends JwtAndOAuth2Properties
{}
