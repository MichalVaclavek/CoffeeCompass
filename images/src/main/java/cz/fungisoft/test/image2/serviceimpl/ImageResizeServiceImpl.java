package cz.fungisoft.test.image2.serviceimpl;

import cz.fungisoft.test.image2.service.ImageResizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Service to change the size of the ImageData to default width and length
 * th.<br>
 * It also performs jpeg resampling using entered quality settings.<br>
 * This leads to compress from about 6 MB original image to about 500 kB image size in average.<br>
 *
 * @author Michal Vaclavek, based on code taken from stackoverflow.com and other internet sources
 */
@Service("imageResize")
@Slf4j
public class ImageResizeServiceImpl implements ImageResizeService {

    /**
     * Max allowed Width and Height of the images
     * Default ratio is fullHD 1920 x 1080 for landscape or 1080 x 1920 for portrait
     */
    private static final int DEF_MAX_WIDTH_HEIGHT = 1920; // 640 or 800 or 1024 or 1920 (full HD)

    private static final float defQuality = 0.9f;

    private boolean isOverMaxSizeLandscape(BufferedImage image) {
        return heightToWidthRatio(image) <= 1 && image.getWidth() > DEF_MAX_WIDTH_HEIGHT; // for landscape or.
    }

    private boolean isOverMaxSizePortrait(BufferedImage image) {
        return heightToWidthRatio(image) >= 1 && image.getHeight() > DEF_MAX_WIDTH_HEIGHT; // for portrait or.
    }

    private float heightToWidthRatio(BufferedImage image) {
        return image.getHeight() * 1f / image.getWidth();
    }


    /**
     * Default resize to keep max. Width and Height
     *
     * @param inputImageFile
     * @return
     * @throws IOException
     */
    @Override
    public BufferedImage resizeAndCompressImage(MultipartFile inputImageFile) throws IOException {
        // Read the input image from the MultipartFile
        BufferedImage originalImage = ImageIO.read(inputImageFile.getInputStream());
        double resizeRatio = 1f;
        if (isOverMaxSizeLandscape(originalImage)) {
            resizeRatio = (DEF_MAX_WIDTH_HEIGHT * 1d) / originalImage.getWidth();
        }
        if (isOverMaxSizePortrait(originalImage)) {
            resizeRatio = (DEF_MAX_WIDTH_HEIGHT * 1d) / originalImage.getHeight();
        }

        return resizeAndCompressImage(inputImageFile, Math.floor(resizeRatio * 10000) / 10000.0);
    }

    @Override
    public BufferedImage resizeAndCompressImage(MultipartFile inputImageFile, double sizeRatio, float compressJpegQuality) throws IOException {
        ResizerWithCompressor resizer = new ResizerWithCompressor();
        return resizer.resizeAndCompressImage(inputImageFile, sizeRatio, compressJpegQuality);
    }

    @Override
    public BufferedImage resizeAndCompressImage(MultipartFile inputImageFile, double sizeRatio) throws IOException {
        ResizerWithCompressor resizer = new ResizerWithCompressor();
        return resizer.resizeAndCompressImage(inputImageFile, sizeRatio, defQuality);
    }

    /**
     * Basic method to perform resizing to given width and height
     *
     * @param inputImageFile
     * @param width
     * @param height
     * @return
     * @throws IOException
     */
    @Override
    public BufferedImage resizeAndCompressImage(MultipartFile inputImageFile, int width, int height) throws IOException {
        ResizerWithCompressor resizer = new ResizerWithCompressor();
        return resizer.resizeAndCompressImage(inputImageFile, width, height, defQuality);
    }
}
