package cz.fungisoft.test.image2.serviceimpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Thread safe class to resize and compress MultipartFile in one run
 */
@Slf4j
public class ResizerWithCompressor {

    private static final int DEF_MAX_WIDTH = 1920; // 640 or 800 or 1024 or 1920 (full HD)
    private static final int DEF_MAX_HEIGHT = 1080;

    private static final float DEF_QUALITY = 0.9f;

    private final Object lock = new Object();

    private final Resizer resizer = new Resizer();

    private final Compressor compressor = new Compressor();


    BufferedImage resizeAndCompressImage(MultipartFile inputImageFile, double sizeRatio, float compressQuality) throws IOException {
        synchronized (lock) {
            if (compressQuality < 0.1f || compressQuality > 0.99f) {
                compressQuality = DEF_QUALITY;
            }
            if (sizeRatio < 0.001 || sizeRatio > 1000) {
                sizeRatio = 1;
            }
            BufferedImage resizedImage = resizer.resizeImage(inputImageFile, sizeRatio);
            return compressor.compressImage(resizedImage, compressQuality);
        }
    }

    BufferedImage resizeAndCompressImage(MultipartFile inputImageFile, int width, int height, float compressQuality) throws IOException {
        synchronized (lock) {
            if (compressQuality < 0.1f || compressQuality > 0.99f) {
                compressQuality = DEF_QUALITY;
            }
            if (width > DEF_MAX_WIDTH) {
                width = DEF_MAX_WIDTH;
            }
            if (height > DEF_MAX_HEIGHT) {
                height = DEF_MAX_HEIGHT;
            }
            BufferedImage resizedImage = resizer.resizeImage(inputImageFile, width, height);
            return compressor.compressImage(resizedImage, compressQuality);
        }
    }
}
