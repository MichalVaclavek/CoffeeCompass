package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.repository.CupTypeRepository;
import cz.fungisoft.coffeecompass.service.CupTypeService;

@Service("cupTypeService")
@Transactional
public class CupTypeServiceImpl implements CupTypeService
{
    private CupTypeRepository cupTypeRepo;
    
    @Autowired
    public CupTypeServiceImpl(CupTypeRepository cupTypeRepo) {
        super();
        this.cupTypeRepo = cupTypeRepo;
    }

    @Override
    public CupType findCupTypeByName(String cupType) {
        return cupTypeRepo.searchByName(cupType);
    }

    @Override
    public List<CupType> getAllCupTypes() {
        return cupTypeRepo.findAll();
    }
}
