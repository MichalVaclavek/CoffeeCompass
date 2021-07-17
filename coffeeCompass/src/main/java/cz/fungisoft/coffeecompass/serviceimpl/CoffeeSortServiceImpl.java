package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.repository.CoffeeSortRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;

@Service("coffeeSortService")
public class CoffeeSortServiceImpl implements CoffeeSortService {

    private CoffeeSortRepository coffeeSortRepo;
    
    @Autowired
    public CoffeeSortServiceImpl(CoffeeSortRepository coffeeSort) {
        super();
        this.coffeeSortRepo = coffeeSort;
    }

    @Override
    @Transactional
    public CoffeeSort findCoffeeSortByName(String coffeeSortName) {
        return coffeeSortRepo.searchByName(coffeeSortName);
    }

    @Override
    @Transactional
    public List<CoffeeSort> getAllCoffeeSorts() {
        return coffeeSortRepo.findAll();
    }
}
