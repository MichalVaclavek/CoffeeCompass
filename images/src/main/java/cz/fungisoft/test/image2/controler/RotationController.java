/**
 *
 */
package cz.fungisoft.test.image2.controler;

import cz.fungisoft.test.image2.service.ImageFileStorageService;
import cz.fungisoft.test.image2.service.ImageRotationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle operations concerning obtaining CoffeeSite's image file.<br>
 * REST version
 *
 * @author Michal Vaclavek
 */
@RestController
@RequestMapping("${site.image.baseurlpath.rest}")
public class RotationController {

    private static final Logger log = LoggerFactory.getLogger(RotationController.class);

    private final ImageRotationService imageRotationService;

    private final ImageFileStorageService imageFileStorageService;

    private final MessageSource messages;

    public RotationController(ImageFileStorageService imageFileStorageService,
                              ImageRotationService imageRotationService,
                              MessageSource messages) {
        this.imageFileStorageService = imageFileStorageService;
        this.imageRotationService = imageRotationService;
        this.messages = messages;
    }


    @PutMapping({"/rotate/{imageExtId}/direction/{direction}"})
    public ResponseEntity<Void> rotateImage(@PathVariable("imageExtId") String imageExtId,
                                            @PathVariable(name = "direction") String direction) {
        var imgSet= imageFileStorageService.getImageFileSetByExtId(imageExtId);
        imgSet.ifPresent(im -> {
                    if ("right".equalsIgnoreCase(direction)) {
                        imageRotationService.rotate90DegreeRight(imageExtId);
                    }
                    if ("left".equalsIgnoreCase(direction)) {
                        imageRotationService.rotate90DegreeLeft(imageExtId);
                    }
                });

        return new ResponseEntity<>(imgSet.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
