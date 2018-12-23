/**
 * 
 */
package cz.fungisoft.coffeecompass.service;

import cz.fungisoft.coffeecompass.entity.Image;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Michal Vaclavek
 *
 */
public interface ImageFileStorageService
{
//    String storeFile(MultipartFile file);
    
    /**
     * Save Image and return db ID of the saved image
     * 
     * @param image
     * @param file
     * @param siteID
     * @return
     */
    Integer storeImageFile(Image image, MultipartFile file, Long siteID);

//    Path loadFile(String filename);

    Image getImageById(Integer imageID);
//    Resource loadFileAsResource(String filename);
//    Resource getImageAsResource(Integer imageID);
    String getImageAsBase64(Integer imageID);
    String getImageAsBase64ForSiteId(Long siteID);

//    void deleteFile(String filename);

    Integer deleteSiteImageById(Integer imageId);

    Image getImageForSiteId(Long siteId);
    
}
