package cz.fungisoft.coffeecompass.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

/**
 * Can be used in case uploaded image files are saved to file system, not to DB.
 * But used also for validation/limitation of the image file size.
 * 
 * @author Michal Vaclavek
 *
 */
@Configuration
@PropertySource("classpath:configprops.properties")
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
