package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteTypeDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CoffeeSiteTypeMapper {

    @Mapping(target = "id", source="extId")
    CoffeeSiteType coffeeSiteTypeDtoToCoffeeSiteType(CoffeeSiteTypeDTO coffeeSiteTypeDTO);

    @Mapping(target = "extId", source="id")
    CoffeeSiteTypeDTO coffeeSiteTypeToCoffeeSiteTypeDto(CoffeeSiteType coffeeSiteType);
}
