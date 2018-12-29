package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.Image;

/**
 * JPA operations with Image entity.
 * 
 * @author Michal Vaclavek
 *
 */
public interface ImageRepository extends JpaRepository<Image, Integer>
{
    @Query("select coffeeSite.id FROM Image im where id=?1")
    public Long getSiteIdForImage(Integer imageId);
    
    @Query("select im from Image im WHERE coffeeSite.id=?1")
    public Image getImageForSite(Long coffeeSiteID);
    
    @Query("select im.id from Image im WHERE coffeeSite.id=?1")
    public Integer getImageIdForSiteId(Long siteID);
    
    @Query("delete from Image im WHERE coffeeSite.id=?1")
    public void deleteBySiteId(Long coffeeSiteId);
    
}
