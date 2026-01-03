/**
 * 
 */
package cz.fungisoft.coffeecompass.controller.rest;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.serviceimpl.images.ImagesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cz.fungisoft.coffeecompass.service.image.ImageStorageService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Controller to handle operations concerning obtaining CoffeeSite's image file.<br>
 * REST version
 *  
 * @author Michal Vaclavek
 */
@Tag(name = "Images", description = "Images of the coffee sites")
@RestController
@RequiredArgsConstructor
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/image")
public class ImageControllerPublicREST  {

    private final ImageStorageService imageStorageService;

    private final ImagesService imagesService;

    private final CoffeeSiteService coffeeSiteService;

    /**
     * Metoda pro ziskani URL vsech obrazku daneho situ
     *
     * @return
     */
    @GetMapping("/allImageUrls/{externalId}")
    public ResponseEntity<Set<String>> allImageUrls(@PathVariable String externalId) {
        Optional<CoffeeSite> cs = coffeeSiteService.findOneByExternalId(externalId);
        HashSet<String> imageUrls = new HashSet<>();

        cs.ifPresent(coffeeSite -> {
            // Add all images for this CoffeeSite
            imageUrls.addAll(imagesService.getSmallImagesUrls(externalId));
            // add also image saved in imageStorageService
            imageUrls.add(coffeeSiteService.getLocalCoffeeSiteImageUrl(coffeeSite));
        });
        return ResponseEntity.ok(imageUrls);
    }

    /**
     * Returns image of the CoffeeSite of id=siteId
     * 
     * @param siteId
     * @return
     */
    @GetMapping("/base64/{siteId}") // napr. http://coffeecompass.cz/rest/image/base64/2
    public ResponseEntity<String> getImageAsBase64BySiteId(@PathVariable String siteId) {
        
        String picString = imageStorageService.getImageAsBase64ForSiteId(siteId);
        
        if (picString == null || picString.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(picString);
    }
    
    /**
     * Returns image as byte array of the CoffeeSite of id=siteId
     * 
     * @param siteExternalId
     * @return
     */
    @GetMapping("/bytes/{siteExternalId}") // napr. http://coffeecompass.cz/rest/image/bytes/26
    public ResponseEntity<byte[]> getImageAsBytesBySiteId(@PathVariable String siteExternalId) {
        
        byte[] pic = imageStorageService.getImageAsBytesForSiteExternalId(siteExternalId).orElse(new byte[0]);
        
        HttpHeaders headers = new HttpHeaders();

        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(pic.length);

        return new ResponseEntity<>(pic, headers, HttpStatus.OK);
    }

    /**
     * Methods to return size and numbers of images of CoffeeSites objects
     */
    @GetMapping("/object/sizeKB/{siteExtId}")
    public ResponseEntity<Long> getImageObjectSizeOfImages(@PathVariable String siteExtId) {
        Long sizeKB = imagesService.getImageObjectSizeOfImages(siteExtId);
        return ResponseEntity.ok(sizeKB);
    }

    @GetMapping("/object/number/{siteExtId}")
    public ResponseEntity<Long> getImageObjectNumberOfImages(@PathVariable String siteExtId) {
        Long numberOfImages = imagesService.getImageObjectNumberOfImages(siteExtId);
        return ResponseEntity.ok(numberOfImages);
    }

    @GetMapping("/all/sizeKB")
    public ResponseEntity<Long> getSizeOfAllImagesToDownload(@RequestParam(required = false) String imageSize) {
        Long sizeKB = imagesService.getSizeOfAllImagesToDownload(imageSize);
        return ResponseEntity.ok(sizeKB);
    }

    @GetMapping("/all/number")
    public ResponseEntity<Long> getNumberOfAllImagesToDownload(@RequestParam(required = false) String imageSize) {
        Long numberOfImages = imagesService.getNumberOfAllImagesToDownload(imageSize);
        return ResponseEntity.ok(numberOfImages);
    }
}
