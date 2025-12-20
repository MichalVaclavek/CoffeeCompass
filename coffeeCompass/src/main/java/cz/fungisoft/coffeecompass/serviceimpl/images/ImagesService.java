package cz.fungisoft.coffeecompass.serviceimpl.images;

import cz.fungisoft.coffeecompass.service.images.ImagesClient;
import cz.fungisoft.images.api.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
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
                .sorted(Comparator.comparing(ImageFile::getSavedOn).reversed())
                .map(ImageFile::getBaseBytesImageUrl)
                .map(this::convertImageUrl)
                .<String>mapMulti(Optional::ifPresent)
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
                .max(Comparator.comparing(ImageFile::getSavedOn));
    }

    public Optional<ImageFile> getLatestImage(String imageObjectExtId) {
        return getImageFiles(imageObjectExtId).max(Comparator.comparing(ImageFile::getSavedOn));
    }

    /**
     * Url of the image  fetched from images service must be converted so the host is not the host of images server,
     * but host of this service.
     * So, if the main image urls is "https://images:12002/api/v1/images/bytes/object/?objectExtId=c8a66433-62b1-41b5-b406-c214a6af4652"
     * the resulting url will be: "https://currenthost:currentport/api/v1/images/bytes/object/?objectExtId=c8a66433-62b1-41b5-b406-c214a6af4652"
     *
     * @param imageObjectExtId
     * @return
     */
    public Optional<String> getBasicObjectImageUrl(String imageObjectExtId) {
        Optional<ImageObject> imageObject = imagesApi.getImageObject(imageObjectExtId);
        Optional<String> imageUrl = imageObject.filter(io -> io.getBaseBytesObjectUrl() != null)
                .map(ImageObject::getBaseBytesObjectUrl);
        return imageUrl.flatMap(this::convertImageUrl);
    }

    public Optional<String> convertImageUrl(String imageUrl) {
        return Optional.ofNullable(imageUrl).map(url -> {
            // Parse the "foreign" URL to extract just the path and query
            // (the essence of the resource location, independent of the server)
            UriComponents originalUri = UriComponentsBuilder.fromHttpUrl(url).build();

            // Build a new URL using the current request's context (host, port, scheme)
            // and append the original path and query.
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(originalUri.getPath())
                    .query(originalUri.getQuery())
                    .build()
                    .toUriString();
        });
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

    // Size and number of images methods

    public Long getImageObjectSizeOfImages(String objectExtId) {
        return imagesApi.getImageObjectSizeOfImages(objectExtId);
    }

    public Long getImageObjectNumberOfImages(String objectExtId) {
        return imagesApi.getImageObjectNumberOfImages(objectExtId);
    }

    public Long getSizeOfAllImagesToDownload(String imageSize) {
        return imagesApi.getSizeOfAllImagesToDownload(imageSize);
    }

    public Long getNumberOfAllImagesToDownload(String imageSize) {
        return imagesApi.getNumberOfAllImagesToDownload(imageSize);
    }
}

