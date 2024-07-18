/**
 * 
 */
package cz.fungisoft.test.image2.service;

import cz.fungisoft.test.image2.dto.ImageObjectDto;
import cz.fungisoft.test.image2.entity.ImageFileSet;
import cz.fungisoft.test.image2.entity.ImageObject;

import java.util.Optional;

/**
 * @author Michal Vaclavek
 *
 */
public interface ImageObjectStorageService {

    Optional<ImageObject> getImageObjectByExtId(String imageObjectExtId);

    Optional<ImageObjectDto> getImageObjectByExtIdToTransfer(String imageObjectExtId);

    Optional<ImageFileSet> getImageFileSetByExtIds(String imageObjectExtId, String imageFileExtId);

    void deleteImageByExtId(String objectExtId, String imageExtId);

    void deleteAllExternalObjectImages(String imageObjectExtId);

    ImageObject save(ImageObject imo);
}
