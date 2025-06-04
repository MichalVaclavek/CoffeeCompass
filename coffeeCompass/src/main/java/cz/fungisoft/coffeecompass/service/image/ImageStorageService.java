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
    
    String getImageAsBase64ForSiteId(String siteID);

    String deleteSiteImageById(UUID imageId);
    String deleteSiteImageBySiteId(String coffeeSiteId);

    Image getImageForSiteId(UUID siteId);

    byte[] getImageAsBytesForSiteId(UUID siteId);

    Optional<byte[]> getImageAsBytesForSiteExternalId(String siteExternalId);

    boolean isImageAvailableForSiteId(UUID siteId);

    String getBaseImageURLPath();
}
