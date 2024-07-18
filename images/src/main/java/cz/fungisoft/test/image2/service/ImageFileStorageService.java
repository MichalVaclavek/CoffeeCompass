/**
 * 
 */
package cz.fungisoft.test.image2.service;

import cz.fungisoft.test.image2.entity.ImageFileSet;
import cz.fungisoft.test.image2.entity.ImageObject;
import cz.fungisoft.test.image2.serviceimpl.ImageSizes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

/**
 * @author Michal Vaclavek
 *
 */
public interface ImageFileStorageService {
    /**
     * Returns saved image External Id
     * @param file
     * @param objectExtId
     * @param description
     * @param type
     * @return
     */
    String storeImageFile(MultipartFile file, String objectExtId, String description, String type);

    Optional<ImageObject> replaceImageFile(MultipartFile file, String objectExtId, String imageExtId, String description, String type);

    Optional<ImageFileSet> getImageFileSetByExtId(String imageExtID);

    Optional<String> getImageAsBase64(String imageExtId, ImageSizes size);

    byte[] getImageAsBytes(String imageExtId, ImageSizes size);

    Optional<String> getFirstImageOfTypeAsBase64(String objectExtId, String type, ImageSizes size);

    byte[] getFirstImageOfTypeAsBytes(String objectExtId, String type, ImageSizes size);

    Optional<File> getImageFile(String imageExtId, ImageSizes size);
}
