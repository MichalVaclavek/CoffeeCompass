/**
 * 
 */
package cz.fungisoft.test.image2.service;

import cz.fungisoft.test.image2.entity.ImageFileSet;
import cz.fungisoft.test.image2.entity.ImageObject;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * @author Michal Vaclavek
 *
 */
public interface ImageFilesDeleteOperationService {

    void deleteFiles(ImageFileSet imageFileSet, Path fileStorageLocation);
}
