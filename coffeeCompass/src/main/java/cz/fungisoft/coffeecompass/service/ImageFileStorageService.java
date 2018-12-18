/**
 * 
 */
package cz.fungisoft.coffeecompass.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Image;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author Michal
 *
 */
public interface ImageFileStorageService
{
//    void init();
    String storeFile(MultipartFile file);
    
    /**
     * Save Image and return db ID of the saved image
     * 
     * @param image
     * @param file
     * @param siteID
     * @return
     */
    Integer storeImageFile(Image image, MultipartFile file, Integer siteID);

    Path loadFile(String filename);

    Image getImageById(Integer imageID);
    Resource loadFileAsResource(String filename);
    Resource getImageAsResource(Integer imageID);
    String getImageAsBase64(Integer imageID);

    void deleteFile(String filename);

    Integer deleteSiteImageById(Integer id);

    Image getImageForSiteId(Integer siteId);
    
}
