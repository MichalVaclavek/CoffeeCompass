package cz.fungisoft.coffeecompass.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Configuration
//@ConfigurationProperties
@PropertySource("classpath:configprops.properties")
@Data
public class FileStorageProperties
{
    /**
     * Directory to save images to
     */
    @Value("${file.upload-dir}")
    private String uploadDir;
}
