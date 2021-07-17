package cz.fungisoft.coffeecompass.service;

/**
 * Service methods for estimate size of data, which can be downloaded by user,
 * when using 'Offline mode' functionality.
 */
public interface DataDownloadSizeService {

    /**
     * Number of all CoffeeSites, which will be downloaded when requested by user using Offline mode.
     * @return
     */
    long getNumberOfCoffeeSitesToDownload();

    /**
     * Size in KBytes of all CoffeeSites, which will be downloaded when requested by user using Offline mode.
     * @return
     */
    long getKBytesOfCoffeeSitesToDownload();

    /**
     * Number of all Comments, which will be downloaded when requested by user using Offline mode.
     * @return
     */
    long getNumberOfCommentsToDownload();

    /**
     * Size in KBytes of all Comments, which will be downloaded when requested by user using Offline mode.
     * @return
     */
    long getKBytesOfCommentsToDownload();

    /**
     * Number of all Images, which will be downloaded when requested by user using Offline mode.
     * @return
     */
    long getNumberOfImagesToDownload();

    /**
     * Size in KBytes of all Images, which will be downloaded when requested by user using Offline mode.
     * @return
     */
    long getKBytesOfImagesToDownload();

    /**
     * Size in KBytes of all data to download, i.e. in current implementations of all CoffeeSites, Comments and Images
     * @return
     */
    long getKBytesOfAllDataToDownload();

    /**
     * Size in KBytes of all data to download except Images.
     * @return
     */
    long getKBytesOfAllDataWithoutImagesToDownload();
}
