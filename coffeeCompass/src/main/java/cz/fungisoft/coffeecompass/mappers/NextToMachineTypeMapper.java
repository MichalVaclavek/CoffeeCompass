package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.NextToMachineTypeDTO;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface NextToMachineTypeMapper {

    @Mapping(target = "id", source="extId")
    @Mapping(source = "nextToMachineType", target = "type")
    NextToMachineType nextToMachineTypeDtoToNextToMachineType(NextToMachineTypeDTO nextToMachineTypeDTO);

    @Mapping(source = "type", target = "nextToMachineType")
    @Mapping(target = "extId", source="id")
    NextToMachineTypeDTO nextToMachineTypeToNextToMachineTypeDto(NextToMachineType nextToMachineType);
}
