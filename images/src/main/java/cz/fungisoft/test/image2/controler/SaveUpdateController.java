/**
 * 
 */
package cz.fungisoft.test.image2.controler;

import cz.fungisoft.test.image2.entity.ImageObject;
import cz.fungisoft.test.image2.exceptions.BadRequestException;
import cz.fungisoft.test.image2.service.ImageFileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Controller to handle operations concerning obtaining CoffeeSite's image file.<br>
 * REST version
 *  
 * @author Michal Vaclavek
 *
 */
@RestController
@RequestMapping("${site.image.baseurlpath.rest}")
public class SaveUpdateController {

    private static final Logger log = LoggerFactory.getLogger(SaveUpdateController.class);

    private final ImageFileStorageService imageFileStorageService;

    private final MessageSource messages;

    public SaveUpdateController(ImageFileStorageService storageFileService,
                                MessageSource messages) {
        this.imageFileStorageService = storageFileService;
        this.messages = messages;
    }


    /**
     * Serves upload new image request for Object. Object is identified by it's ID included
     * in the ImageData object to be uploaded/saved.<br>
     *
     *
     * @param file uploaded ImageData from View. Contains file to be uploaded
     * @param objectExtId  ID of the external object the image belongs to
     *
     * @return load ImageObject with all images IDs
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // POST https://localhost:8080/api/v1/images/upload?externalObjectId=dsdsd a správné Body typu form-data
    public ResponseEntity<String> handleFileUpload(@RequestPart("file") MultipartFile file,
                                        @RequestPart String description,
                                        @RequestPart String type,
                                        @RequestPart("objectExtId") String objectExtId) {
        if (file == null) {
            throw new BadRequestException(messages.getMessage("image.upload.rest.error", null, null));
        }
        String imageExtId = imageFileStorageService.storeImageFile(file, objectExtId, description, type);

        return ResponseEntity.ok(imageExtId);
    }

    /**
     *
     * In one operation, deletes the image identified by Object and Image Id and saves new Images with the same ID
     *
     * @param file
     * @param description
     * @return
     */
    @PostMapping(value = "/object/{objectExtId}/replace/{imageExtId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // POST https://localhost:8080/api/v1/images/object/objectId/replace/{imageId} a správné Body typu form-data
    public ResponseEntity<ImageObject> replaceImage(@RequestPart("file") MultipartFile file,
                                                    @RequestPart String description,
                                                    @RequestPart String type,
                                                    @PathVariable("objectExtId") String objectExtId,
                                                    @PathVariable("imageExtId") String imageExtId) {
        if (file == null) {
            throw new BadRequestException(messages.getMessage("image.upload.rest.error", null, null));
        }

        Optional<ImageObject> imageObject = imageFileStorageService.replaceImageFile(file, objectExtId, imageExtId, description, type);

        return imageObject.map(ResponseEntity::ok)
                          .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }
}
