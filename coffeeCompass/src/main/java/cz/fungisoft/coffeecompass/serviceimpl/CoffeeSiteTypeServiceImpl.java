package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteTypeDTO;
import cz.fungisoft.coffeecompass.mappers.CoffeeSiteTypeMapper;
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

    private final CoffeeSiteTypeMapper coffeeSiteTypeMapper;
    
    @Autowired
    public CoffeeSiteTypeServiceImpl(CoffeeSiteTypeRepository coffeeSiteTypeRepo, CoffeeSiteTypeMapper coffeeSiteTypeMapper) {
        super();
        this.coffeeSiteTypeRepo = coffeeSiteTypeRepo;
        this.coffeeSiteTypeMapper = coffeeSiteTypeMapper;
    }

    @Cacheable(cacheNames = "coffeeSiteTypesCache")
    @Override
    @Transactional
    public CoffeeSiteType findCoffeeSiteTypeByName(String coffeeSiteType) {
        return coffeeSiteTypeRepo.searchByName(coffeeSiteType);
    }

    @Override
    public Optional<CoffeeSiteType> findById(UUID uuid) {
        return coffeeSiteTypeRepo.findById(uuid);
    }

    @Cacheable(cacheNames = "coffeeSiteTypesCache")
    @Override
    @Transactional
    public List<CoffeeSiteTypeDTO> getAllCoffeeSiteTypes() {
        return coffeeSiteTypeRepo.findAll().stream().map(coffeeSiteTypeMapper::coffeeSiteTypeToCoffeeSiteTypeDto).toList();
    }
}