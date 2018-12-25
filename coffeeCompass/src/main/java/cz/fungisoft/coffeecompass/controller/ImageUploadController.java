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
import cz.fungisoft.coffeecompass.service.ImageFileStorageService;
import io.swagger.annotations.Api;

/**
 * Controller to handle operations concerning upload or delete CoffeeSite's image files
 *  
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@Controller
public class ImageUploadController
{
    private final ImageFileStorageService imageStorageService;

    @Autowired
    public ImageUploadController(ImageFileStorageService storageService) {
        this.imageStorageService = storageService;
    }

    /**
     * Serves upload image request to siteID.
     * 
     * @param image
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/imageUpload")
    public String handleFileUpload(@ModelAttribute("image") @Valid Image image, BindingResult result, RedirectAttributes redirectAttributes) {
    
       Long siteId = image.getCoffeeSiteID();
       
       if (result.hasErrors()) {
           result.rejectValue("file", "error.image.empty");
           return "redirect:/showSite/" + siteId;
       }
        
       Integer imageID = imageStorageService.storeImageFile(image, image.getFile(), siteId);
        
       redirectAttributes.addFlashAttribute("imageID", imageID);
       redirectAttributes.addFlashAttribute("savedFileName", image.getFile().getOriginalFilename());
       redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + image.getFileName() + "!");

       return "redirect:/showSite/" + siteId;
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare ze stranky zobrazujici komentare k jednomu CoffeeSitu.
     * Muze byt volano pouze ADMINEM (zarizeno v Thymeleaf View strance coffeesite_detail.html)
     * 
     * @param id of the Comment to delete
     * @return
     */
    @DeleteMapping("/deleteImage/{id}") 
    public ModelAndView deleteCommentAndStarsForSite(@PathVariable Integer id) {
        // Smazat Image daneho coffeeSite - need to have site Id to give it to /showSite Controller
        Integer siteId = imageStorageService.deleteSiteImageById(id);
        
        // Show same coffee site with updated Stars and comments
        ModelAndView mav = new ModelAndView("redirect:/showSite/" + siteId);
        
        return mav;
    }

}
