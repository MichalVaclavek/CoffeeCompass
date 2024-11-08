/**
 * 
 */
package cz.fungisoft.coffeecompass.controller.rest;

import cz.fungisoft.coffeecompass.serviceimpl.images.ImagesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.service.image.ImageStorageService;

/**
 * Controller to handle operations concerning obtaining CoffeeSite's image file.<br>
 * REST version
 *  
 * @author Michal Vaclavek
 *
 */
@Tag(name = "Images", description = "Images of the coffee sites")
@RestController
@RequiredArgsConstructor
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/image")
public class ImageControllerPublicREST  {

    private final ImageStorageService imageStorageService;

    private final ImagesService imagesService;


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
}
