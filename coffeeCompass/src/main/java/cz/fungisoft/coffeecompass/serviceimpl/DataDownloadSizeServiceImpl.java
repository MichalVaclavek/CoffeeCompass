package cz.fungisoft.coffeecompass.serviceimpl;

import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.CommentRepository;
import cz.fungisoft.coffeecompass.repository.ImageRepository;
import cz.fungisoft.coffeecompass.service.DataDownloadSizeService;
import org.springframework.stereotype.Service;

@Service("dataDownloadSizeService")
public class DataDownloadSizeServiceImpl implements DataDownloadSizeService {

    private final CoffeeSiteRepository coffeeSiteRepo;

    private final CommentRepository commentsRepo;

    private final ImageRepository imageRepo;


    public DataDownloadSizeServiceImpl(CoffeeSiteRepository coffeeSiteRepo, CommentRepository commentsRepo, ImageRepository imageRepo) {
        this.coffeeSiteRepo = coffeeSiteRepo;
        this.commentsRepo = commentsRepo;
        this.imageRepo = imageRepo;
    }

    @Override
    public long getNumberOfCoffeeSitesToDownload() {
        return coffeeSiteRepo.getNumOfAllActiveSites();
    }

    /**
     * Estimated size of one CoffeeSite sent over JSON is 1.5 kB (according Postman)
     * @return
     */
    @Override
    public long getKBytesOfCoffeeSitesToDownload() {
        return (long) (getNumberOfCoffeeSitesToDownload() * 1.5);
    }

    @Override
    public long getNumberOfCommentsToDownload() {
        return commentsRepo.getNumberOfAllComments();
    }

    /**
     * Estimated size of one Comment sent over JSON is 0.3 kB (according Postman)
     * @return
     */
    @Override
    public long getKBytesOfCommentsToDownload() {
        return (long) (getNumberOfCommentsToDownload() * 0.3);
    }

    @Override
    public long getNumberOfImagesToDownload() {
        return imageRepo.getNumOfAllImagesForAllSites();
    }

    /**
     * @return average size of one image sent over JSON is 250 kB (according Postman)
     */
    @Override
    public long getKBytesOfImagesToDownload() {
        return getNumberOfImagesToDownload() * 250;
    }

    @Override
    public long getKBytesOfAllDataToDownload() {
        return getKBytesOfImagesToDownload() + getKBytesOfCommentsToDownload() + getKBytesOfCoffeeSitesToDownload();
    }

    @Override
    public long getKBytesOfAllDataWithoutImagesToDownload() {
        return getKBytesOfCommentsToDownload() + getKBytesOfCoffeeSitesToDownload();
    }
}
