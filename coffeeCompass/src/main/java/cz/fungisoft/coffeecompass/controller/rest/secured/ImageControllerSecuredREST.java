package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.Locale;

import javax.validation.Valid;

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

import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.exceptions.rest.BadRESTRequestException;
import cz.fungisoft.coffeecompass.service.ImageStorageService;
import io.swagger.annotations.Api;

/**
* Controller to handle operations upload or delete CoffeeSite's image file.<br>
* REST version
*  
* @author Michal Vaclavek
*
*/
@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/rest/secured/image")
public class ImageControllerSecuredREST
{
    private final ImageStorageService imageStorageService;
    
    private MessageSource messages;

    @Autowired
    public ImageControllerSecuredREST(ImageStorageService storageService, MessageSource messages) {
        this.imageStorageService = storageService;
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
     * @return newly saved image ID. But bettter is to send 'load URL'  of the new Image  
     */
    @PostMapping("/upload") // POST https://coffeecompass.cz/rest/secured/image/upload?coffeeSiteId=2 a správné Body
    public ResponseEntity<Integer> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("coffeeSiteId") Long coffeeSiteId,
                                                    Locale locale) {
       //Image newImage = new Image();
       
       if (file == null) {
           throw new BadRESTRequestException(messages.getMessage("coffeesite.image.upload.rest.error", null, locale));
       }
       
       Integer imageId = imageStorageService.storeImageFile(file, coffeeSiteId, false);
       if (imageId == null || imageId == 0) {
           return new ResponseEntity<Integer>(0, HttpStatus.NOT_FOUND);
       }
       return new ResponseEntity<Integer>(imageId, HttpStatus.OK);
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani obrazku/Image k jednomu CoffeeSitu.<br>
     * 
     * @param id of the Image to delete
     * @return coffeeSiteId the image belonged to before deleting
     */
    @DeleteMapping("/delete/{id}") 
    public ResponseEntity<Long> deleteImageByImageId(@PathVariable Integer imageId) {
        // Smazat Image daneho coffeeSite - need to have site Id to give it to /showSite Controller
        Long siteId = imageStorageService.deleteSiteImageById(imageId);
        
        if (siteId == null || siteId == 0) {
            return new ResponseEntity<Long>(0L, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Long>(siteId, HttpStatus.OK);
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani obrazku/Image k jednomu CoffeeSitu.<br>
     * 
     * @param id of the Image to delete
     * @return coffeeSiteId the image belonged to before deleting. Can be used to compare if requested coffeeSiteId
     * is same as returned one
     */
    @DeleteMapping("/delete/site/{id}") 
    public ResponseEntity<Long> deleteImageBySiteId(@PathVariable Long coffeeSiteId) {
        // Smazat Image daneho coffeeSite - need to have site Id to give it to /showSite Controller
        //Long siteId = imageStorageService.deleteSiteImageById(id);
        Long siteId = imageStorageService.deleteSiteImageBySiteId(coffeeSiteId);
        
        if (siteId == null || siteId == 0) {
            return new ResponseEntity<Long>(0L, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Long>(siteId, HttpStatus.OK);
    }
    
}
