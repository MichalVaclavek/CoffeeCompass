/**
 * 
 */
package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.dto.ImageDTO;
import cz.fungisoft.coffeecompass.serviceimpl.images.ImagesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
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
@RequiredArgsConstructor
public class ImageUploadController {

    private static final String REDIRECT_SHOW_SITE_VIEW = "redirect:/showSite/";
    
    private final ImagesService imagesService;

    /**
     * Serves upload image request for CoffeeSite. Coffee site is identified by it's External ID included
     * in the Image object to be uploaded/saved.
     *
     * @param newImage uploaded Image from View. Contains file to be uploaded and ID of the coffeeSite the image belongs to.
     * @param result for checking errors during form validation
     * @param redirectAttributes attributes to be passed to other Controller after redirection from this View/Controller.
     * @return
     */
    @PostMapping("/newImageUpload")
    public String handleImageFileUpload(@ModelAttribute("newImageFile") @Valid ImageDTO newImage, BindingResult result, RedirectAttributes redirectAttributes) {

        var redirectionURL = REDIRECT_SHOW_SITE_VIEW + newImage.getExternalObjectId();

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newImage", result);
            redirectAttributes.addFlashAttribute("newImage", newImage); // to pass validation errors to other View
            return redirectionURL;
        }

        var newImageExtId = imagesService.uploadImageFile(newImage.getFile(), newImage.getExternalObjectId(), newImage.getDescription(), newImage.getImageType());

        redirectAttributes.addFlashAttribute("savedFileName", newImage.getFile().getOriginalFilename());
        redirectAttributes.addFlashAttribute("uploadSuccessMessage", "You have successfully uploaded " + newImage.getFile().getOriginalFilename() + "!");

        return redirectionURL + "/selectedImageExtId/" + newImageExtId;
    }

    /**
     * Zpracuje DELETE pozadavek na smazani obrazku/Image ze stranky zobrazujici detaily k jednomu CoffeeSitu.<br>
     * Muze byt volano pouze ADMINEM resp. zakladatelem coffee situ (zarizeno v Thymeleaf View strance coffeesite_detail.html)
     * 
     * @param coffeeSiteExternalId of the Image to delete
     * @param selectedImageExtId of the Image to delete
     * @return
     */
    @DeleteMapping("/deleteImage/{coffeeSiteExternalId}/selectedImageExtId/{selectedImageExtId}")
    public ModelAndView deleteImage(@PathVariable String coffeeSiteExternalId,
                                    @PathVariable String selectedImageExtId) {
        // Smazat Image daneho coffeeSite - need to have site Id to give it to /showSite Controller
        imagesService.deleteImage(coffeeSiteExternalId, selectedImageExtId);
        // Show coffee site
        return new ModelAndView(REDIRECT_SHOW_SITE_VIEW + coffeeSiteExternalId);
    }
}
