package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CoffeeSortDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CoffeeSortMapper {

    @Mapping(target = "id", source="extId")
    CoffeeSort coffeeSortDtoToCoffeeSort(CoffeeSortDTO coffeeSortDTO);

    @Mapping(target = "extId", source="id")
    CoffeeSortDTO coffeeSorToCoffeeSortDto(CoffeeSort coffeeSort);
}
