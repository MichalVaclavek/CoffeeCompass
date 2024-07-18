package cz.fungisoft.test.image2.validators;

import cz.fungisoft.test.image2.configuration.ConfigProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;


/**
 * Validator to check if the file requested to upload:
 *  - is jpg, jpeg or png
 *  - has size lower then limit in FileStorageProperties resp. in configprops.properties or any definied config file.
 * 
 * @author Michal Vaclavek
 *
 */
public class ImageFileValidator implements ConstraintValidator<ImageFileValidatorConstraint, MultipartFile> {

    private Long maxFileSize = 5_242_880L; // 5 MB
    
    private ConfigProperties properties;
    
    /**
     * Default construktor needed for Hibernate? othervise Exception is thrown
     */
    public ImageFileValidator() {};
    
    /**
     * FileStorageProperties is injected by Spring as this class implements ConstraintValidator interface
     * 
     * @param properties
     */
    public ImageFileValidator(ConfigProperties properties) {
        super();
        this.properties = properties;
        maxFileSize = this.properties.getMaxUploadFileByteSize();
    }

    @Override
    public void initialize(ImageFileValidatorConstraint file) {
    }
 
    @Override
    public boolean isValid(MultipartFile fileToUpload, ConstraintValidatorContext cxt) {
        
        boolean result = true;

        if (fileToUpload != null) {
            if (!fileToUpload.isEmpty()) {
                String contentType = fileToUpload.getContentType();
                assert contentType != null;
                if (!isSupportedContentType(contentType)) {
                    cxt.disableDefaultConstraintViolation();
                    cxt.buildConstraintViolationWithTemplate("{ImageFileValidatorConstraint.ImageData.file.type}") // retrieve message from Validation Messages source defined in CoffeeCompassConfiguration
                       .addConstraintViolation();
        
                    result = false;
                }
                
                if (!isAcceptableSize(fileToUpload)) {
                    cxt.disableDefaultConstraintViolation();
                    cxt.buildConstraintViolationWithTemplate("{ImageFileValidatorConstraint.ImageData.file.size}") // retrieve message from Validation Messages source defined in CoffeeCompassConfiguration
                       .addConstraintViolation();
        
                    result = false;
                }
            } else {
                cxt.disableDefaultConstraintViolation();
                cxt.buildConstraintViolationWithTemplate("{ImageFileValidatorConstraint.ImageData.file.empty}")
                   .addConstraintViolation();
    
                result = false;
            }
        }

        return result;
    }
    
    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg");
    }
    
    private boolean isAcceptableSize(MultipartFile file) {
        return file.getSize() <= maxFileSize;
    }
}
