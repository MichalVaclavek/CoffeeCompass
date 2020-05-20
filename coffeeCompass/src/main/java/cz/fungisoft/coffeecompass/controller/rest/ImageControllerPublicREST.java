/**
 * 
 */
package cz.fungisoft.coffeecompass.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.service.ImageStorageService;
import io.swagger.annotations.Api;

/**
 * Controller to handle operations concerning obtaining CoffeeSite's image file.<br>
 * REST version
 *  
 * @author Michal Vaclavek
 *
 */
@Api // Swagger
@RestController
@RequestMapping("/rest/image")
public class ImageControllerPublicREST
{
    private final ImageStorageService imageStorageService;

    @Autowired
    public ImageControllerPublicREST(ImageStorageService storageService) {
        this.imageStorageService = storageService;
    }

    
    /**
     * Returns image of the CoffeeSite of id=siteId
     * 
     * @param siteId
     * @return
     */
    @GetMapping("/base64/{siteId}") // napr. http://coffeecompass.cz/rest/image/base64/2
    public ResponseEntity<String> getImageAsBase64BySiteId(@PathVariable Long siteId) {
        
        String picString = imageStorageService.getImageAsBase64ForSiteId(siteId);
        
        if (picString == null || picString.isEmpty()) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(picString, HttpStatus.OK);
    }
    
    /**
     * Returns image as byte array of the CoffeeSite of id=siteId
     * 
     * @param siteId
     * @return
     */
    @GetMapping("/bytes/{siteId}") // napr. http://coffeecompass.cz/rest/image/bytes/26
    public ResponseEntity<byte[]> getImageAsBytesBySiteId(@PathVariable Long siteId) {
        
        byte[] pic = imageStorageService.getImageAsBytesForSiteId(siteId);
        
        HttpHeaders headers = new HttpHeaders();

        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        headers.setContentType(MediaType.IMAGE_JPEG);
        if (pic != null)
            headers.setContentLength(pic.length);
        
        if (pic == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<byte[]>(pic, headers, HttpStatus.OK);
    }

}
