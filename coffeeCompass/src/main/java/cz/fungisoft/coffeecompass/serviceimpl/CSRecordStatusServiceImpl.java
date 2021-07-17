package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRecordStatusRepository;
import cz.fungisoft.coffeecompass.service.CSRecordStatusService;

@Service("csRecordStatusService")
@Transactional
public class CSRecordStatusServiceImpl implements CSRecordStatusService {

    private CoffeeSiteRecordStatusRepository csRecordStatusRepo;
    
    @Autowired
    public CSRecordStatusServiceImpl(CoffeeSiteRecordStatusRepository csRecordStatusRepo) {
        super();
        this.csRecordStatusRepo = csRecordStatusRepo;
    }

    @Override
    public CoffeeSiteRecordStatus findCSRecordStatusByName(String coffeeSiteRecordStatus) {
        CoffeeSiteRecordStatus csRecStatus = csRecordStatusRepo.searchByName(coffeeSiteRecordStatus);
        if (csRecStatus == null)
            throw new EntityNotFoundException("Coffee site Record status name " + coffeeSiteRecordStatus + " not found in DB.");
        return csRecStatus;
    }

    @Override
    public List<CoffeeSiteRecordStatus> getAllCSRecordStatuses() {
        return csRecordStatusRepo.findAll();
    }

    @Override
    public CoffeeSiteRecordStatus findCSRecordStatus(CoffeeSiteRecordStatusEnum coffeeSiteRecordStatus) {
        String recordStatus = coffeeSiteRecordStatus.getSiteRecordStatus();
        return csRecordStatusRepo.searchByName(recordStatus);
    }
}
