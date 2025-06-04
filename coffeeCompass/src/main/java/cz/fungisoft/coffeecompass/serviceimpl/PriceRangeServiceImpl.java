package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.PriceRangeDTO;
import cz.fungisoft.coffeecompass.mappers.PriceRangeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.PriceRangeRepository;
import cz.fungisoft.coffeecompass.service.PriceRangeService;

@Service("priceRangeService")
@Transactional
public class PriceRangeServiceImpl implements PriceRangeService {

    private final PriceRangeRepository priceRangeRepo;

    private final PriceRangeMapper priceRangeMapper;
    
    @Autowired
    public PriceRangeServiceImpl(PriceRangeRepository priceRangeRepo, PriceRangeMapper priceRangeMapper) {
        super();
        this.priceRangeRepo = priceRangeRepo;
        this.priceRangeMapper = priceRangeMapper;
    }

    @Override
    @Cacheable(cacheNames = "priceRangesCache")
    public PriceRange findPriceRangeByString(String priceRangeString) {
        PriceRange priceRange = priceRangeRepo.searchByName(priceRangeString);
        if (priceRange == null) {
            throw new EntityNotFoundException("Price range " + priceRangeString + " not found in DB.");
        }
        return priceRange;
    }

    @Override
    @Cacheable(cacheNames = "priceRangesCache")
    public PriceRange findPriceRangeByExtId(String extId) {
        PriceRange priceRange = findPriceRangeById(UUID.fromString(extId)).orElse(null);
        if (priceRange == null) {
            throw new EntityNotFoundException("Price range extId " + extId + " not found in DB.");
        }
        return priceRange;
    }


    @Override
    @Cacheable(cacheNames = "priceRangesCache")
    public List<PriceRangeDTO> getAllPriceRanges() {
        return priceRangeRepo.findAll().stream().map(priceRangeMapper::priceRangeToPriceRangeDto).toList();
    }

    @Override
    public Optional<PriceRange> findPriceRangeById(UUID id) {
        return priceRangeRepo.findById(id);
    }
}
