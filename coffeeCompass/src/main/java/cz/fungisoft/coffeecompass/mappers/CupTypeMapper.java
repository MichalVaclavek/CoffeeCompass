package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CupTypeDTO;
import cz.fungisoft.coffeecompass.entity.CupType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CupTypeMapper {

    @Mapping(target = "id", source="extId")
    CupType cupTypeDtoToCupType(CupTypeDTO cupTypeDTO);

    @Mapping(target = "extId", source="id")
    CupTypeDTO cupTypeToCupTypeDto(CupType cupType);
}
