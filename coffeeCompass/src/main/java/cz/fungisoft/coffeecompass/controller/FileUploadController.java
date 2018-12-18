/**
 * 
 */
package cz.fungisoft.coffeecompass.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.dto.UserDataDto;
import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.exception.StorageFileException;
import cz.fungisoft.coffeecompass.service.ImageFileStorageService;
import io.swagger.annotations.Api;

/**
 * Controller to handle operations concerning upload of CoffeeSite image files
 *  
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@Controller
public class FileUploadController
{
    private final ImageFileStorageService imageStorageService;

    @Autowired
    public FileUploadController(ImageFileStorageService storageService) {
        this.imageStorageService = storageService;
    }

    /**
     * To show name of the uploaded file
     * 
     * @param model
     * @return
     * @throws IOException
     */
    @GetMapping(value = {"/imageUpload", "/imageUpload/{siteId}"})
    public ModelAndView listUploadedFiles(final Image image, @RequestParam(defaultValue = "21") Integer siteID, ModelMap model) throws IOException {

        ModelAndView mav = new ModelAndView();
        
        if (model.containsAttribute("imageID")) {
            
            Integer imageID = (Integer) model.get("imageID");
            if (imageID != null) {
                mav.addObject("pic", imageStorageService.getImageAsBase64(imageID));
                mav.addObject("image", imageStorageService.getImageById(imageID));
            }
        } else
            mav.addObject("image", image);
            
        mav.setViewName("upload_file_form");
        return mav;
    }
    

    /**
     * To download image file ?
     * 
     * @param filename
     * @return
     */
    /*
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = imageStorageService.loadFileAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
    */
    /*
    @PostMapping("/imageUpload")
    public String handleFileUpload(@RequestParam("file") @Valid MultipartFile file, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
           result.rejectValue("fileName", "error.user.password.empty");
           return "upload_file_form"; 
        }
        
        String savedFileName = storageService.storeFile(file);
        
        redirectAttributes.addFlashAttribute("savedFileName", savedFileName);
        
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/imageUpload";
    }
    */
    
    /**
     * Serves upload image request to siteID.
     * 
     * @param image
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/imageUpload")
    public String handleFileUpload(@ModelAttribute("image") @Valid Image image, BindingResult result, RedirectAttributes redirectAttributes) {
    
        if (result.hasErrors()) {
           result.rejectValue("file", "error.image.empty");
           return "upload_file_form"; 
        }
        
        Integer imageID = imageStorageService.storeImageFile(image, image.getFile(), 21);
        
        redirectAttributes.addFlashAttribute("imageID", imageID);
        
        redirectAttributes.addFlashAttribute("savedFileName", image.getFile().getOriginalFilename());
        
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + image.getFileName() + "!");

        return "redirect:/imageUpload";
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare ze stranky zobrazujici komentare k jednomu CoffeeSitu.
     * Muze byt volano pouze ADMINEM (zarizeno v Thymeleaf View strance coffeesite_detail.html)
     * 
     * @param id of the Comment to delete
     * @return
     */
    @DeleteMapping("/deleteImage/{id}") 
    public ModelAndView deleteCommentAndStarsForSite(@PathVariable Integer id) {
        // Smazat komentar - need to have site Id to give it to /showSite Controller
        Integer siteId = imageStorageService.deleteSiteImageById(id);
        
        // Show same coffee site with updated Stars and comments
        ModelAndView mav = new ModelAndView("redirect:/imageUpload/?siteID=" + siteId);
        
        return mav;
    }

    /*
    @PostMapping("/uploadFile")
    public ModelAndView uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = storageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }
*/
    
    
}
