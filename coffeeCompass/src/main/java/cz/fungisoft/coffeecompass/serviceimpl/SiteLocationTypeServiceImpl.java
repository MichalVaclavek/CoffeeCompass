package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.SiteLocationTypeRepository;
import cz.fungisoft.coffeecompass.service.SiteLocationTypeService;

@Service("siteLocationTypeService")
@Transactional
public class SiteLocationTypeServiceImpl implements SiteLocationTypeService {

    private final SiteLocationTypeRepository siteLocTypeRepo;
    
    @Autowired
    public SiteLocationTypeServiceImpl(SiteLocationTypeRepository siteLocTypeRepo) {
        super();
        this.siteLocTypeRepo = siteLocTypeRepo;
    }

    @Override
    @Cacheable(cacheNames = "siteLocationTypesCache")
    public SiteLocationType findSiteLocationType(String siteLocationTypeName) {
        
        SiteLocationType locType = siteLocTypeRepo.searchByName(siteLocationTypeName);
        if (locType == null)
            throw new EntityNotFoundException("Location type " + siteLocationTypeName + " not found in DB.");
        return locType;
    }

    @Override
    @Cacheable(cacheNames = "siteLocationTypesCache")
    public List<SiteLocationType> getAllSiteLocationTypes() {
        return siteLocTypeRepo.findAll();
    }
}
