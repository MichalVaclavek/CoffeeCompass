package cz.fungisoft.test.image2.serviceimpl;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;


/**
 * Thread safe Class ty resize image using Thumbnailator library
 */
public class Resizer {

    public synchronized BufferedImage resizeImage(MultipartFile inputImageFile, int width, int height) throws IOException {
        return Thumbnails.of(inputImageFile.getInputStream())
                         .size(width, height)
                         .asBufferedImage();
    }

    public synchronized BufferedImage resizeImage(MultipartFile inputImageFile, double ratio) throws IOException {
        return Thumbnails.of(inputImageFile.getInputStream())
                         .scale(ratio)
                         .asBufferedImage();
    }
}
