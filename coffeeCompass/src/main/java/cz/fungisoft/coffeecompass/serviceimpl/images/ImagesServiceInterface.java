package cz.fungisoft.coffeecompass.serviceimpl.images;

import cz.fungisoft.images.api.ImageObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ImagesServiceInterface {

        String uploadImageFile(MultipartFile file, String objectExtId, String description, String imageType);

        Optional<ImageObject> getImageObject(String objectExtId);

        Optional<String> getBasicObjectImageUrl(String imageObjectExtId);

        byte[] getBasicObjectImage(String imageObjectExtId);

        byte[] getBasicImageFile(String imageFileExtId, String variant);

        void rotateImageLeft(String imageObjectExtId);

        void rotateImageRight(String imageObjectExtId);

        void deleteImage(String imageObjectExtIdt, String imageFileExtId);

        void deleteAllImages(String imageObjectExtIdt);
}
