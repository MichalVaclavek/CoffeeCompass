package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.Image;

/**
 * JPA operations with Image entity.
 * 
 * @author Michal Vaclavek
 *
 */
public interface ImageRepository extends JpaRepository<Image, Integer> {

    @Query("select coffeeSite.id FROM Image im where id=?1")
    Long getSiteIdForImage(Integer imageId);
    
    @Query("select im from Image im WHERE coffeeSite.id=?1")
    Image getImageForSite(Long coffeeSiteID);
    
    @Query("select im.id from Image im WHERE coffeeSite.id=?1")
    Integer getImageIdForSiteId(Long siteID);
    
    @Modifying // required by Hibernate, otherwise there is an Exception ' ... Illegal state ...'
    @Query("delete from Image im WHERE coffeeSite.id=?1")
    void deleteBySiteId(Long coffeeSiteId);

    @Query("select COUNT(im) from Image im WHERE coffeeSite.id=?1")
    int getNumOfImagesForSiteId(Long siteId);

    @Query("select COUNT(*) from Image")
    int getNumOfAllImagesForAllSites();
}
