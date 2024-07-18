package cz.fungisoft.test.image2.serviceimpl;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

/**
 * Thread safe class to jpeg compress BufferedImage
 */
@Slf4j
public class Compressor {

    synchronized BufferedImage compressImage(BufferedImage image, float compressionQuality) throws IOException {

        if (compressionQuality == 1f) {
            return image;
        }

        // Create a temporary FileImageOutputStream to hold the compressed image data
        File tempFile = File.createTempFile("compressed_image", ".jpg");
        FileImageOutputStream output = new FileImageOutputStream(tempFile);

        // Create an ImageWriter for the JPEG format
        ImageWriter imageWriter = getImageWriter();
        // Write the compressed image data to the temporary file
        imageWriter.setOutput(output);

        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(Locale.getDefault());
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(compressionQuality); // quality compression

        imageWriter.write(null, new IIOImage(removeAlphaChannel(image), null, null), jpegParams);

        // Clean up resources
        output.flush();
        output.close();
        imageWriter.dispose();

        // Read the compressed image data from the temporary file
        BufferedImage compressedImage = ImageIO.read(tempFile);

        // Delete the temporary file
        tempFile.delete();
        return compressedImage;
    }

    private static BufferedImage removeAlphaChannel(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) {
            return img;
        }

        BufferedImage target = createImage(img.getWidth(), img.getHeight());
        Graphics2D g = target.createGraphics();
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return target;
    }

    private static BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    private ImageWriter getImageWriter() {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (writers.hasNext()) {
            return writers.next();
        }
        throw new RuntimeException("No JPG writer found!");
    }
}
