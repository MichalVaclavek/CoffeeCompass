package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.exceptions.rest.BadRESTRequestException;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.ImageStorageService;
import io.swagger.annotations.Api;

/**
* Controller to handle operations upload or delete CoffeeSite's image file.<br>
* REST version
*  
* @author Michal Vaclavek
*
*/
@Api // Swagger
@RestController 
@RequestMapping("/rest/secured/image")
public class ImageControllerSecuredREST
{
    private static final Logger log = LoggerFactory.getLogger(ImageControllerSecuredREST.class);
    
    private final ImageStorageService imageStorageService;
    
    private CoffeeSiteService coffeeSiteService;
    
    private MessageSource messages;

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
     * 
     * @param image uploaded Image from View. Contains file to be uploaded and ID of the coffeeSite the image belongs to.
     * @param result for checking errors during form validation
     * @param redirectAttributes attributes to be passed to other Controller after redirection from this View/Controller.
     * @return load URL of the new image - used to assign to the edited CoffeeSite as a new CoffeeSite's image URL 
     */
    @PostMapping("/upload") // POST https://coffeecompass.cz/rest/secured/image/upload?coffeeSiteId=2 a správné Body
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("coffeeSiteId") Long coffeeSiteId,
                                                    Locale locale) {
       if (file == null) {
           throw new BadRESTRequestException(messages.getMessage("coffeesite.image.upload.rest.error", null, locale));
       }
       
       Integer imageId = imageStorageService.storeImageFile(file, coffeeSiteId, false);
       if (imageId == null || imageId == 0) {
           return new ResponseEntity<>( HttpStatus.NOT_FOUND);
       }
       log.info("Image uploaded. CoffeeSite id: {}. Image id: {}", coffeeSiteId, imageId);
       CoffeeSiteDTO cs = coffeeSiteService.findOneToTransfer(coffeeSiteId);
       return new ResponseEntity<>(cs.getMainImageURL(), HttpStatus.OK);
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani obrazku/Image k jednomu CoffeeSitu.<br>
     * 
     * @param id of the Image to delete
     * @return coffeeSiteId the image belonged to before deleting
     */
    @DeleteMapping("/delete/{imageId}") 
    public ResponseEntity<Long> deleteImageByImageId(@PathVariable Integer imageId) {
        
        Long siteId = imageStorageService.deleteSiteImageById(imageId);
        
        if (siteId == null || siteId == 0) {
            return new ResponseEntity<>(0L, HttpStatus.NOT_FOUND);
        }
        log.info("Image deleted. Image id: {}", imageId);
        return new ResponseEntity<>(siteId, HttpStatus.OK);
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani obrazku/Image k jednomu CoffeeSitu.<br>
     * 
     * @param id of the Image to delete
     * @return coffeeSiteId the image belonged to before deleting. Can be used to compare if requested coffeeSiteId
     * is same as returned one
     */
    @DeleteMapping("/delete/site/{coffeeSiteId}") 
    public ResponseEntity<Long> deleteImageBySiteId(@PathVariable Long coffeeSiteId) {
        
        Long siteId = imageStorageService.deleteSiteImageBySiteId(coffeeSiteId);
        
        if (siteId == null || siteId == 0) {
            return new ResponseEntity<>(0L, HttpStatus.NOT_FOUND);
        }
        log.info("Image deleted. CoffeeSite id: {}.", coffeeSiteId);
        return new ResponseEntity<>(siteId, HttpStatus.OK);
    }
}
