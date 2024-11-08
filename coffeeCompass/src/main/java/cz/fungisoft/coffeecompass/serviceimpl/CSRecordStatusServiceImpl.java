package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteRecordStatusDTO;
import cz.fungisoft.coffeecompass.mappers.CoffeeSiteRecordStatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

    private final CoffeeSiteRecordStatusRepository csRecordStatusRepo;

    private final CoffeeSiteRecordStatusMapper csRecordStatusMapper;
    
    @Autowired
    public CSRecordStatusServiceImpl(CoffeeSiteRecordStatusRepository csRecordStatusRepo, CoffeeSiteRecordStatusMapper csRecordStatusMapper) {
        super();
        this.csRecordStatusRepo = csRecordStatusRepo;
        this.csRecordStatusMapper = csRecordStatusMapper;
    }

    @Override
    public CoffeeSiteRecordStatus findCSRecordStatusByName(String coffeeSiteRecordStatus) {
        CoffeeSiteRecordStatus csRecStatus = csRecordStatusRepo.searchByName(coffeeSiteRecordStatus);
        if (csRecStatus == null)
            throw new EntityNotFoundException("Coffee site Record status name " + coffeeSiteRecordStatus + " not found in DB.");
        return csRecStatus;
    }

    @Override
    @Cacheable(cacheNames = "csRecordStatusesCache")
    public List<CoffeeSiteRecordStatusDTO> getAllCSRecordStatuses() {
        return csRecordStatusRepo.findAll().stream().map(csRecordStatusMapper::csRecordStatusToCsRecordStatusDto).toList();
    }

    @Override
    @Cacheable(cacheNames = "csRecordStatusesCache")
    public CoffeeSiteRecordStatus findCSRecordStatus(CoffeeSiteRecordStatusEnum coffeeSiteRecordStatus) {
        String recordStatus = coffeeSiteRecordStatus.getSiteRecordStatus();
        return csRecordStatusRepo.searchByName(recordStatus);
    }
}
