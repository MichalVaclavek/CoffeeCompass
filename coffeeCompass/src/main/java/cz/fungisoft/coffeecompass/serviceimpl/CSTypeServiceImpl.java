package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.exception.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteTypeRepository;
import cz.fungisoft.coffeecompass.service.CSTypeService;

@Service("csTypeService")
@Transactional
public class CSTypeServiceImpl implements CSTypeService
{
    private CoffeeSiteTypeRepository csTypeRepo;
    
    @Autowired
    public CSTypeServiceImpl(CoffeeSiteTypeRepository csTypeRepo) {
        super();
        this.csTypeRepo = csTypeRepo;
    }

    @Override
    public CoffeeSiteType findCoffeeSiteTypeByName(String csTypeName) {
        CoffeeSiteType csType = csTypeRepo.searchByName(csTypeName);
        if (csType == null)
            throw new EntityNotFoundException("Coffee site type " + csTypeName + " not found in DB.");
        return csType;
    }

    @Override
    public List<CoffeeSiteType> getAllCoffeeSiteTypes() {
        return csTypeRepo.findAll();
    }
}
