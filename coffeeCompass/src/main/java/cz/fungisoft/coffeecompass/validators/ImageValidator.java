package cz.fungisoft.coffeecompass.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import cz.fungisoft.coffeecompass.entity.Image;

public class ImageValidator implements Validator
{

    @Override
    public boolean supports(Class<?> clazz) {
        return Image.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Image image = (Image)target;

        // Validate file only if the Gif's id is null (with a null id, it must be a new Gif),
        // so that existing Gif can be updated without uploading new file
        if(image.getId() == null && (image.getFile() == null || image.getFile().isEmpty())) {
            errors.rejectValue("file","file.required","Please choose a file to upload");
        }
    }

}
