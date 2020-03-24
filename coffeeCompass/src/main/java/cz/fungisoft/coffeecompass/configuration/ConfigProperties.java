package cz.fungisoft.coffeecompass.configuration;

import javax.validation.constraints.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
@Data
public class ConfigProperties
{   
    @Email
    @Value("${contactme.mail.to}")
    private String contactMeEmailTo = "sadlokan@email.cz";
    
    /**
     * Base path part of the URL for requesting images for CoffeeSites
     * The othe parts of complete URL is the CoffeeSite id and the protocol and server at the begining
     * so, the complete example could be http://coffeecompass.cz/rest/image/bytes/171
     */
    @Value("${site.image.baseurlpath.rest}")
    private String baseURLPathforImages = "/rest/image/bytes/";
    
    /**
     * number of days back from now for the statistics overview of newest CoffeeSites
     */
    @Value("${site.statistics.newest.days.back}")
    private int daysBackForNewestSites = 60; 
    
    
    /**
     * Attributes to store configuration parameters of the image File.
    * <p>
    * Can be used in case uploaded image files are saved to file system, not to DB.
    * and for validation/limitation of the image file size.
    **/ 
    /**
     * Directory to save images to
     */
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${image.upload.max-file-size}")
    private Long maxUploadFileByteSize;
    
    /**
     * Should be a role automaticaly added to user's profile (list of roles), when user confirms it's email address?
     */
    @Value("${user.addrole.whenemailaddress.confirmed}")
    private boolean addRoleWhenUsersEmailIsConfirmed = false;
    
    /**
     * A role to be automaticaly added to user's profile (list of roles), when user confirms it's email address?
     */
    @Value("${user.addrole.whenemailaddress.confirmed.role}")
    private String roleToAddWhenUsersEmailIsConfirmed = "DBA";
}
