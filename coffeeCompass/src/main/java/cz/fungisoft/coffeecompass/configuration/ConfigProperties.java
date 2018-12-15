package cz.fungisoft.coffeecompass.configuration;

import javax.validation.constraints.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

/**
 * Obsahuje dalsi specificke konfiguracni parametry aplikace, ktere lze nacist z src/main/resources/configprops.properties souboru
 * 
 * @author Michal Vaclavek
 */
@Configuration
//@ConfigurationProperties
@PropertySource("classpath:configprops.properties")
@Data
public class ConfigProperties
{   
    @Email
    @Value("${contactme.mail.to}")
    private String contactMeEmailTo;
    
}
