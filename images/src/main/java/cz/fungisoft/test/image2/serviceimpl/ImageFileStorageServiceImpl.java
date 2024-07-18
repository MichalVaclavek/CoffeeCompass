package cz.fungisoft.test.image2.serviceimpl;

import cz.fungisoft.test.image2.configuration.ConfigProperties;
import cz.fungisoft.test.image2.entity.ImageFileSet;
import cz.fungisoft.test.image2.entity.ImageObject;
import cz.fungisoft.test.image2.exceptions.StorageFileException;
import cz.fungisoft.test.image2.repository.ImageFileRepository;
import cz.fungisoft.test.image2.service.ImageFileStorageService;
import cz.fungisoft.test.image2.service.ImageFilesDeleteOperationService;
import cz.fungisoft.test.image2.service.ImageObjectStorageService;
import cz.fungisoft.test.image2.service.ImageResizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Sluzba pro ukladani objektu ImageFileSet
 *
 * @author Michal Vaclavek
 */
@Service("imageFileStorageService")
@Slf4j
public class ImageFileStorageServiceImpl implements ImageFileStorageService {

    private static final String THUMBNAIL_LARGE = "thumbnail-large";
    private static final String THUMBNAIL_MID = "thumbnail-mid";
    private static final String THUMBNAIL_SMALL = "thumbnail-small";

    private static final double LARGE_RATIO = 0.5;
    private static final double MID_RATIO = 0.25;
    private static final double SMALL_RATIO = 0.125;

    private static final byte[] EMPTY_IMAGE_BYTES = new byte[0];

    private static final Map<Double, String> THUMBNAIL_NAME_AND_RATIO = Map.of(
                LARGE_RATIO, THUMBNAIL_LARGE,
                MID_RATIO, THUMBNAIL_MID,
                SMALL_RATIO, THUMBNAIL_SMALL);


    private Map<Double, String> thumbnailFileNameAndRatio;

    private String originalFileName;

    private int imageWidthAfterResize = 0;
    private int imageHeightAfterResize = 0;


    private final ImageFileRepository imageFileRepo;

    private final ImageObjectStorageService imageObjectService;

    private final ImageResizeService imageResizeService;

    private final Path fileStorageLocation;

    private final ImageFilesDeleteOperationService imageFilesDeleteOperationService;


    /**
     * Base part of the CoffeeSite's image URL, loaded from ConfigProperties.<br>
     * <p>
     * as it can be build using current html request URI and current requsted CoffeeSite id.
     */
    private String baseImageURLPath;


    /**
     * Constructor to insert some of the services, repositories and config properties required.
     *
     * @param imageRepo
     */
    public ImageFileStorageServiceImpl(ImageFileRepository imageRepo,
                                       ImageObjectStorageService imageObjectService,
                                       ImageResizeService imageResizeService,
                                       ImageFilesDeleteOperationService imageFilesDeleteOperationService,
                                       ConfigProperties fileStorageProperties) {
        this.imageFileRepo = imageRepo;
        this.imageObjectService = imageObjectService;
        this.imageResizeService = imageResizeService;
        this.imageFilesDeleteOperationService = imageFilesDeleteOperationService;

        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new StorageFileException("Could not create the directory for storing uploaded files.", ex);
        }
    }

    @Override
    @Transactional
    public String storeImageFile(MultipartFile file, String objectExtId, String description, String type) {
        ImageFileSet imageFileSet = new ImageFileSet();
        imageFileSet.setExtId(UUID.randomUUID().toString());

        return storeImageFiles(file, objectExtId, description, type, imageFileSet);
    }

    @Override
    @Transactional
    public Optional<ImageObject> replaceImageFile(MultipartFile file, String objectExtId, String imageExtId, String description, String type) {
        // get ImageFileSet
        Optional<ImageFileSet> imageFileSet = imageObjectService.getImageFileSetByExtIds(objectExtId, imageExtId);
        // delete all images of the ImageFileSet
        imageFileSet.ifPresent(ifs -> imageFilesDeleteOperationService.deleteFiles(ifs, this.fileStorageLocation));
        // save new ImageFileSet files
        imageFileSet.ifPresent(ifs -> storeImageFiles(file, objectExtId, description, type, ifs));

        return imageObjectService.getImageObjectByExtId(objectExtId);
    }

    private String storeImageFiles(MultipartFile uploadedFile, String objectExtId, String imageDescription, String imageType, ImageFileSet imageFileSet) {
        imageFileSet.setFile(uploadedFile);
        imageFileSet.setDescription(imageDescription);
        imageFileSet.setImageType(imageType);

        originalFileName = uploadedFile.getOriginalFilename();
        imageFileSet.setOriginalFileName(originalFileName);

        thumbnailFileNameAndRatio = THUMBNAIL_NAME_AND_RATIO.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> createThumbnailFileName(entry.getValue())));

        Path imageObjectFolder = this.fileStorageLocation.resolve(objectExtId);
        try {
            if (!Files.exists(imageObjectFolder)) {
                Files.createDirectories(imageObjectFolder);
            }
        } catch (IOException ex) {
            log.error("Could not create subfolder " + imageObjectFolder);
            throw new StorageFileException("Could not create subfolder " + imageObjectFolder, ex);
        }

        String imagefileName = StringUtils.cleanPath(Objects.requireNonNull(originalFileName));
        try {
            // Copy original file to the target location (replacing existing file with the same name)
            Path targetLocation = imageObjectFolder.resolve(imagefileName);
            uploadedFile.transferTo(targetLocation);
            log.info("ImageData saved. Original file name: {}.", originalFileName);

            // Resize to HD size and save as jpg to the target location
            imagefileName = replaceSuffix("hd_" + imagefileName, "jpg");
            imageFileSet.setFileNameHd(imagefileName);
            targetLocation = imageObjectFolder.resolve(imagefileName);

            BufferedImage resizedImage = imageResizeService.resizeAndCompressImage(uploadedFile);
            imageWidthAfterResize = resizedImage.getWidth();
            imageHeightAfterResize = resizedImage.getHeight();
            ImageIO.write(resizedImage, "jpg", targetLocation.toFile());
            log.info("HD image saved. File name: {}.", imagefileName);
        } catch (IOException ex) {
            log.error("Could not store file " + imagefileName + ". Please try again!", ex);
            throw new StorageFileException("Could not store file " + imagefileName + ". Please try again!", ex);
        }

        // Generate and save thumbnails of the main image
        createThumbnails(uploadedFile, imageObjectFolder);
        imageFileSet.setThumbnailLargeName(thumbnailFileNameAndRatio.get(LARGE_RATIO));
        imageFileSet.setThumbnailMidName(thumbnailFileNameAndRatio.get(MID_RATIO));
        imageFileSet.setThumbnailSmallName(thumbnailFileNameAndRatio.get(SMALL_RATIO));

        ImageObject imo = imageObjectService.getImageObjectByExtId(objectExtId)
                                            .orElseGet(ImageObject::new);

        imo.setExternalObjectId(objectExtId);
        imo.getObjectImages().add(imageFileSet);
        imageFileSet.setImageObject(imo);

        imageObjectService.save(imo);
        imageFileRepo.save(imageFileSet);

        return imageFileSet.getExtId();
    }

    private void createThumbnails(MultipartFile file, Path imageObjectFolder) {
        final ExecutorService executorService = Executors.newFixedThreadPool(Math.min(THUMBNAIL_NAME_AND_RATIO.size(), Runtime.getRuntime().availableProcessors()));
        thumbnailFileNameAndRatio
                .forEach((resizeRatio, thumbnailFileName) ->
                        executorService.submit(() -> {
                            try {
                                resizeAndSaveThumbnailImage(file, thumbnailFileName, imageObjectFolder, (int) (imageWidthAfterResize * resizeRatio), (int) (imageHeightAfterResize * resizeRatio));
                            } catch (IOException ex) {
                                log.error("Could not store file.", ex);
                                throw new StorageFileException("Could not store file.", ex);
                            }
                }));

        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Interruption error.", ex);
        }
    }

    private void resizeAndSaveThumbnailImage(MultipartFile originalFile, String thumbnailFileName, Path imageFolder, int width, int height) throws IOException {
        Path targetLocation = imageFolder.resolve(thumbnailFileName);

        BufferedImage resizedImage = this.imageResizeService.resizeAndCompressImage(originalFile, width, height);
        ImageIO.write(resizedImage, "jpg", targetLocation.toFile());
    }

    private String createThumbnailFileName(String thumbnailName) {
        int lastDotIndex = this.originalFileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            // Remove the current suffix, add thumbnailName and add jpg suffix
            String nameWithoutSuffix = this.originalFileName.substring(0, lastDotIndex);
            return thumbnailName + "-" + nameWithoutSuffix  + ".jpg";
        } else {
            return thumbnailName + "-"  + this.originalFileName + ".jpg";
        }
    }

    private static String replaceSuffix(String filename, String newSuffix) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex != -1) {
            // Remove the current suffix and add the new one
            String nameWithoutSuffix = filename.substring(0, lastDotIndex);
            return nameWithoutSuffix + "." + newSuffix;
        } else {
            // If there's no existing suffix, simply append the new one
            return filename + "." + newSuffix;
        }
    }

    @Override
    public Optional<String> getImageAsBase64(String imageExtId, ImageSizes size) {
        return this.getImageFileSetByExtId(imageExtId).map(imgSet -> this.convertImageToBase64(imgSet, size));
    }

    @Override
    public byte[] getImageAsBytes(String imageExtId, ImageSizes size) {
        Optional<ImageFileSet> image = getImageFileSetByExtId(imageExtId);
        return image.map(imgSet -> this.getImageBytes(imgSet, size))
                    .orElse(EMPTY_IMAGE_BYTES);
    }

    @Override
    public Optional<String> getFirstImageOfTypeAsBase64(String objectExtId, String type, ImageSizes size) {
        return this.getFirstImageOfType(objectExtId, type)
                   .map(imgSet -> this.convertImageToBase64(imgSet, size));
    }

    @Override
    public byte[] getFirstImageOfTypeAsBytes(String objectExtId, String type, ImageSizes size) {
        Optional<ImageFileSet> image = getFirstImageOfType(objectExtId, type);

        return image.map(imgSet -> this.getImageBytes(imgSet, size))
                    .orElse(EMPTY_IMAGE_BYTES);
    }

    @Override
    public Optional<ImageFileSet> getImageFileSetByExtId(String imageExtID) {
        return imageFileRepo.findByExtId(imageExtID);
    }

    private Optional<ImageFileSet> getFirstImageOfType(String objectExtId, String type) {
        Optional<ImageObject> imageObject = imageObjectService.getImageObjectByExtId(objectExtId);
        return imageObject.map(ImageObject::getId)
                .stream()
                .map(imageObjectId -> imageFileRepo.findByImageObjectIdAndImageType(imageObjectId, type))
                .flatMap(Collection::stream)
                .max(Comparator.comparing(ImageFileSet::getSavedOn));
    }

    /**
     * Conversion of the Image bytes into "standard" Base64 String used in web browsers
     *
     * @param image
     * @return
     */
    private String convertImageToBase64(ImageFileSet image, ImageSizes variant) {
        StringBuilder imageString = new StringBuilder();
        if (image != null) {
            imageString.append(Base64.getEncoder().encodeToString(getImageBytes(image, variant)));
        }
        return imageString.toString();
    }

    private byte[] getImageBytes(ImageFileSet imageSet, ImageSizes variant) {
        byte[] imageBytes = EMPTY_IMAGE_BYTES;

        File imageFile = getImageFile(imageSet, variant);
        try (RandomAccessFile reader = new RandomAccessFile(imageFile, "r")) {
            FileChannel channel = reader.getChannel();
            ByteBuffer buff = ByteBuffer.allocate((int) channel.size());
            channel.read(buff);
            buff.flip();
            imageBytes = buff.array();
        } catch (IOException ex) {
            log.error("Neco se rozbilo. Cteni image bytes. Exception: " + ex.getMessage());
        }

        return imageBytes;
    }

    @Override
    public Optional<File> getImageFile(String imageExtId, ImageSizes size) {
        return this.getImageFileSetByExtId(imageExtId)
                   .map(imgSet -> this.getImageFile(imgSet, size));
    }

    private File getImageFile(ImageFileSet image, ImageSizes size) {
        String imageFileName =
                switch (size) {
                    case ORIGINAL -> image.getOriginalFileName();
                    case HD -> image.getFileNameHd();
                    case LARGE -> image.getThumbnailLargeName();
                    case MID -> image.getThumbnailMidName();
                    case SMALL -> image.getThumbnailSmallName();
                };
        Path targetLocation = this.fileStorageLocation.resolve(image.getImageObject().getExternalObjectId()).resolve(imageFileName);
        return targetLocation.toFile();
    }
}
