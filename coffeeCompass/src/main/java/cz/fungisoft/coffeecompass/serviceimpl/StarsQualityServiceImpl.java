package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.repository.StarsQualityDescriptionRepository;
import cz.fungisoft.coffeecompass.service.StarsQualityService;

@Service("starsQualityDescrService")
@Transactional
public class StarsQualityServiceImpl implements StarsQualityService
{
    private StarsQualityDescriptionRepository starsQaulityRepo;
        
    @Autowired
    public StarsQualityServiceImpl(StarsQualityDescriptionRepository starsQaulityRepo) {
        super();
        this.starsQaulityRepo = starsQaulityRepo;
    }
    
    @Override
    public StarsQualityDescription findStarsQualityDescr(String starsQualityDescr) {
        return starsQaulityRepo.searchByName(starsQualityDescr);
    }

    @Override
    public List<StarsQualityDescription> getAllStarsQualityDescriptions() {
        return starsQaulityRepo.findAll();
    }
    
    @Override
    public StarsQualityDescription findStarsQualityById(Integer id) {
        return starsQaulityRepo.findById(id).orElse(null);
    }
}
