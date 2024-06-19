package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.serviceimpl.images.ImagesService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cz.fungisoft.coffeecompass.service.image.ImageResizeAndRotateService;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;

@Controller
public class ImageRotationController {

    private static final String REDIRECT_SHOW_SITE_VIEW = "redirect:/showSite/";

    private final ImagesService imagesService;

    
    public ImageRotationController(ImagesService imagesService) {
        super();
        this.imagesService = imagesService;
    }

    /**
     * 
     * @return
     */
    @PutMapping({"/rotateImageLeft/{siteExternalId}", "/rotateImageLeft/{siteExternalId}/selectedImageExtId/{selectedImageExtId}" }) // http://localhost:8080/rotateImageLeft/Ddfdf55/selectedImageExtId/xz&imageExternalId=as
    public String rotateLeft(@PathVariable(name = "siteExternalId") String siteExternalId,
                             @PathVariable(required = false) String selectedImageExtId) {
        imagesService.rotateImageLeft(selectedImageExtId);
        return REDIRECT_SHOW_SITE_VIEW + siteExternalId + "/selectedImageExtId/" + selectedImageExtId;
    }

    /**
     *
     * @return
     */
    @PutMapping( {"/rotateImageRight/{siteExternalId}", "/rotateImageRight/{siteExternalId}/selectedImageExtId/{selectedImageExtId}" }) // http://localhost:8080/rotateImageRight/siteExternalId=xz&imageExternalId=as
    public String rotateRight(@PathVariable(name = "siteExternalId") String siteExternalId,
                              @PathVariable(required = false) String selectedImageExtId) {
        imagesService.rotateImageRight(selectedImageExtId);
        return REDIRECT_SHOW_SITE_VIEW + siteExternalId + "/selectedImageExtId/" + selectedImageExtId;
    }
}
