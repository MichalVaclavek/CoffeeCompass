package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.StarsQualityDescriptionDTO;
import cz.fungisoft.coffeecompass.mappers.StarsQualityDescriptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.StarsQualityDescriptionRepository;
import cz.fungisoft.coffeecompass.service.StarsQualityService;

@Service("starsQualityDescrService")
@Transactional
public class StarsQualityServiceImpl implements StarsQualityService {

    private final StarsQualityDescriptionRepository starsQaulityRepo;

    private final StarsQualityDescriptionMapper starsQualityDescriptionMapper;
        
    @Autowired
    public StarsQualityServiceImpl(StarsQualityDescriptionRepository starsQaulityRepo, StarsQualityDescriptionMapper starsQualityDescriptionMapper) {
        super();
        this.starsQaulityRepo = starsQaulityRepo;
        this.starsQualityDescriptionMapper = starsQualityDescriptionMapper;
    }
    
    @Override
    public StarsQualityDescription findStarsQualityDescr(String starsQualityDescr) {
        StarsQualityDescription qualityDescr = starsQaulityRepo.searchByName(starsQualityDescr);
        if (qualityDescr == null)
            throw new EntityNotFoundException("Quality description " + starsQualityDescr + " not found in DB.");
        return qualityDescr;
    }

    @Override
    @Cacheable(cacheNames = "starsQualityRatingsCache")
    public List<StarsQualityDescriptionDTO> getAllStarsQualityDescriptions() {
        return starsQaulityRepo.findAll().stream().map(starsQualityDescriptionMapper::starsQualityDescriptionTostarsQualityDescriptionDto).toList();
    }
    
    @Override
    @Cacheable(cacheNames = "starsQualityRatingsCache")
    public StarsQualityDescription findStarsQualityById(Integer id) {
        Optional<StarsQualityDescription> qualityDescr = starsQaulityRepo.searchById(id);
        if (qualityDescr.isEmpty())
            throw new EntityNotFoundException("Quality description id " + id + " not found in DB.");
        return qualityDescr.get();
    }

    @Override
    @Cacheable(cacheNames = "starsQualityRatingsCache")
    public StarsQualityDescription findStarsQualityByExtId(String extId) {
        StarsQualityDescription qualityDescr = starsQaulityRepo.findById(UUID.fromString(extId)).orElse(null);
        if (qualityDescr == null)
            throw new EntityNotFoundException("Quality description id " + extId + " not found in DB.");
        return qualityDescr;
    }
}
