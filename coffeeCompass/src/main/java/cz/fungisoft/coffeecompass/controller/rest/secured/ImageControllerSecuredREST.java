package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.Locale;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cz.fungisoft.coffeecompass.exceptions.rest.BadRESTRequestException;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;

/**
* Controller to handle operations upload or delete CoffeeSite's image file.<br>
* REST version
*  
* @author Michal Vaclavek
*
*/
@Tag(name = "CoffeeSiteImage", description = "Coffee site image REST operations")
@RestController 
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/secured/image")
public class
ImageControllerSecuredREST {

    private static final Logger log = LoggerFactory.getLogger(ImageControllerSecuredREST.class);
    
    private final ImageStorageService imageStorageService;

    private final CoffeeSiteService coffeeSiteService;
    
    private final MessageSource messages;

    @Autowired
    public ImageControllerSecuredREST(ImageStorageService storageService,
                                      CoffeeSiteService coffeeSiteService,
                                      MessageSource messages) {
        this.imageStorageService = storageService;
        this.coffeeSiteService = coffeeSiteService;
        this.messages = messages;
    }

    /**
     * Serves upload image request for CoffeeSite. Coffee site is identified by it's ID included
     * in the Image object to be uploaded/saved.<br>
     *
     * @param file uploaded Image from View. Contains file to be uploaded
     * @param coffeeSiteId  ID of the coffeeSite the image belongs to
     * @param locale
     * 
     * @return load URL of the new image - used to assign to the edited CoffeeSite as a new CoffeeSite's image URL 
     */
    @PostMapping("/upload") // POST https://coffeecompass.cz/rest/secured/image/upload?coffeeSiteId=uuid a správné Body
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("coffeeSiteId") String coffeeSiteId,
                                                   Locale locale) {
       if (file == null) {
           throw new BadRESTRequestException(messages.getMessage("coffeesite.image.upload.rest.error", null, locale));
       }
       
       UUID imageId = imageStorageService.storeImageFile(file, UUID.fromString(coffeeSiteId), false);
       if (imageId == null) {
           return new ResponseEntity<>( HttpStatus.NOT_FOUND);
       }
       log.info("Image uploaded. CoffeeSite id: {}. Image id: {}", coffeeSiteId, imageId);
        return coffeeSiteService.findOneToTransfer(coffeeSiteId)
                .map(cs ->  new ResponseEntity<>(cs.getMainImageURL(), HttpStatus.OK))
                                                                 .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani obrazku/Image k jednomu CoffeeSitu.<br>
     * 
     * @param imageExtId id of the Image to delete
     * @return coffeeSiteId the image belonged to before deleting
     */
    @DeleteMapping("/delete/{imageExtId}")
    public ResponseEntity<String> deleteImageByImageId(@PathVariable String imageExtId) {
        
        String siteId = imageStorageService.deleteSiteImageById(UUID.fromString(imageExtId));
        
        if (siteId == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Image deleted. Image id: {}", imageExtId);
        return new ResponseEntity<>(siteId, HttpStatus.OK);
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani obrazku/Image k jednomu CoffeeSitu.<br>
     * 
     * @param coffeeSiteId id of the CoffeeSites who's Image is to be deleted
     * @return coffeeSiteId the image belonged to before deleting. Can be used to compare if requested coffeeSiteId
     * is same as returned one
     */
    @DeleteMapping("/delete/site/{coffeeSiteId}") 
    public ResponseEntity<String> deleteImageBySiteId(@PathVariable String coffeeSiteId) {
        
        String siteId = imageStorageService.deleteSiteImageBySiteId(coffeeSiteId);
        
        if (siteId == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Image deleted. CoffeeSite id: {}.", coffeeSiteId);
        return new ResponseEntity<>(siteId, HttpStatus.OK);
    }
}
