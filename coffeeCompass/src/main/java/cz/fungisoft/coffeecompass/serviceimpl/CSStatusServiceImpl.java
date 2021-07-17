package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteStatusRepository;
import cz.fungisoft.coffeecompass.service.CSStatusService;

@Service("csStatusService")
@Transactional
public class CSStatusServiceImpl implements CSStatusService {

    private CoffeeSiteStatusRepository csStatusRepo;
    
    @Autowired
    public CSStatusServiceImpl(CoffeeSiteStatusRepository csStatusRepo) {
        super();
        this.csStatusRepo = csStatusRepo;
    }

    @Override
    public CoffeeSiteStatus findCoffeeSiteStatusByName(String coffeeSiteStatus) {
        CoffeeSiteStatus csStatus = csStatusRepo.searchByName(coffeeSiteStatus);
        if (csStatus == null)
            throw new EntityNotFoundException("Coffee site status " + coffeeSiteStatus + " not found in DB.");
        return csStatus;
    }

    @Override
    public List<CoffeeSiteStatus> getAllCoffeeSiteStatuses() {
        return csStatusRepo.findAll();
    }
}
