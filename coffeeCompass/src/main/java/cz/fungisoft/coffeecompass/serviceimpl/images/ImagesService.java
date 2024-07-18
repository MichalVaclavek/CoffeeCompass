package cz.fungisoft.coffeecompass.serviceimpl.images;

import cz.fungisoft.coffeecompass.service.images.ImagesClient;
import cz.fungisoft.images.api.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ImagesService implements ImagesServiceInterface {

    private final ImagesClient imagesApi;

    public String uploadImageFile(MultipartFile file, String objectExtId, String description, String imageType) {
        return imagesApi.uploadImageFile(file, objectExtId, description, imageType);
    }

    public Optional<ImageObject> getImageObject(String objectExtId) {
        return imagesApi.getImageObject(objectExtId);
    }

    public List<String> getSmallImagesUrls(String imageObjectExtId) {
        Optional<ImageObject> imageObject = getImageObject(imageObjectExtId);
        return imageObject.stream()
                .filter(io -> Objects.nonNull(io.getObjectImages()))
                .flatMap(io -> io.getObjectImages().stream())
                .map(ImageFile::getBaseBytesImageUrl)
                .map(url -> url + "&variant=small")
                .toList();
    }

    public Stream<ImageFile> getImageFiles(String imageObjectExtId) {
        Optional<ImageObject> imageObject = getImageObject(imageObjectExtId);
        return imageObject.stream()
                .filter(io -> Objects.nonNull(io.getObjectImages()))
                .flatMap(io -> io.getObjectImages().stream());
    }

    public Optional<ImageFile> getDefaultSelectedImage(String imageObjectExtId) {
        return getImageFiles(imageObjectExtId)
                .filter(img -> img.getImageType().equalsIgnoreCase("main"))
                .findFirst();
    }

    public Optional<String> getBasicObjectImageUrl(String imageObjectExtId) {
        Optional<ImageObject> imageObject = imagesApi.getImageObject(imageObjectExtId);
        return imageObject.filter(io -> io.getBaseBytesObjectUrl() != null)
                          .map(ImageObject::getBaseBytesObjectUrl);
    }

    public byte[] getBasicObjectImage(String imageObjectExtId) {
        return (byte[]) imagesApi.getObjectBasicImageBytes(imageObjectExtId, "main", "mid");
    }

    public byte[] getBasicImageFile(String imageFileExtId, String variant) {
        return (byte[]) imagesApi.getImageBytes(imageFileExtId, variant);
    }

    public void rotateImageLeft(String imageObjectExtId) {
        imagesApi.rotateImage(imageObjectExtId, "left");
    }

    public void rotateImageRight(String imageObjectExtId) {
        imagesApi.rotateImage(imageObjectExtId, "right");
    }

    public void deleteImage(String imageObjectExtIdt, String imageFileExtId) {
        imagesApi.deleteImage(imageObjectExtIdt, imageFileExtId);
    }

    public void deleteAllImages(String imageObjectExtIdt) {
        imagesApi.deleteAllImages(imageObjectExtIdt);
    }
}

