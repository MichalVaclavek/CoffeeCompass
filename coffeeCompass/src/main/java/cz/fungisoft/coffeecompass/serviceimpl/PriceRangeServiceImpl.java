package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.PriceRangeRepository;
import cz.fungisoft.coffeecompass.service.PriceRangeService;

@Service("priceRangeService")
@Transactional
public class PriceRangeServiceImpl implements PriceRangeService {

    private PriceRangeRepository priceRangeRepo;
    
    @Autowired
    public PriceRangeServiceImpl(PriceRangeRepository priceRangeRepo) {
        super();
        this.priceRangeRepo = priceRangeRepo;
    }

    @Override
    public PriceRange findPriceRangeByString(String priceRangeString) {
        PriceRange priceRange = priceRangeRepo.searchByName(priceRangeString);
        if (priceRange == null) {
            throw new EntityNotFoundException("Price range " + priceRangeString + " not found in DB.");
        }
        return priceRange;
    }

    @Override
    public List<PriceRange> getAllPriceRanges() {
        return priceRangeRepo.findAll();
    }
}
