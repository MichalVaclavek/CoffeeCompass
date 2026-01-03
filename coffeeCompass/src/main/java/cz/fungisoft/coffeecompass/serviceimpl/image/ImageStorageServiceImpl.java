package cz.fungisoft.coffeecompass.serviceimpl.image;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cz.fungisoft.coffeecompass.configuration.ConfigProperties;
import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.exceptions.StorageFileException;
import cz.fungisoft.coffeecompass.repository.ImageRepository;
import cz.fungisoft.coffeecompass.service.image.ImageResizeAndRotateService;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.transaction.Transactional;

/**
 * Sluzba pro praci s objekty Image, obrazek CoffeeSitu. Ukladani, mazani, konverze na Base64String apod.
 * 
 * @author Michal Vaclavek
 */
@Service("imageFileStorageService")
@Slf4j
public class ImageStorageServiceImpl implements ImageStorageService {

    private final ImageRepository imageRepo;
    
    private final ImageResizeAndRotateService imageResizer;

    private CoffeeSiteRepository coffeeSiteRepository;

    /**
     * Base part of the CoffeeSite's image URL, loaded from ConfigProperties.<br>
     * Complete URL of the image is provided by {@link CoffeeSiteServiceImpl#getMainImageURL(CoffeeSite)} (CoffeeSiteDTO)}.
     * as it can be build using current html request URI and current requsted CoffeeSite id.
     */
    private String baseImageURLPath;
    
    /**
     * Constructor to insert some of the services, repositories and config properties required.
     * 
     * @param fileStorageProperties
     * @param imageRepo
     * @param imageResizer
     */
    @Autowired
    public ImageStorageServiceImpl(ConfigProperties fileStorageProperties, ImageRepository imageRepo, ImageResizeAndRotateService imageResizer, CoffeeSiteRepository coffeeSiteRepository) {
        
        this.imageRepo = imageRepo;
        this.imageResizer = imageResizer;
        this.coffeeSiteRepository = coffeeSiteRepository;

        // Currently not used in production (image files are stored in DB). Can be used for testing.
        Path fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new StorageFileException("Could not create the directory for storing uploaded files.", ex);
        }
    }
    
    /**
     * Setter for ConfigProperties attribute. If it was injected using @Autowired on attribute and
     * used within Constructor, then it was null.
     * 
     * @param config
     */
    @Autowired
    public void setConfig(ConfigProperties config) {
        if (config != null) {
            baseImageURLPath = config.getBaseURLPathforImages();
        }
    }

    /**
     * Saves the image file into DB and creates Image object for CoffeeSite with siteID
     */
    @Override
    @Transactional
    public UUID storeImageFile(MultipartFile file, UUID siteID, boolean resize) {
        AtomicReference<Image> atomicImage = new AtomicReference<>(null);
        coffeeSiteRepository.findById(siteID).ifPresent(cs -> {
            // Check if there is already image assigned to the CS. If yes, delete the old image first
            Optional.ofNullable(imageRepo.getImageForSite(siteID))
                    .ifPresent(imageRepo::delete);
            Image image = new Image();
            try {
                image.setImageBytes(file.getBytes());
                image.setFile(file);
                image.setCoffeeSiteId(cs.getId());
                image.setSavedOn(LocalDateTime.now());
            } catch (IOException e) {
                log.warn("Error during creating Image object. File name: {}. CoffeeSite name: {}. Exception: {}", image.getFileName(), cs.getSiteName(), e.getMessage());
            }
            try {
                if (resize) {
                    image = imageResizer.resize(image);
                }
            } catch (IOException e) {
                log.warn("Error during resizing Image. File name: {}. CoffeeSite name: {}. Exception: {}", image.getFileName(), cs.getSiteName(), e.getMessage());
            }
            if (image != null) {
                imageRepo.save(image);
                log.info("Image saved. File name: {}. CoffeeSite name: {}", image.getFileName(), cs.getSiteName());
                atomicImage.set(image);
            }
        });

        return atomicImage.get().getId();
}
    
    @Override
    public String getImageAsBase64ForSiteId(String siteID) {
        return convertImageToBase64(imageRepo.getImageForSite(UUID.fromString(siteID)));
    }

    @Override
    public Optional<byte[]> getImageAsBytesForSiteExternalId(String siteExternalId) {
        return coffeeSiteRepository.findById(UUID.fromString(siteExternalId)).map(CoffeeSite::getId).map(this::getImageAsBytesForSiteId);
    }

    @Override
    public byte[] getImageAsBytesForSiteId(UUID siteId) {
        Image image = getImageForSiteId(siteId);
        return (image != null) ? image.getImageBytes() : null;
    }
    
    /**
     * Conversion of the Image bytes into "standard" Base64 String used in web browsers
     * 
     * @param image
     * @return
     */
    private String convertImageToBase64(Image image) {
        StringBuilder imageString = new StringBuilder();
        
        if (image != null) {
            imageString.append("data:image/png;base64,");
            imageString.append(Base64.getEncoder().encodeToString(image.getImageBytes())); //bytes are image byte[] coming from DB
        }
        return imageString.toString();
    }

    /**
     * @return id of the CoffeeSites this image was belonging to before delete or 0 if image delete failed
     */
    @Transactional
    @Override
    public String deleteSiteImageById(UUID imageId) {
        UUID siteId;
        try {
            siteId = imageRepo.getSiteIdForImage(imageId);
            imageRepo.deleteById(imageId);
        } catch (Exception ex) {
            log.error("Image delete failed for Image id: {}", imageId);
            throw ex;
        }
        log.info("Image id {} deleted for CoffeeSite id: {}", imageId, siteId);
        return siteId.toString();
    }
    
    @Transactional
    @Override
    public String deleteSiteImageBySiteId(String coffeeSiteId) {
        try {
            imageRepo.deleteBySiteId(UUID.fromString(coffeeSiteId));
        } catch (Exception ex) {
            log.error("Image delete failed for CoffeeSite id: {}", coffeeSiteId);
            throw ex;
        }
        log.info("Image deleted for CoffeeSite id: {}", coffeeSiteId);
        return coffeeSiteId;
    }
    
    @Override
    public Image getImageForSiteId(UUID siteId) {
        return imageRepo.getImageForSite(siteId);
    }

    @Override
    public boolean isImageAvailableForSiteId(UUID siteId) {
        return imageRepo.getNumOfImagesForSiteId(siteId) > 0;
    }

    @Override
    public String getBaseImageURLPath() {
        return baseImageURLPath;
    }
}
