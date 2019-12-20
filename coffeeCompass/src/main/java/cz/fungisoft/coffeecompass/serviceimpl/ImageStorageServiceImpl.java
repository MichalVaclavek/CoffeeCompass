package cz.fungisoft.coffeecompass.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cz.fungisoft.coffeecompass.configuration.ConfigProperties;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.exceptions.StorageFileException;
import cz.fungisoft.coffeecompass.repository.ImageRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.ImageStorageService;
import cz.fungisoft.coffeecompass.service.ImageResizeAndRotateService;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;

import javax.transaction.Transactional;
import javax.validation.ValidationException;

/**
 * Sluzba pro praci s objekty Image, obrazek CoffeeSitu. Ukladani, mazani, konverze na Base64String apod.
 * 
 * @author Michal Vaclavek
 *
 */
@Service("imageFileStorageService")
@Log4j2
public class ImageStorageServiceImpl implements ImageStorageService
{
    // Currently not used in production (image files are stored in DB). Can be used for testing.
    private final Path fileStorageLocation;
    
    private ImageRepository imageRepo;
    
    private ImageResizeAndRotateService imageResizer;

    private CoffeeSiteService coffeeSiteService;
    
    private ConfigProperties config;
    
    /**
     * Base part of the CoffeeSite's image URL, loaded from ConfigProperties.<br>
     * Complete URL of the image provided by {@link CoffeeSiteService#getMainImageURL()}.
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
    public ImageStorageServiceImpl(ConfigProperties fileStorageProperties, ImageRepository imageRepo, ImageResizeAndRotateService imageResizer) {
        
        this.imageRepo = imageRepo;
        this.imageResizer = imageResizer;
        
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                                        .toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new StorageFileException("Could not create the directory for storing uploaded files.", ex);
        }
    }
    
    /**
     * Used setter injection for {@code CoffeeSiteService} to avoid circular dependency
     * 
     * @param coffeeSiteService
     */
    @Autowired
    public void setCoffeeSiteService(CoffeeSiteService coffeeSiteService) {
        this.coffeeSiteService = coffeeSiteService;
    }

    /**
     * Setter for ConfigProperties attribute. If it was injected using @Autowired on attribute and
     * used within Constructor, then it was null.
     * 
     * @param config
     */
    @Autowired
    public void setConfig(ConfigProperties config) {
        this.config = config;
        if (this.config != null) {
            baseImageURLPath = config.getBaseURLPathforImages();
        }
    }

    /*
     * Stores uploaded image file into local file system file
    @Override
    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        log.info("File saved: " + fileName);

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new StorageFileException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new StorageFileException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
    */
    
    /**
     * Saves the image file into DB and creates Image object for CoffeeSite with siteID
     */
    @Override
    @Transactional
    public Integer storeImageFile(Image image, MultipartFile file, Long siteID) {
        
        Integer retVal = 0;
        CoffeeSite cs = coffeeSiteService.findOneById(siteID);
        if (cs != null) {
            try {
                // Check if there is already image assigned to the CS. If yes, delete the old image first
                if (cs.getImage() != null) {
                    Image oldImage = cs.getImage();
                    imageRepo.delete(oldImage);
                }
                image.setImageBytes(file.getBytes());
                image.setFile(file);
                image.setCoffeeSite(cs);
                image.setSavedOn(new Timestamp(new Date().getTime()));
            } catch (IOException e) {
                log.warn("Error during resizing Image. File name: {}. CoffeeSite name: {}. Exception: {}", image.getFileName(), cs.getSiteName(), e.getMessage());
            }
            try {
                image = imageResizer.resize(image);
            } catch (IOException e) {
                log.warn("Error during resizing Image. File name: {}. CoffeeSite name: {}. Exception: {}", image.getFileName(), cs.getSiteName(), e.getMessage());
            }
            imageRepo.save(image);
            cs.setImage(image);
            log.info("Image saved. File name: {}. CoffeeSite name: {}", image.getFileName(), cs.getSiteName());
            retVal = image.getId();
        } 
        
        return retVal;
    }
    
    /**
     * Saves already created Image object.
     * If there is already assigned Image to CoffeeSite, delete old image first
     */
    @Override
    @Transactional
    public void saveImageToDB(Image image) {
        try {
            image.setSavedOn(new Timestamp(new Date().getTime()));
            imageRepo.save(image);
         } catch (ValidationException ex) {
            log.error("Failed to validate: {}", ex); 
         }
    }
    
    @Override
    public Image getImageById(Integer imageID) {
        return imageRepo.getOne(imageID);
    }
   
    @Override
    public String getImageAsBase64(Integer imageID) {
        
        Image imFromDB = getImageById(imageID);
        return convertImageToBase64(imFromDB);
    }
    
    @Override
    public String getImageAsBase64ForSiteId(Long siteID) {
        return convertImageToBase64(coffeeSiteService.findOneById(siteID).getImage());
    }

    @Override
    public byte[] getImageAsBytesForSiteId(Long siteId) {
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
     * @return id of the CoffeeSites this image was belonging to before delete
     */
    @Transactional
    @Override
    public Long deleteSiteImageById(Integer imageId) {
        Long siteId = imageRepo.getSiteIdForImage(imageId);
        imageRepo.deleteById(imageId);
        log.info("Image deleted for CoffeeSite id: {}", siteId);
        return siteId;
    }
    
    @Transactional
    @Override
    public Long deleteSiteImageBySiteId(Long coffeeSiteId) {
        imageRepo.deleteBySiteId(coffeeSiteId);
        log.info("Image deleted for CoffeeSite id: {}", coffeeSiteId);
        return coffeeSiteId;
    }
    
    @Transactional
    @Override
    public Image getImageForSiteId(Long siteId) {
        return imageRepo.getImageForSite(siteId);
    }
    
    @Transactional
    @Override
    public Integer getImageIdForSiteId(Long siteID) {
        return imageRepo.getImageIdForSiteId(siteID);
    }

    @Override
    public boolean isImageAvailableForSiteId(Long siteId) {
        return imageRepo.getNumOfImagesForSiteId(siteId) > 0;
    }
    
    @Override
    public String getBaseImageURLPath() {
        return baseImageURLPath;
    }

}
