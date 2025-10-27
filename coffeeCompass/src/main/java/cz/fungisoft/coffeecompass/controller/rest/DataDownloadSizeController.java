package cz.fungisoft.coffeecompass.controller.rest;

import cz.fungisoft.coffeecompass.service.DataDownloadSizeService;
import cz.fungisoft.coffeecompass.serviceimpl.images.ImagesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle requests about size of data to be downloaded for 'Offline mode'
 */
@Tag(name = "DownloadSize", description = "Get data size for download")
@RestController
@RequiredArgsConstructor
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/dataDownloadSize")
public class DataDownloadSizeController {

    @NonNull
    private final DataDownloadSizeService dataDownloadSizeService;

    @NonNull
    private final ImagesService imagesService;

    /**
     * @return number of CoffeeSites to download.
     */
    @GetMapping("/coffeeSites/number")
    @ResponseStatus(HttpStatus.OK)
    public Long getNumberOfCoffeeSitesToDownload() {
        return dataDownloadSizeService.getNumberOfCoffeeSitesToDownload();
    }

    /**
     * @return size of CoffeeSites to download in kB
     */
    @GetMapping("/coffeeSites/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public Long getSizeOfCoffeeSitesToDownload() {
        return dataDownloadSizeService.getKBytesOfCoffeeSitesToDownload();
    }

    /**
     * @return number of Comments to download.
     */
    @GetMapping("/comments/number")
    @ResponseStatus(HttpStatus.OK)
    public Long getNumberOfCommentsToDownload() {
        return dataDownloadSizeService.getNumberOfCommentsToDownload();
    }

    /**
     * @return size of Comments to download in kB
     */
    @GetMapping("/comments/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public Long getSizeOfCommentsToDownload() {
        return dataDownloadSizeService.getKBytesOfCommentsToDownload();
    }

    /**
     * @return number of Images to download.
     */
//    @GetMapping("/images/number")
//    @ResponseStatus(HttpStatus.OK)
//    public Long getNumberOfImagesToDownload() {
//        return dataDownloadSizeService.getNumberOfImagesToDownload();
//    }

    /**
     * @return size of CoffeeSites to download in kB
     */
//    @GetMapping("/images/sizeKB")
//    @ResponseStatus(HttpStatus.OK)
//    public Long getSizeOfImagesToDownload() {
//        return dataDownloadSizeService.getKBytesOfImagesToDownload();
//    }

    @GetMapping("/object/sizeKB/{siteExtId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Long> getImageObjectSizeOfImages(@PathVariable String siteExtId) {
        Long sizeKB = imagesService.getImageObjectSizeOfImages(siteExtId);
        return ResponseEntity.ok(sizeKB);
    }

    @GetMapping("/object/number/{siteExtId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Long> getImageObjectNumberOfImages(@PathVariable String siteExtId) {
        Long numberOfImages = imagesService.getImageObjectNumberOfImages(siteExtId);
        return ResponseEntity.ok(numberOfImages);
    }

    @GetMapping("/images/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Long> getSizeOfAllImagesToDownload(@RequestParam(required = false) String imageSize) {
        Long sizeKB = imagesService.getSizeOfAllImagesToDownload(imageSize) + dataDownloadSizeService.getKBytesOfImagesToDownload();
        return ResponseEntity.ok(sizeKB);
    }

    @GetMapping("/images/number")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Long> getNumberOfAllImagesToDownload(@RequestParam(required = false) String imageSize) {
        Long numberOfImages = imagesService.getNumberOfAllImagesToDownload(imageSize) + dataDownloadSizeService.getNumberOfImagesToDownload();
        return ResponseEntity.ok(numberOfImages);
    }

    /**
     * @return size of all entities to download, i.e. size of all CoffeeSites, Comments and Images
     */
    @GetMapping("/all/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public Long getSizeOfAllToDownload() {
        return dataDownloadSizeService.getKBytesOfAllDataToDownload();
    }

    /**
     * @return size of all entities to download except Images
     */
    @GetMapping("/allExceptImages/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public Long getSizeOfAllExceptImagesToDownload() {
        return dataDownloadSizeService.getKBytesOfAllDataWithoutImagesToDownload();
    }
}
