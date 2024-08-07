package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteTypeRepository;
import cz.fungisoft.coffeecompass.service.CoffeeSiteTypeService;

@Service("coffeeSiteTypeService")
public class CoffeeSiteTypeServiceImpl implements CoffeeSiteTypeService {

    private final CoffeeSiteTypeRepository coffeeSiteTypeRepo;
    
    @Autowired
    public CoffeeSiteTypeServiceImpl(CoffeeSiteTypeRepository coffeeSiteTypeRepo) {
        super();
        this.coffeeSiteTypeRepo = coffeeSiteTypeRepo;
    }

    @Cacheable(cacheNames = "coffeesitetypes", key = "#coffeeSiteType")
    @Override
    @Transactional
    public CoffeeSiteType findCoffeeSiteTypeByName(String coffeeSiteType) {
        return coffeeSiteTypeRepo.searchByName(coffeeSiteType);
    }

    @Cacheable(cacheNames = "coffeesitetypes", key = "#coffeeSiteType")
    @Override
    @Transactional
    public List<CoffeeSiteType> getAllCoffeeSiteTypes() {
        return coffeeSiteTypeRepo.findAll();
    }
}