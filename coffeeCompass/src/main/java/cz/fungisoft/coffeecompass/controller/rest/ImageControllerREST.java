/**
 * 
 */
package cz.fungisoft.coffeecompass.controller.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.service.ImageFileStorageService;
import io.swagger.annotations.Api;

/**
 * Controller to handle operations concerning upload or delete CoffeeSite's image file.<br>
 * REST verze.
 *  
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/rest/image")
public class ImageControllerREST
{
    private final ImageFileStorageService imageStorageService;

    @Autowired
    public ImageControllerREST(ImageFileStorageService storageService) {
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
    @GetMapping("/{siteId}") // napr. http://coffeecompass.cz/rest/image/2
    public ResponseEntity<String> imageBySiteId(@PathVariable Long siteId) {
        
        // Add picture object (image of this coffee site) to the model
        String picString = imageStorageService.getImageAsBase64ForSiteId(siteId);
        
        if (picString == null || picString.isEmpty()) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(picString, HttpStatus.OK);
    }

}
