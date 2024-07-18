package cz.fungisoft.test.image2.service;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Service to rotate image +-90 degrees, if required by user
 * 
 * @author Michal Václavek
 *
 */
public interface ImageRotationService {

    void rotate90DegreeLeft(String fileExtId);

    void rotate90DegreeRight(String fileExtId);
    
    /**
     * Rotates imageData 90 degrees left, i.e. counterclockwise
     * 
     * @param imageFile
     * @return
     */
    BufferedImage rotate90DegreeLeft(File imageFile) throws IOException;
    
    /**
     * Rotates imageData 90 degrees left, i.e. clockwise
     * 
     * @param imageFile
     * @return
     */
    BufferedImage rotate90DegreeRight(File imageFile) throws IOException;

}
