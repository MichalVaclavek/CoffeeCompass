/**
 * 
 */
package cz.fungisoft.test.image2.controler;

import cz.fungisoft.test.image2.service.ImageObjectStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle operations concerning obtaining CoffeeSite's image file.<br>
 * REST version
 *  
 * @author Michal Vaclavek
 *
 */
@RestController
@RequestMapping("${site.image.baseurlpath.rest}" + "/object")
public class DeleteController {

    private static final Logger log = LoggerFactory.getLogger(DeleteController.class);

    private final ImageObjectStorageService imageObjectStorageService;

    private final MessageSource messages;

    public DeleteController(ImageObjectStorageService imageObjectStorageService,
                            MessageSource messages) {
        this.imageObjectStorageService = imageObjectStorageService;
        this.messages = messages;
    }


    /**
     * Zpracuje DELETE pozadavek na smazani obrazku/ImageData k jednomu Object ID<br>
     *
     * @param imageExtId id of the ImageData to be deleted
     */
    @DeleteMapping("/{objectExtId}/image/{imageExtId}")
    public ResponseEntity<Void> deleteImageByImageId(@PathVariable String objectExtId, @PathVariable String imageExtId) {
        var imgSet= imageObjectStorageService.getImageFileSetByExtIds(objectExtId, imageExtId);
        imgSet.ifPresent( imgs -> imageObjectStorageService.deleteImageByExtId(objectExtId, imageExtId));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Zpracuje DELETE pozadavek na smazani vsech obrazku k danemu objectExtId
     *
     * @param objectExtId id of the Object who's ImageData is to be deleted
     */
    @DeleteMapping("/{objectExtId}/all")
    public ResponseEntity<Void> deleteImageByObjectId(@PathVariable String objectExtId) {
        var imageObject= imageObjectStorageService.getImageObjectByExtId(objectExtId);
        imageObject.ifPresent( io -> imageObjectStorageService.deleteAllExternalObjectImages(objectExtId));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
