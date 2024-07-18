package cz.fungisoft.test.image2.service;


import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Service to resize the uploaded ImageData of the CoffeeSite to standard size,
 * before saving into DB.
 * Also to or rotate image +-90 degrees, if required by user
 * 
 * @author Michal Václavek
 *
 */
public interface ImageResizeService {

    BufferedImage resizeAndCompressImage(MultipartFile inputImageFile) throws IOException;

    BufferedImage resizeAndCompressImage(MultipartFile inputImageFile, int width, int height) throws IOException;


    /**
     * Change the size of the ImageData by the ratio.
     * 
     * @param inputImageFile to be resized
     * @param sizeRatio number between 0.001 to 1000 used factor to multiplicate both sides of imageData
     * @return inserted imageData, resized by the ratio.
     * @throws IOException 
     */
    BufferedImage resizeAndCompressImage(MultipartFile inputImageFile, double sizeRatio) throws IOException;

    /**
     * Change the size of the ImageData by the ratio and change the jpeg quality compression
     * 
     * @param inputImageFile to be resized
     * @param sizeRatio - number between 0.001 to 1000 used factor to multiplicate both sides of imageData
     * @compressJpegRation - jpeg quality of compression, between 0.1 and 0.99
     * @return inserted imageData, resized by the ratio.
     * @throws IOException 
     */
    BufferedImage resizeAndCompressImage(MultipartFile inputImageFile, double sizeRatio, float compressJpegQuality) throws IOException;
}
