package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Integer>
{
    @Query("select coffeeSite.id FROM Image im where id=?1")
    public Integer getSiteIdForImage(Integer commentId);
    
    @Query("select im from Image im WHERE coffeeSite.id=?1")
    public Image getImageForSite(Long coffeeSiteID);
}