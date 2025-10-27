/**
 * 
 */
package cz.fungisoft.test.image2.controler;

import cz.fungisoft.test.image2.dto.ImageObjectDto;
import cz.fungisoft.test.image2.service.ImageFileStorageService;
import cz.fungisoft.test.image2.service.ImageObjectStorageService;
import cz.fungisoft.test.image2.serviceimpl.ImageSizes;
import jakarta.websocket.server.PathParam;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller to handle operations concerning obtaining CoffeeSite's image file.<br>
 * Also provides information about sizes of images.<br>
 * REST version
 *  
 * @author Michal Vaclavek
 */
@RestController
@RequestMapping("${site.image.baseurlpath.rest}")
public class GetController {

    private static final ImageObjectDto EMPTY_IMAGE_OBJECT = new ImageObjectDto();

    private final ImageFileStorageService imageFileStorageService;

    private final ImageObjectStorageService imageObjectStorageService;

    public GetController(ImageFileStorageService storageService,
                         ImageObjectStorageService imageObjectStorageService) {
        this.imageFileStorageService = storageService;
        this.imageObjectStorageService = imageObjectStorageService;
    }

    /**
     * Returns ImageObject
     *
     * @return
     */
    @GetMapping("/object/{objectExtId}") // napr.
    public ResponseEntity<ImageObjectDto> getImageObjectByExtId(@PathVariable String objectExtId) {
        return imageObjectStorageService.getImageObjectByExtIdToTransfer(objectExtId)
                                        .map(ResponseEntity::ok)
                                        .orElse(ResponseEntity.ok(EMPTY_IMAGE_OBJECT));
    }

    /**
     * Returns image as in byte64 coding.
     * Size means which a size of image i.e. "original", "hd", "large", "mid", "small"
     *
     * @return
     */
    @GetMapping("/base64/") // napr.
    public ResponseEntity<String> getImageAsBase64ByExtId(@RequestParam String imageExtId, @RequestParam(defaultValue = "large") String size) {
        Optional<String> picString = imageFileStorageService.getImageAsBase64(imageExtId, ImageSizes.get(size).orElse(ImageSizes.MID));
        return picString.map(ResponseEntity::ok)
                        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Returns image as byte array
     * Variant mean which size of image i.e. "original", "hd", "large", "mid", "small"
     *
     * @return
     */
    @GetMapping("/bytes/") // napr.
    public ResponseEntity<byte[]> getImageAsBytesByExtId(@RequestParam String imageExtId, @RequestParam(defaultValue = "large") String size) {

        byte[] pic = imageFileStorageService.getImageAsBytes(imageExtId, ImageSizes.get(size).orElse(ImageSizes.MID));

        HttpHeaders headers = new HttpHeaders();

        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        headers.setContentType(MediaType.IMAGE_PNG);
        if (pic != null) {
            headers.setContentLength(pic.length);
        }

        if (pic == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(pic, headers, HttpStatus.OK);
    }

    /**
     * Returns image as in base64 coding.
     * Variant mean which size of image i.e. "original", "hd", "large", "mid", "small"
     *
     * @return
     */
    @GetMapping("/base64/object/") // napr.
    public ResponseEntity<String> getFirstImageOfTypeAsBase64ByExtId(@RequestParam String objectExtId, @RequestParam(defaultValue = "main") String type, @RequestParam(defaultValue = "large") String size) {
        Optional<String> picString = imageFileStorageService.getFirstImageOfTypeAsBase64(objectExtId, type, ImageSizes.get(size).orElse(ImageSizes.MID));
        return picString.map(ResponseEntity::ok)
                        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Returns image as byte array
     * Variant mean which size of image i.e. "original", "hd", "large", "mid", "small"
     *
     * @return
     */
    @GetMapping("/bytes/object/") // napr.
    public ResponseEntity<byte[]> getFirstImageOfTypeAsBytesByExtId(@RequestParam String objectExtId, @RequestParam(defaultValue = "main")  String type, @RequestParam(defaultValue = "large") String size) {

        byte[] pic = imageFileStorageService.getFirstImageOfTypeAsBytes(objectExtId, type, ImageSizes.get(size).orElse(ImageSizes.MID));

        HttpHeaders headers = new HttpHeaders();

        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        headers.setContentType(MediaType.IMAGE_PNG);
        if (pic != null) {
            headers.setContentLength(pic.length);
        }

        if (pic == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(pic, headers, HttpStatus.OK);
    }

    /**
     * Returns size of all images for given ImageObjectExtId in kB
     */
    @GetMapping("/object/sizeKB/{objectExtId}") // napr.
    public ResponseEntity<Long> getSizeOfAllImagesForObjectToDownload(@PathVariable String objectExtId) {
        Long sizeKB = imageFileStorageService.getKBytesOfAllImagesForObjectToDownload(objectExtId);
        return ResponseEntity.ok(sizeKB);
    }

    /**
     * Returns number of all images for given ImageObjectExtId
     */
    @GetMapping("/object/number/{objectExtId}") // napr.
    public ResponseEntity<Long> getNumberOfAllImagesForObjectToDownload(@PathVariable String objectExtId) {
        Long numberOfImages = imageFileStorageService.getNumberOfAllImagesForObjectToDownload(objectExtId);
        return ResponseEntity.ok(numberOfImages);
    }

    /**
     * Returns size of all images for all ImageObjects in kB for given size or for HD sizes if size param is not given
     */
    @GetMapping("/all/sizeKB") // napr.
    public ResponseEntity<Long> getSizeOfAllImagesToDownload(@RequestParam(required = false) String imageSize) {
        Long sizeKB = imageFileStorageService.getKBytesOfAllImagesToDownload(
                (imageSize != null) ? ImageSizes.get(imageSize).orElse(null) : null);
        return ResponseEntity.ok(sizeKB);
    }

    /**
     * Returns number of all images for all ImageObjects id given size or HD sizes if size param is not given
     */
    @GetMapping("/all/number") // napr.
    public ResponseEntity<Long> getNumberOfAllImagesToDownload(@RequestParam(required = false) String imageSize) {
        Long numberOfImages = imageFileStorageService.getNumberOfAllImagesToDownload(
                (imageSize != null) ? ImageSizes.get(imageSize).orElse(null) : null);
        return ResponseEntity.ok(numberOfImages);
    }
}
