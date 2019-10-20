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
public interface ImageStorageService
{
    /**
     * Save Image and return db ID of the saved image
     * 
     * @param image
     * @param file
     * @param siteID
     * @return id of the saved Image
     */
    public Integer storeImageFile(Image image, MultipartFile file, Long siteID);
    
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
