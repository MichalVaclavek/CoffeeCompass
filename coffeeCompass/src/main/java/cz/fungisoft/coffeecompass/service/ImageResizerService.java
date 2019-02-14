package cz.fungisoft.coffeecompass.service;

import java.io.IOException;

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
     * @throws IOException 
     */
    public Image resize(Image image) throws IOException;
    
    /**
     * Change the size of the Image by the ratio.
     * 
     * @param image to be resized
     * @param sizeRatio number between 0.001 to 1000 used factor to multiplicate both sides of image 
     * @return inserted image, resized by the ratio.
     * @throws IOException 
     */
    public Image resize(Image image, double sizeRatio) throws IOException;
    
    /**
     * Change the size of the Image by the ratio and change the jpeg quality compression
     * 
     * @param image to be resized
     * @param sizeRatio - number between 0.001 to 1000 used factor to multiplicate both sides of image 
     * @compressJpegRation - jpeg quality of compression, between 0.1 and 0.99
     * @return inserted image, resized by the ratio.
     * @throws IOException 
     */
    public Image resize(Image image, double sizeRatio, float compressJpegQuality) throws IOException;
    
    
    
}
