package cz.fungisoft.test.image2.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * Obsahuje dalsi specificke konfiguracni parametry aplikace, ktere lze nacist z src/main/resources/configprops.properties souboru
 * Pokud trida obsahovala anotaci @PropertySource("classpath:configprops.properties")
 * tak bohuzel hodnoty v tomto souboru (configprops.properties), "prebily" hodnoty napr. v souboru configprops-dev.properties,
 * ktere odkazuje trida ConfigPropertiesDev ve svem @PropertySource v pripade, ze byl aktivni Profile=dev (ktery anotuje prave
 * i tridu ConfigPropertiesDev )
 * 
 * @author Michal Vaclavek
 */
@Profile("default")
@Configuration
@Data
public class ConfigProperties {

    @Value("${images.host}")
    private String imagesHost;

    /**
     * Base path part of the URL for requesting images for CoffeeSites
     * The othe parts of complete URL is the CoffeeSite id and the protocol and server at the begining
     * so, the complete example could be http://coffeecompass.cz/rest/image/bytes/171
     */
    @Value("${site.image.baseurlpath.rest}")
    private String baseURLPathforImages = "/rest/image/bytes/";
    

    /**
     * Attributes to store configuration parameters of the image File.
     * <p>
     * Can be used in case uploaded image files are saved to file system, not to DB.
     * and for validation/limitation of the image file size.
     * 
     * Directory to save uploaded images to.
     */
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${image.upload.max-file-size}")
    private Long maxUploadFileByteSize;
}
