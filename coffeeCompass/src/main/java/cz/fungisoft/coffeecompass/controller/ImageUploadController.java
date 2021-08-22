/**
 * 
 */
package cz.fungisoft.coffeecompass.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;

/**
 * Controller to handle operations concerning upload or delete CoffeeSite's image file.
 *  
 * @author Michal Vaclavek
 *
 */
@Controller
public class ImageUploadController {

    private static final String REDIRECT_SHOW_SITE_VIEW = "redirect:/showSite/";
    
    private final ImageStorageService imageStorageService;

    @Autowired
    public ImageUploadController(ImageStorageService storageService) {
        this.imageStorageService = storageService;
    }

    /**
     * Serves upload image request for CoffeeSite. Coffee site is identified by it's ID included
     * in the Image object to be uploaded/saved.
     * 
     * @param newImage uploaded Image from View. Contains file to be uploaded and ID of the coffeeSite the image belongs to.
     * @param result for checking errors during form validation
     * @param redirectAttributes attributes to be passed to other Controller after redirection from this View/Controller.
     * @return
     */
    @PostMapping("/imageUpload")
    public String handleFileUpload(@ModelAttribute("newImage") @Valid Image newImage, BindingResult result, RedirectAttributes redirectAttributes) {
    
       Long siteId = newImage.getCoffeeSiteID();
       
       if (result.hasErrors()) {
           redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newImage", result);
           redirectAttributes.addFlashAttribute("newImage", newImage); // to pass validation errors to other View
           return REDIRECT_SHOW_SITE_VIEW + siteId;
       }
        
       imageStorageService.storeImageFile(newImage, newImage.getFile(), siteId, true);
       redirectAttributes.addFlashAttribute("savedFileName", newImage.getFile().getOriginalFilename());
       redirectAttributes.addFlashAttribute("uploadSuccessMessage", "You have successfully uploaded " + newImage.getFileName() + "!");

       return REDIRECT_SHOW_SITE_VIEW + siteId;
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani obrazku/Image ze stranky zobrazujici detaily k jednomu CoffeeSitu.<br>
     * Muze byt volano pouze ADMINEM resp. zakladatelem coffee situ (zarizeno v Thymeleaf View strance coffeesite_detail.html)
     * 
     * @param id of the Image to delete
     * @return
     */
    @DeleteMapping("/deleteImage/{id}") 
    public ModelAndView deleteImage(@PathVariable Integer id) {
        // Smazat Image daneho coffeeSite - need to have site Id to give it to /showSite Controller
        Long siteId = imageStorageService.deleteSiteImageById(id);
        
        // Show same coffee site with deleted Image
        return new ModelAndView(REDIRECT_SHOW_SITE_VIEW + siteId);
    }
}
