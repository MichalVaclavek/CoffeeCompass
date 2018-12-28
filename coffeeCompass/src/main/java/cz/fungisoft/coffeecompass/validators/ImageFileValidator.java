package cz.fungisoft.coffeecompass.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.multipart.MultipartFile;

public class ImageFileValidator implements ConstraintValidator<ImageFileValidatorConstraint, MultipartFile> {
 
    @Override
    public void initialize(ImageFileValidatorConstraint fileName) {
    }
 
    @Override
    public boolean isValid(MultipartFile fileToUpload, ConstraintValidatorContext cxt) {
        
        boolean result = true;

        if (!fileToUpload.isEmpty()) {
        
            String contentType = fileToUpload.getContentType();
            if (!isSupportedContentType(contentType)) {
                cxt.disableDefaultConstraintViolation();
                cxt.buildConstraintViolationWithTemplate("{ImageFileValidatorConstraint.Image.file}") // retrieve message from Validation Messages source defined in CoffeeCompassConfiguration
                       .addConstraintViolation();
    
                result = false;
            }
        } else
        {
            cxt.disableDefaultConstraintViolation();
            cxt.buildConstraintViolationWithTemplate("{error.image.empty}")
                   .addConstraintViolation();

            result = false;
        }

        return result;
    }
    
    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg");
    }
 
}
