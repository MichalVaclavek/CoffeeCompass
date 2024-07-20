package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

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
    
    @Autowired
    public NextToMachineTypeServiceImpl(NextToMachineTypeRepository ntmTypeRepo) {
        super();
        this.ntmTypeRepo = ntmTypeRepo;
    }

    @Override
    @Cacheable(cacheNames = "csNextToMachineTypesCache")
    public NextToMachineType findNextToMachineTypeByName(String nextToMachineTypeName) {
        NextToMachineType ntmType = ntmTypeRepo.searchByName(nextToMachineTypeName);
        if (ntmType == null)
            throw new EntityNotFoundException("Next to machine type name " + nextToMachineTypeName + " not found in DB.");
        return ntmType;
    }

    @Override
    @Cacheable(cacheNames = "csNextToMachineTypesCache")
    public List<NextToMachineType> getAllNextToMachineTypes() {
        return ntmTypeRepo.findAll();
    }
}
