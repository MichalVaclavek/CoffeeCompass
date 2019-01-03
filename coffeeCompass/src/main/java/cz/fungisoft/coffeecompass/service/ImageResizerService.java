package cz.fungisoft.coffeecompass.service;

import cz.fungisoft.coffeecompass.entity.Image;

/**
 * Service to resize the uploaded Image of the CoffeeSite to standard size,
 * before saving into DB.
 * 
 * @author Michal Václavek
 *
 */
public interface ImageResizerService
{
    /**
     * Sets the default image size. All images are to be resized to this Width and Height.
     * 
     * @param defWidth image width in pixels
     * @param defHeight image width in pixels
     */
    public void setDefaultSize(int defWidth, int defHeight);
    
    /**
     * Change the size of the Image to default size and return it.
     * 
     * @param image to be resized
     * @return inserted image, resized to default size.
     */
    public Image resize(Image image);
    
    /**
     * Change the size of the Image by the ratio.
     * 
     * @param image to be resized
     * @param ratio number between 0.001 to 1000 used factor to multiplicate both sides of image 
     * @return inserted image, resized by the ratio.
     */
    public Image resize(Image image, double ratio);
    
}
