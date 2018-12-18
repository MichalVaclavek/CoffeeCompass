package cz.fungisoft.coffeecompass.validators;

import java.io.File;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements  ConstraintValidator<FileValidatorConstraint , MultipartFile> {
 
    @Override
    public void initialize(FileValidatorConstraint fileName) {
    }
 
    @Override
    public boolean isValid(MultipartFile fileToUpload, ConstraintValidatorContext cxt) {
        
        File file = (File) fileToUpload;
        return file.exists() && file.isFile();
    }
 
}
