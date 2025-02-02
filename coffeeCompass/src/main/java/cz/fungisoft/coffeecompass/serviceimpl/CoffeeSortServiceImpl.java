package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.CoffeeSortDTO;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.mappers.CoffeeSortMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.repository.CoffeeSortRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;

@Service("coffeeSortService")
public class CoffeeSortServiceImpl implements CoffeeSortService {

    private final CoffeeSortRepository coffeeSortRepo;

    private final CoffeeSortMapper coffeeSortMapper;
    
    @Autowired
    public CoffeeSortServiceImpl(CoffeeSortRepository coffeeSort, CoffeeSortMapper coffeeSortMapper) {
        super();
        this.coffeeSortRepo = coffeeSort;
        this.coffeeSortMapper = coffeeSortMapper;
    }

    @Override
    public CoffeeSort findCoffeeSortById(String uuid) {
        return coffeeSortRepo.findById(UUID.fromString(uuid)).orElseThrow(() -> new EntityNotFoundException("Cup type with id " + uuid + " not found in DB."));
    }

    @Override
    @Transactional
    @Cacheable(cacheNames = "coffeeSortsCache")
    public List<CoffeeSortDTO> getAllCoffeeSorts() {
        return coffeeSortRepo.findAll().stream().map(coffeeSortMapper::coffeeSorToCoffeeSortDto).toList();
    }
}
