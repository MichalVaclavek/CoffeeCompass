package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.Image;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.UUID;

/**
 * JPA operations with Image entity.
 * 
 * @author Michal Vaclavek
 *
 */
public interface ImageRepository extends JpaRepository<Image, UUID> {

    @Query("select coffeeSiteID FROM Image im where id=?1")
    UUID getSiteIdForImage(UUID imageId);

    @Query("select im from Image im WHERE coffeeSiteID=?1")
    Image getImageForSite(UUID coffeeSiteID);
    
    @Query("select im.id from Image im WHERE coffeeSiteID=?1")
    UUID getImageIdForSiteId(UUID siteID);
    
    @Modifying // required by Hibernate, otherwise there is an Exception ' ... Illegal state ...'
    @Query("delete from Image im WHERE coffeeSiteID=?1")
    void deleteBySiteId(UUID coffeeSiteId);

    @Query("select COUNT(im) from Image im WHERE coffeeSiteID=?1")
    int getNumOfImagesForSiteId(UUID siteId);

    @Query("select COUNT(*) from Image")
    int getNumOfAllImagesForAllSites();
}
