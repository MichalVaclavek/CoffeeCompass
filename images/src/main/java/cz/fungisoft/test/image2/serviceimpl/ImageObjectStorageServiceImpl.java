package cz.fungisoft.test.image2.serviceimpl;

import cz.fungisoft.test.image2.configuration.ConfigProperties;
import cz.fungisoft.test.image2.dto.ImageObjectDto;
import cz.fungisoft.test.image2.entity.ImageFileSet;
import cz.fungisoft.test.image2.entity.ImageObject;
import cz.fungisoft.test.image2.mappers.ImageObjectMapper;
import cz.fungisoft.test.image2.repository.ImageObjectRepository;
import cz.fungisoft.test.image2.service.ImageFilesDeleteOperationService;
import cz.fungisoft.test.image2.service.ImageObjectStorageService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;


/**
 * Sluzba pro praci s objekty ImageObject
 *
 * @author Michal Vaclavek
 */
@Service("imageObjectStorageService")
@Slf4j
public class ImageObjectStorageServiceImpl implements ImageObjectStorageService {

    @Value("${site.image.baseurlpath.rest}" + "/bytes/")
    private String baseImageBytesUrlPath;

    @Value("${site.image.baseurlpath.rest}" + "/base64/")
    private String baseImageBase64UrlPath;

    @Value("${site.image.baseurlpath.rest}" + "/bytes/object/")
    private String baseImageObjectBytesUrlPath;

    @Value("${site.image.baseurlpath.rest}" + "/base64/object/")
    private String baseImageObjectBase64UrlPath;

    private final ImageObjectRepository imageObjectRepo;

    private final ImageFilesDeleteOperationService imageFilesDeleteOperationService;

    private final Path fileStorageLocation;

    private ImageObjectMapper imageObjectMapper;

    public ImageObjectStorageServiceImpl(ImageObjectRepository imageObjectRepo,
                                         ImageFilesDeleteOperationService imageFilesDeleteOperationService,
                                         ConfigProperties fileStorageProperties,
                                         ImageObjectMapper imageObjectMapper) {
        this.imageObjectRepo = imageObjectRepo;
        this.imageFilesDeleteOperationService = imageFilesDeleteOperationService;
        this.imageObjectMapper = imageObjectMapper;

        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
    }

    public Optional<ImageFileSet> getImageFileSetByExtIds(String imageObjectExtId, String imageFileExtId) {
        Optional<ImageObject> imageObject = getImageObjectByExtId(imageObjectExtId);
        return imageObject.map(ImageObject::getObjectImages)
                          .stream()
                          .flatMap(Collection::stream)
                          .filter(ifs -> ifs.getExtId().equals(imageFileExtId))
                          .findFirst();
    }

    @Override
    public Optional<ImageObject> getImageObjectByExtId(String imageObjectExtId) {
        return imageObjectRepo.findByExternalObjectId(imageObjectExtId);
    }

    @Override
    public Optional<ImageObjectDto> getImageObjectByExtIdToTransfer(String imageObjectExtId) {
        return this.getImageObjectByExtId(imageObjectExtId)
                   .map(io -> imageObjectMapper.imageObjectToImageObjectDto(io, baseImageObjectBytesUrlPath, baseImageObjectBase64UrlPath, baseImageBytesUrlPath, baseImageBase64UrlPath));
    }

    @Transactional
    @Override
    public void deleteAllExternalObjectImages(String imageObjectExtId) {
        // delete image files first
        Optional<ImageObject> imageObject = getImageObjectByExtId(imageObjectExtId);
        imageObject.map(ImageObject::getObjectImages)
                   .stream()
                   .flatMap(Collection::stream)
                   .forEach(imageFileSet -> imageFilesDeleteOperationService.deleteFiles(imageFileSet, this.fileStorageLocation));

        imageObjectRepo.deleteByExternalObjectId(imageObjectExtId);
    }

    @Override
    public ImageObject save(ImageObject imo) {
        return imageObjectRepo.save(imo);
    }


    @Override
    public void deleteImageByExtId(String objectExtId, String imageExtId) {
        Optional<ImageObject> imageObject = getImageObjectByExtId(objectExtId);
        imageObject.ifPresent(io -> {
            io.getObjectImages().stream()
                    .filter(ifs -> ifs.getExtId().equals(imageExtId))
                    .findFirst()
                    .ifPresent(ifs -> imageFilesDeleteOperationService.deleteFiles(ifs, this.fileStorageLocation));
            io.getObjectImages().removeIf(ifs -> ifs.getExtId().equals(imageExtId));
            imageObjectRepo.saveAndFlush(io);
        });
    }
}
