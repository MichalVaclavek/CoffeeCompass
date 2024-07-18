package cz.fungisoft.test.image2.serviceimpl;

import cz.fungisoft.test.image2.service.ImageFileStorageService;
import cz.fungisoft.test.image2.service.ImageRotationService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

/**
 * Service to rotate ImageFile
 * th.<br>
 * @author Michal Vaclavek, based on code taken from stackoverflow.com and other internet sources
 */
@Service("imageRotate")
@Slf4j
public class ImageRotationServiceImpl implements ImageRotationService {

    private final ImageFileStorageService imageFileStorageService;

    public ImageRotationServiceImpl(ImageFileStorageService imageFileStorageService) {
        this.imageFileStorageService = imageFileStorageService;
    }

    @Override
    public void rotate90DegreeLeft(String fileExtId) {
        log.info("Rotating left start ...");
        Instant start = Instant.now();
        Stream.of(ImageSizes.values())
              .forEach(size ->
                      imageFileStorageService.getImageFile(fileExtId, size)
                              .ifPresent(imgFile -> {
                                  BufferedImage rotatedImage;
                                  try {
                                      rotatedImage = rotate90DegreeLeft(imgFile);
                                      ImageIO.write(rotatedImage, "jpg", imgFile);
                                  } catch (IOException e) {
                                      log.error("Error during image rotating left.");
                                  }
                              })
              );
        Instant finish = Instant.now();
        log.info("Rotating left end in: {} ms", Duration.between(start, finish).toMillis());
    }

    @Override
    public void rotate90DegreeRight(String fileExtId) {
        log.info("Rotating right start ...");
        Instant start = Instant.now();
        Stream.of(ImageSizes.values())
              .forEach(size ->
                      imageFileStorageService.getImageFile(fileExtId, size)
                              .ifPresent(imgFile -> {
                                  BufferedImage rotatedImage;
                                  try {
                                      rotatedImage = rotate90DegreeRight(imgFile);
                                      ImageIO.write(rotatedImage, "jpg", imgFile);
                                  } catch (IOException e) {
                                      log.error("Error during image rotating right.");
                                  }
                              })
              );
        Instant finish = Instant.now();
        log.info("Rotating right end in: {} ms", Duration.between(start, finish).toMillis());
    }

    @Override
    public BufferedImage rotate90DegreeLeft(File imageFile) throws IOException {
        return rotate(imageFile, -90);
    }

    @Override
    public BufferedImage rotate90DegreeRight(File imageFile) throws IOException {
        return rotate(imageFile, 90);
    }

    private BufferedImage rotate(File imageFile, int angle) throws IOException {
        return Thumbnails.of(imageFile)
                .rotate(angle)
                .scale(1)
                .asBufferedImage();
    }
}
