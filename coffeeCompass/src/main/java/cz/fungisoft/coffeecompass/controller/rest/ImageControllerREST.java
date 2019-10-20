/**
 * 
 */
package cz.fungisoft.coffeecompass.controller.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.service.ImageStorageService;
import io.swagger.annotations.Api;

/**
 * Controller to handle operations concerning upload or delete CoffeeSite's image file.<br>
 * REST version
 *  
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/rest/image")
public class ImageControllerREST
{
    private final ImageStorageService imageStorageService;

    @Autowired
    public ImageControllerREST(ImageStorageService storageService) {
        this.imageStorageService = storageService;
    }

    /**
     * Serves upload image request for CoffeeSite. Coffee site is identified by it's ID included
     * in the Image object to be uploaded/saved.<br>
     * 
     * NOT YET USED in REST CONTROLLER
     * 
     * @param image uploaded Image from View. Contains file to be uploaded and ID of the coffeeSite the image belongs to.
     * @param result for checking errors during form validation
     * @param redirectAttributes attributes to be passed to other Controller after redirection from this View/Controller.
     * @return
     */
    @PostMapping("/imageUpload") // POST http://coffeecompass.cz/rest/image/imageUpload
    public String handleFileUpload(@ModelAttribute("newImage") @Valid Image newImage, BindingResult result, RedirectAttributes redirectAttributes) {
    
       Long siteId = newImage.getCoffeeSiteID();
       
       if (result.hasErrors()) {
           redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newImage", result);
           redirectAttributes.addFlashAttribute("newImage", newImage); // to pass validation errors to other View
           return "redirect:/showSite/" + siteId;
       }
        
       imageStorageService.storeImageFile(newImage, newImage.getFile(), siteId);
       redirectAttributes.addFlashAttribute("savedFileName", newImage.getFile().getOriginalFilename());
       redirectAttributes.addFlashAttribute("uploadSuccessMessage", "You successfully uploaded " + newImage.getFileName() + "!");

       return "redirect:/showSite/" + siteId;
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
