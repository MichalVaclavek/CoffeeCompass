/**
 * 
 */
package cz.fungisoft.coffeecompass.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import cz.fungisoft.coffeecompass.configuration.FileStorageProperties;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.exception.MyFileNotFoundException;
import cz.fungisoft.coffeecompass.exception.StorageFileException;
import cz.fungisoft.coffeecompass.repository.ImageRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.ImageFileStorageService;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;

import javax.transaction.Transactional;

/**
 * @author Michal Vaclavek
 *
 */
@Service("imageFileStorageService")
@Log4j2
public class ImageFileStorageServiceImpl implements ImageFileStorageService
{
    private final Path fileStorageLocation;
    
    private ImageRepository imageRepo;
    
    private CoffeeSiteService coffeeSiteService;
    
    @Autowired
    public ImageFileStorageServiceImpl(FileStorageProperties fileStorageProperties, ImageRepository imageRepo, CoffeeSiteService coffeeSiteService) {
        
        this.imageRepo = imageRepo;
        this.coffeeSiteService = coffeeSiteService;
        
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new StorageFileException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /*
    @Override
    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        log.info("File saved: " + fileName);

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new StorageFileException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new StorageFileException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
    */
    
    @Override
    @Transactional
    public Integer storeImageFile(Image image, MultipartFile file, Long siteID) {
        
        Integer retVal = 0;
        CoffeeSite cs = coffeeSiteService.findOneById(siteID);
        if (cs != null) {
            try {
                // Check if there is already image assigned to the CS. If yes, delete the old image first
                if (cs.getImage() != null) {
                    Image oldImage = cs.getImage();
                    imageRepo.delete(oldImage);
                }
                image.setImageBytes(file.getBytes());
                image.setFile(file);
                image.setCoffeeSite(cs);
                image.setSavedOn(new Timestamp(new Date().getTime()));
            } catch (IOException e) {
    
            }
            imageRepo.save(image);
            cs.setImage(image);
            retVal = image.getId();
        } 
        
        return retVal;
    }
    
    @Override
    @Transactional
    public Image getImageById(Integer imageID) {
        return imageRepo.getOne(imageID);
    }

    /**
     * 
     */
    /*
    @Override
    public Resource loadFileAsResource(String fileName) {
        
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
    */

    /*
    @Override
    public Path loadFile(String filename) {
        return fileStorageLocation.resolve(filename);
    }
*/
    /*
    @Override
    public void deleteFile(String fileName) {
        
        try {
            Files.delete(fileStorageLocation.resolve(fileName));
        } catch (IOException ex) {
            throw new MyFileNotFoundException("File not found for delete " + fileName, ex);
        }
    }
*/
    /*
    @Override
    public Resource getImageAsResource(Integer imageID) {
        // TODO Auto-generated method stub
        return null;
    }
*/
    @Override
    public String getImageAsBase64(Integer imageID) {
        
        Image imFromDB = getImageById(imageID);
        return convertImageToBase64(imFromDB);
    }
    
    private String convertImageToBase64(Image image) {
        
        StringBuilder imageString = new StringBuilder();
        
        if (image != null) {
            imageString.append("data:image/png;base64,");
            imageString.append(Base64.getEncoder().encodeToString(image.getImageBytes())); //bytes will be image byte[] come from DB 
        }
        return imageString.toString();
    }

    /**
     * @return id of the CoffeeSites this image beloned to before deletition
     */
    @Transactional
    @Override
    public Integer deleteSiteImageById(Integer id) {
        Integer siteId = imageRepo.getSiteIdForImage(id);
        imageRepo.deleteById(id);
        return siteId;
    }
    
    @Transactional
    @Override
    public Image getImageForSiteId(Long siteId) {
        
        return imageRepo.getImageForSite(siteId);
    }

    @Override
    public String getImageAsBase64ForSiteId(Long siteID) {
        return convertImageToBase64(coffeeSiteService.findOneById(siteID).getImage());
    }
       
}
