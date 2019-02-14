package cz.fungisoft.coffeecompass.serviceimpl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.service.ImageResizerService;

/**
 * Service to change the size of the Image to "standard" width and lenghth.<br>
 * It also performs jpeg resampling using entered quality settings.<br>
 * This leads to compress from about 5 MB original image to about 300kB image size.<br>
 * <br>
 * Used before saving the Image into DB.
 * 
 * @author Michal Vaclavek, based on code taken from stackoverflow.com and other internet sites
 *
 */
@Service("imageResizer")
public class ImageResizerServiceImpl implements ImageResizerService
{
    /**
     * Default ratio 4/3
     */
    private int defWidth = 1280; // 640
    private int defHeight = 960; // 480
    
    private float defQuality = 0.8f;
    
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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

}
