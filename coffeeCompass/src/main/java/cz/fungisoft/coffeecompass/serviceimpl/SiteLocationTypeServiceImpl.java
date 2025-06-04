package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.SiteLocationTypeDTO;
import cz.fungisoft.coffeecompass.mappers.SiteLocationTypeMapper;
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

    private final SiteLocationTypeMapper siteLocationTypeMapper;
    
    @Autowired
    public SiteLocationTypeServiceImpl(SiteLocationTypeRepository siteLocTypeRepo, SiteLocationTypeMapper siteLocationTypeMapper) {
        super();
        this.siteLocTypeRepo = siteLocTypeRepo;
        this.siteLocationTypeMapper = siteLocationTypeMapper;
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
    public Optional<SiteLocationType> findSiteLocationTypeById(UUID id) {
        return siteLocTypeRepo.findById(id);
    }

    @Override
    @Cacheable(cacheNames = "siteLocationTypesCache")
    public List<SiteLocationTypeDTO> getAllSiteLocationTypes() {
        return siteLocTypeRepo.findAll().stream().map(siteLocationTypeMapper::siteLocationTypeToSiteLocationTypeDto).toList();
    }
}
