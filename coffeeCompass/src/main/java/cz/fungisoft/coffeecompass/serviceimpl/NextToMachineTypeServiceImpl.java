package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.NextToMachineTypeDTO;
import cz.fungisoft.coffeecompass.mappers.NextToMachineTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.NextToMachineTypeRepository;
import cz.fungisoft.coffeecompass.service.NextToMachineTypeService;

@Service("nextToMachineTypeService")
@Transactional
public class NextToMachineTypeServiceImpl implements NextToMachineTypeService {

    private final NextToMachineTypeRepository ntmTypeRepo;

    private final NextToMachineTypeMapper ntmTypeMapper;
    
    @Autowired
    public NextToMachineTypeServiceImpl(NextToMachineTypeRepository ntmTypeRepo, NextToMachineTypeMapper ntmTypeMapper) {
        super();
        this.ntmTypeRepo = ntmTypeRepo;
        this.ntmTypeMapper = ntmTypeMapper;
    }

    @Override
    @Cacheable(cacheNames = "csNextToMachineTypesCache")
    public NextToMachineType findNextToMachineTypeByExtId(String extId) {
        Optional<NextToMachineType> ntmType = ntmTypeRepo.findById(UUID.fromString(extId));
        return ntmType.orElseThrow(() -> new EntityNotFoundException("Next to machine type Ext id " + extId + " not found in DB."));
    }

    @Override
    @Cacheable(cacheNames = "csNextToMachineTypesCache")
    public List<NextToMachineTypeDTO> getAllNextToMachineTypes() {
        return ntmTypeRepo.findAll().stream().map(ntmTypeMapper::nextToMachineTypeToNextToMachineTypeDto).toList();
    }
}
