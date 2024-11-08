package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.CupTypeDTO;
import cz.fungisoft.coffeecompass.mappers.CupTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.CupTypeRepository;
import cz.fungisoft.coffeecompass.service.CupTypeService;

@Service("cupTypeService")
@Transactional
public class CupTypeServiceImpl implements CupTypeService {

    private final CupTypeRepository cupTypeRepo;

    private final CupTypeMapper cupTypeMapper;
    
    @Autowired
    public CupTypeServiceImpl(CupTypeRepository cupTypeRepo, CupTypeMapper cupTypeMapper) {
        super();
        this.cupTypeRepo = cupTypeRepo;
        this.cupTypeMapper = cupTypeMapper;
    }

    @Override
    @Cacheable(cacheNames = "cupTypesCache")
    public CupType findCupTypeByName(String cupTypeName) {
        CupType cupType = cupTypeRepo.searchByName(cupTypeName);
        if (cupType == null)
            throw new EntityNotFoundException("Cup type name " + cupTypeName + " not found in DB.");
        return cupType;
    }

    @Override
    @Cacheable(cacheNames = "cupTypesCache")
    public List<CupTypeDTO> getAllCupTypes() {
        return cupTypeRepo.findAll().stream().map(cupTypeMapper::cupTypeToCupTypeDto).toList();
    }
}
