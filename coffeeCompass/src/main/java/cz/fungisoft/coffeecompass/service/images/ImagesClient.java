package cz.fungisoft.coffeecompass.service.images;

import cz.fungisoft.coffeecompass.configuration.CustomFeignConfiguration;
import cz.fungisoft.images.api.ImageObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;


@FeignClient(name ="images",
             configuration = CustomFeignConfiguration.class,
             url = "http://localhost:12002/api/v1/images")
public interface ImagesClient {

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadImageFile(@RequestPart("file") MultipartFile file,
                                @RequestPart("objectExtId") String objectExtId,
                                @RequestPart("description") String description,
                                @RequestPart("type") String type);

    @PostMapping(value = "/object/{objectExtId}/replace/{imageExtId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ImageObject replaceImageFile(@PathVariable("objectExtId") String objectExtId,
                                 @PathVariable("imageObjectExtId") String imageObjectExtId,
                                 @RequestPart("file") MultipartFile file,
                                 @RequestParam("description") String description,
                                 @RequestParam("type") String type);

    @GetMapping(value = "/object/{imageObjectExtId}", consumes = "application/json")
    Optional<ImageObject> getImageObject(@PathVariable("imageObjectExtId") String imageObjectExtId);

    @GetMapping(value = "/bytes/object/", consumes = "image/png")
    Object getObjectBasicImageBytes(@RequestParam("objectExtId") String objectExtId, @RequestParam("type") String type, @RequestParam("variant") String variant);

    @GetMapping(value = "/bytes/", consumes = "image/png")
    Object getImageBytes(@RequestParam("imageExtId") String imageExtId, @RequestParam("variant") String variant);

    @PutMapping(value = "/rotate/{imageExtId}/direction/{rotationDirection}", consumes = "image/png")
    void rotateImage(@PathVariable("imageExtId") String imageExtId,
                     @PathVariable("rotationDirection") String rotationDirection);

    @DeleteMapping(value = "/object/{objectExtId}/image/{imageExtId}", consumes = "application/json")
    void deleteImage(@PathVariable("objectExtId") String objectExtId,
                     @PathVariable("imageExtId") String imageExtId);

    @DeleteMapping(value = "/object/{objectExtId}/all", consumes = "application/json")
    void deleteAllImages(@PathVariable("objectExtId") String objectExtId);
}
