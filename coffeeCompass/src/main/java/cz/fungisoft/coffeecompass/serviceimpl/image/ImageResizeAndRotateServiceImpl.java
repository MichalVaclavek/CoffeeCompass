package cz.fungisoft.coffeecompass.serviceimpl.image;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.service.image.ImageResizeAndRotateService;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

/**
 * Service to change the size of the Image to "standard" width and lenghth.<br>
 * It also performs jpeg resampling using entered quality settings.<br>
 * This leads to compress from about 6 MB original image to about 500 kB image size in average.<br>
 * <br>
 * Used before saving the Image into DB.
 * 
 * @author Michal Vaclavek, based on code taken from stackoverflow.com and other internet sites
 *
 */
@Service("imageResizeAndRotate")
@Slf4j
public class ImageResizeAndRotateServiceImpl implements ImageResizeAndRotateService {

    /**
     * Default ratio 4/3
     */
    private int defWidth = 1024; // 640 or 800 or 1024
    private int defHeight = 768; // 480 or 600 or 768
    
    private float defQuality = 0.85f;
    
    @Override
    public void setDefaultSize(int defWidth, int defHeight) {
        this.defWidth = defWidth;
        this.defHeight = defHeight;
    }

    @Override
    public Image resize(Image image) throws IOException {
        
        BufferedImage inputImage;
        ByteArrayInputStream bais = new ByteArrayInputStream(image.getImageBytes());
        try {
            inputImage = ImageIO.read(bais);
        } catch (IOException e) {
            log.error("Cannot read image file. Name '{}'", image.getFileName());
            throw e;
        }
 
        BufferedImage resizedImage = new BufferedImage(defWidth, defHeight, inputImage.getType());
        
        // scales the input image to the output image
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, defWidth, defHeight, null);
        g2d.dispose();
        
        // creates output Stream for compression and for getting byte[] of image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            // Change jpeg compress ratio
            ImageWriter imgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageOutputStream ioStream = ImageIO.createImageOutputStream(baos);
            imgWriter.setOutput(ioStream);
    
            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(Locale.getDefault());
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(defQuality); // quality compression 
    
            imgWriter.write(null, new IIOImage(resizedImage, null, null), jpegParams);
    
            ioStream.flush();
            ioStream.close();
            imgWriter.dispose();
            
        } catch (IOException e) {
            log.error("Cannot resize image file. Name '{}'", image.getFileName());
            throw e;
        }
        
        image.setImageBytes(baos.toByteArray());
        return image;
    }
    

    @Override
    public Image resize(Image image, double sizeRatio) throws IOException {
        
        BufferedImage inputImage;
        ByteArrayInputStream bais = new ByteArrayInputStream(image.getImageBytes());
        try {
            inputImage = ImageIO.read(bais);
        } catch (IOException e) {
            log.error("Cannot resize image file. Name '{}'", image.getFileName());
            throw e;
        }
        if (sizeRatio >= 0.001 && sizeRatio <= 1000) {
            this.defWidth = (int) (inputImage.getWidth() * sizeRatio);
            this.defHeight = (int) (inputImage.getHeight() * sizeRatio);
        }
        
        return resize(image);
    }

    @Override
    public Image resize(Image image, double sizeRatio, float compressJpegQuality) throws IOException {
        
        if (compressJpegQuality >= 0.1f && compressJpegQuality <= 0.99f) {
            this.defQuality = compressJpegQuality;
        }
        
        return resize(image, sizeRatio);
    }

    private Image rotate(Image image, int angle) {
        
        BufferedImage inputImage;
        ByteArrayInputStream bais = new ByteArrayInputStream(image.getImageBytes());
        try {
            inputImage = ImageIO.read(bais);
        } catch (IOException e) {
            log.error("Cannot read image file to rotate. File name '{}'", image.getFileName());
            throw new RuntimeException(e);
        }
        
        
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = this.defWidth;
        int h = this.defHeight;
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);
 
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, inputImage.getType());
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(inputImage, 0, 0, this.defWidth, this.defHeight, null);
        g2d.dispose();

        // creates output Stream for getting byte[] of image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            // Save as jpeg
            ImageWriter imgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageOutputStream ioStream = ImageIO.createImageOutputStream(baos);
            imgWriter.setOutput(ioStream);

            imgWriter.write(rotated);
    
            ioStream.flush();
            ioStream.close();
            imgWriter.dispose();

        } catch (IOException e) {
            log.error("Failed to write image file after its rotation. File name '{}'", image.getFileName());
            throw new RuntimeException(e);
        }
        
        image.setImageBytes(baos.toByteArray());
        return image;
    }

    @Override
    public Image rotate90DegreeLeft(Image image) {
        return rotate(image, -90);
    }

    @Override
    public Image rotate90DegreeRight(Image image) {
        return rotate(image, 90);
    }
}
