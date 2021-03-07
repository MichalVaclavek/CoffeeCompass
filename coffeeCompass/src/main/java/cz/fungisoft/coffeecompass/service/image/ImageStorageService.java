/**
 * 
 */
package cz.fungisoft.coffeecompass.service.image;

import cz.fungisoft.coffeecompass.entity.Image;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Michal Vaclavek
 *
 */
public interface ImageStorageService
{
    /**
     * Save Image and return db ID of the saved image.
     * This is version used for saving Image inserted via web form in Thymeleaf
     * 
     * @param image to be saved
     * @param file file containing image
     * @param siteID - site ID to which the image belongs to
     * @param rezize - if the image should be resized to 'standard' size i.e. width 1280 and height 960 currently.
     * Usualy true as it is expected that image is larger
     *  
     * @return id of the saved Image
     */
    public Integer storeImageFile(Image image, MultipartFile file, Long siteID, boolean resize);
    
    /**
     * Save Image and return db ID of the saved image.
     * This is version for savin image file uploaded via REST api.
     * 
     * @param file file containing image
     * @param siteID - site ID to which the image belongs to
     * @param rezize - if the image should be resized to 'standard' size i.e. width 1280 and height 960 currently
     *  can be sent via REST, default is false as for example from Android it is sent already resized to 1280x960
     *  
     * @return id of the saved Image
     */
    public Integer storeImageFile(MultipartFile file, Long siteID, boolean resize);
    
    public void saveImageToDB(Image image);

    public Image getImageById(Integer imageID);
    public String getImageAsBase64(Integer imageID);
    public String getImageAsBase64ForSiteId(Long siteID);
    public Integer getImageIdForSiteId(Long siteID);

    public Long deleteSiteImageById(Integer imageId);
    public Long deleteSiteImageBySiteId(Long coffeeSiteId);

    public Image getImageForSiteId(Long siteId);

    public byte[] getImageAsBytesForSiteId(Long siteId);

    public boolean isImageAvailableForSiteId(Long siteId);

    public String getBaseImageURLPath();
}
