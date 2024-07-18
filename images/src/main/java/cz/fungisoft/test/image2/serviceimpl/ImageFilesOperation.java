package cz.fungisoft.test.image2.serviceimpl;


import cz.fungisoft.test.image2.entity.ImageFileSet;
import cz.fungisoft.test.image2.service.ImageFilesDeleteOperationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sluzba pro smazani vsech souboru, ktere vnikly pri ukladani objektu ImageFileSet
 *
 * @author Michal Vaclavek
 */
@Service("imageFilesOperationService")
@Slf4j
public class ImageFilesOperation implements ImageFilesDeleteOperationService {


    @Override
    public void deleteFiles(ImageFileSet imageFileSet, Path fileStorageLocation) {
        Path imageObjectFolder = fileStorageLocation.resolve(imageFileSet.getImageObject().getExternalObjectId());

        Path imageOriginalFileLocation = imageObjectFolder.resolve(imageFileSet.getOriginalFileName());
        deleteFileIfExists(imageOriginalFileLocation);

        Path imageHdFileLocation = imageObjectFolder.resolve(imageFileSet.getFileNameHd());
        deleteFileIfExists(imageHdFileLocation);

        Path imageThumbnailLargeLocation = imageObjectFolder.resolve(imageFileSet.getThumbnailLargeName());
        deleteFileIfExists(imageThumbnailLargeLocation);

        Path imageThumbnailMidLocation = imageObjectFolder.resolve(imageFileSet.getThumbnailMidName());
        deleteFileIfExists(imageThumbnailMidLocation);

        Path imageThumbnailSmallLocation = imageObjectFolder.resolve(imageFileSet.getThumbnailSmallName());
        deleteFileIfExists(imageThumbnailSmallLocation);
    }

    private void deleteFileIfExists(Path fileToDelete) {
        try {
            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
            }
        } catch(IOException ex) {
            log.error("Error deleting file: {}", ex.getMessage());
        }
    }
}
