package cz.fungisoft.coffeecompass.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.image.ImageResizeAndRotateService;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;

@Controller
public class ImageRotationController {
    
    private final ImageStorageService imageStorageService;
    
    private final ImageResizeAndRotateService imageRotateService;
    
    private final CoffeeSiteService cofeeSiteService;
    
    @Autowired
    public ImageRotationController(ImageStorageService imageStorageService,
                                   ImageResizeAndRotateService imageRotateService,
                                   CoffeeSiteService cofeeSiteService) {
        super();
        this.imageStorageService = imageStorageService;
        this.imageRotateService = imageRotateService;
        this.cofeeSiteService = cofeeSiteService;
    }

    /**
     * 
     * @param siteID
     * @return
     */
    @PutMapping("/rotateImageLeft/") // http://localhost:8080/rotateImageLeft/?siteID=1
    public String rotateLeftAndSave(@RequestParam Long siteID) {
        Image siteImage = imageStorageService.getImageForSiteId(siteID);

        if (siteImage != null) {
            // Rotate
            Image rotatedImage = imageRotateService.rotate90DegreeLeft(siteImage);
            // Save new, rotated
            imageStorageService.saveImageToDB(rotatedImage);
        }

        return "redirect:/showSite/" + siteID;
    }
    
    /**
     * 
     * @param siteID
     * @return
     */
    @PutMapping("/rotateImageRight/") // http://localhost:8080/rotateImageRight/?siteID=2
    public String rotateRightAndSave(@RequestParam Long siteID) {
        Image siteImage = imageStorageService.getImageForSiteId(siteID);
        
        if (siteImage != null) {
            // Rotate
            Image rotatedImage = imageRotateService.rotate90DegreeRight(siteImage);
            // Save new, rotated
            imageStorageService.saveImageToDB(rotatedImage);
        }

        return "redirect:/showSite/" + siteID;
    }
}
