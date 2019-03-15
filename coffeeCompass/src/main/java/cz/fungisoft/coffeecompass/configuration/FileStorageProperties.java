package cz.fungisoft.coffeecompass.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

/**
 * Class to store configuration parameters of the image File<br>
 * <br>
 * Can be used in case uploaded image files are saved to file system, not to DB.
 * and for validation/limitation of the image file size.
 * 
 * @author Michal Vaclavek
 *
 */
@Configuration
@PropertySource("classpath:configprops-${spring.profiles.active}.properties")
@Data
public class FileStorageProperties
{
    /**
     * Directory to save images to
     */
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${image.upload.max-file-size}")
    private Long maxUploadFileByteSize;
}
