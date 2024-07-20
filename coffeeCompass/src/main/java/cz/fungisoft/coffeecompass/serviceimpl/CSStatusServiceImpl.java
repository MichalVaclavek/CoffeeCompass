package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteStatusRepository;
import cz.fungisoft.coffeecompass.service.CSStatusService;

@Service("csStatusService")
@Transactional
public class CSStatusServiceImpl implements CSStatusService {

    private final CoffeeSiteStatusRepository csStatusRepo;
    
    @Autowired
    public CSStatusServiceImpl(CoffeeSiteStatusRepository csStatusRepo) {
        super();
        this.csStatusRepo = csStatusRepo;
    }

    @Override
    @Cacheable(cacheNames = "csStatusesCache")
    public CoffeeSiteStatus findCoffeeSiteStatusByName(String coffeeSiteStatus) {
        CoffeeSiteStatus csStatus = csStatusRepo.searchByName(coffeeSiteStatus);
        if (csStatus == null)
            throw new EntityNotFoundException("Coffee site status " + coffeeSiteStatus + " not found in DB.");
        return csStatus;
    }

    @Override
    @Cacheable(cacheNames = "csStatusesCache")
    public List<CoffeeSiteStatus> getAllCoffeeSiteStatuses() {
        return csStatusRepo.findAll();
    }
}
