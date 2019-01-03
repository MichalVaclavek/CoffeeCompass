package cz.fungisoft.coffeecompass.serviceimpl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.service.ImageResizerService;

/**
 * Service to change the size of the Image to "standard" width and lenghth.
 * Used before saving the Image into DB.
 * 
 * @author Michal Vaclavek, based on code taken from stackoverflow.com
 *
 */
@Service("imageResizer")
public class ImageResizerServiceImpl implements ImageResizerService
{
    /**
     * Default ratio 4/3
     */
    private int defWidth = 640; // 640
    
    private int defHeight = 480; // 480
    
    @Override
    public void setDefaultSize(int defWidth, int defHeight) {
        this.defWidth = defWidth;
        this.defHeight = defHeight;
    }

    @Override
    public Image resize(Image image) {
        
        BufferedImage inputImage;
        ByteArrayInputStream bais = new ByteArrayInputStream(image.getImageBytes());
        try {
            inputImage = ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
 
        // creates output image
        BufferedImage resizedImage = new BufferedImage(defWidth,
                defHeight, inputImage.getType());
 
        // scales the input image to the output image
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, defWidth, defHeight, null);
        g2d.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(resizedImage, "png", baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        image.setImageBytes(baos.toByteArray());
        return image;
    }

    @Override
    public Image resize(Image image, double ratio) {
        
        BufferedImage inputImage;
        ByteArrayInputStream bais = new ByteArrayInputStream(image.getImageBytes());
        try {
            inputImage = ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.defWidth = (int) (inputImage.getWidth() * ratio);
        this.defHeight = (int) (inputImage.getHeight() * ratio);
        
        return resize(image);
    }

}
