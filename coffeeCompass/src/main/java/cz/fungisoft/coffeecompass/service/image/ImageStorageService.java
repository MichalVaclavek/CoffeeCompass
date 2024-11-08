/**
 * 
 */
package cz.fungisoft.coffeecompass.service.image;

import cz.fungisoft.coffeecompass.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Michal Vaclavek
 *
 */
public interface ImageStorageService {

    /**
     * Save Image and return db ID of the saved image.
     * This is version used for saving Image inserted via web form in Thymeleaf
     * 
     * @param image to be saved
     * @param file file containing image
     * @param siteID - site ID to which the image belongs to
     * @param resize - if the image should be resized to 'standard' size i.e. width 1024 and height 768 currently.
     * Usualy true as it is expected that image is larger
     *  
     * @return id of the saved Image
     */
    UUID storeImageFile(Image image, MultipartFile file, UUID siteID, boolean resize);

    /**
     * Save Image and return db ID of the saved image.
     * This is version for savin image file uploaded via REST api.
     * 
     * @param file file containing image
     * @param siteID - site ID to which the image belongs to
     * @param resize - if the image should be resized to 'standard' size i.e. width 1024 and height 768 currently
     *  can be sent via REST, default is false as for example from Android it is sent already resized to 1280x960
     *  
     * @return id of the saved Image
     */
    UUID storeImageFile(MultipartFile file, UUID siteID, boolean resize);
    
    void saveImageToDB(Image image);

    String getImageAsBase64ForSiteId(String siteID);
    UUID getImageIdForSiteId(String siteID);

    String deleteSiteImageById(UUID imageId);
    String deleteSiteImageBySiteId(String coffeeSiteId);

    Image getImageForSiteId(UUID siteId);

    byte[] getImageAsBytesForSiteId(UUID siteId);

    Optional<byte[]> getImageAsBytesForSiteExternalId(String siteExternalId);

    boolean isImageAvailableForSiteId(UUID siteId);

//    boolean isImageAvailableForSiteId(UUID siteExtId);

    String getBaseImageURLPath();
}
