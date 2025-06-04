package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteStatusDTO;
import cz.fungisoft.coffeecompass.mappers.CoffeeSiteStatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteStatusRepository;
import cz.fungisoft.coffeecompass.service.CSStatusService;

@Service("csStatusService")
@Transactional
public class CSStatusServiceImpl implements CSStatusService {

    private final CoffeeSiteStatusRepository csStatusRepo;

    private final CoffeeSiteStatusMapper csStatusMapper;
    
    @Autowired
    public CSStatusServiceImpl(CoffeeSiteStatusRepository csStatusRepo, CoffeeSiteStatusMapper csStatusMapper) {
        super();
        this.csStatusRepo = csStatusRepo;
        this.csStatusMapper = csStatusMapper;
    }

    @Override
    @Cacheable(cacheNames = "csStatusesCache")
    public Optional<CoffeeSiteStatus> findCoffeeSiteStatusByName(String coffeeSiteStatus) {
        Optional<CoffeeSiteStatus> csStatus = csStatusRepo.searchByName(coffeeSiteStatus);
//        return csStatus.orElseThrow(() -> new EntityNotFoundException("Coffee site status " + coffeeSiteStatus + " not found in DB."));
        return csStatus;
    }

    @Override
    public Optional<CoffeeSiteStatus> findCoffeeSiteStatusById(UUID uuid) {
        return csStatusRepo.findById(uuid);
    }

    @Override
    @Cacheable(cacheNames = "csStatusesCache")
    public List<CoffeeSiteStatusDTO> getAllCoffeeSiteStatuses() {
        return csStatusRepo.findAll().stream().map(csStatusMapper::csStatusToCsStatusDto).toList();
    }
}
