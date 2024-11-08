package cz.fungisoft.coffeecompass.controller.rest;

import cz.fungisoft.coffeecompass.service.DataDownloadSizeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle requests about size of data to be downloaded for 'Offline mode'
 */
@Tag(name = "DownloadSize", description = "Get data size for download")
@RestController
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/dataDownloadSize")
public class DataDownloadSizeController {

    @NonNull
    private final DataDownloadSizeService dataDownloadSizeService;

    public DataDownloadSizeController(@NonNull DataDownloadSizeService dataDownloadSizeService) {
        this.dataDownloadSizeService = dataDownloadSizeService;
    }

    /**
     * Returns number of CoffeeSites to download.
     *
     * @return
     */
    @GetMapping("/coffeeSites/number")
    @ResponseStatus(HttpStatus.OK)
    public Long getNumberOfCoffeeSitesToDownload() {
        return dataDownloadSizeService.getNumberOfCoffeeSitesToDownload();
    }

    /**
     * Returns size of CoffeeSites to download in kB
     * @return
     */
    @GetMapping("/coffeeSites/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public Long getSizeOfCoffeeSitesToDownload() {
        return dataDownloadSizeService.getKBytesOfCoffeeSitesToDownload();
    }

    /**
     * Returns number of CoffeeSites to download.
     *
     * @return
     */
    @GetMapping("/comments/number")
    @ResponseStatus(HttpStatus.OK)
    public Long getNumberOfCommentsToDownload() {
        return dataDownloadSizeService.getNumberOfCommentsToDownload();
    }

    /**
     * Returns size of CoffeeSites to download in kB
     * @return
     */
    @GetMapping("/comments/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public Long getSizeOfCommentsToDownload() {
        return dataDownloadSizeService.getKBytesOfCommentsToDownload();
    }

    /**
     * Returns number of CoffeeSites to download.
     *
     * @return
     */
    @GetMapping("/images/number")
    @ResponseStatus(HttpStatus.OK)
    public Long getNumberOfImagesToDownload() {
        return dataDownloadSizeService.getNumberOfImagesToDownload();
    }

    /**
     * Returns size of CoffeeSites to download in kB
     * @return
     */
    @GetMapping("/images/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public Long getSizeOfImagesToDownload() {
        return dataDownloadSizeService.getKBytesOfImagesToDownload();
    }

    /**
     * Returns size of all entities to download, i.e. size of all CoffeeSites, Comments and Images
     * @return
     */
    @GetMapping("/all/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public Long getSizeOfAllToDownload() {
        return dataDownloadSizeService.getKBytesOfAllDataToDownload();
    }

    /**
     * Returns size of all entities to download except Images
     * @return
     */
    @GetMapping("/allExceptImages/sizeKB")
    @ResponseStatus(HttpStatus.OK)
    public Long getSizeOfAllExceptImagesToDownload() {
        return dataDownloadSizeService.getKBytesOfAllDataWithoutImagesToDownload();
    }
}
