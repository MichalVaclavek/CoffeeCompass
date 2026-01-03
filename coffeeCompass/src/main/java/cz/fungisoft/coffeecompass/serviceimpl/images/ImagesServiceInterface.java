package cz.fungisoft.coffeecompass.serviceimpl.images;

import cz.fungisoft.images.api.ImageFile;
import cz.fungisoft.images.api.ImageObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface ImagesServiceInterface {

        String uploadImageFile(MultipartFile file, String objectExtId, String description, String imageType);

        Optional<ImageObject> getImageObject(String objectExtId);

        Set<String> getSmallImagesUrls(String imageObjectExtId);

        Stream<ImageFile> getImageFiles(String imageObjectExtId);

        Optional<ImageFile> getDefaultSelectedImage(String imageObjectExtId);

        Optional<String> getBasicObjectImageUrl(String imageObjectExtId);

        Optional<String> convertImageUrl(String imageUrl);

        byte[] getBasicObjectImage(String imageObjectExtId);

        byte[] getBasicImageFile(String imageFileExtId, String variant);

        void rotateImageLeft(String imageObjectExtId);

        void rotateImageRight(String imageObjectExtId);

        void deleteImage(String imageObjectExtIdt, String imageFileExtId);

        void deleteAllImages(String imageObjectExtIdt);
}
