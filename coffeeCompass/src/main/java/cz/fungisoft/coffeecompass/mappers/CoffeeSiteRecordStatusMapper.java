package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteRecordStatusDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CoffeeSiteRecordStatusMapper {

    @Mapping(target = "id", source="extId")
    CoffeeSiteRecordStatus csRecordStatusDtoToCsRecordStatus(CoffeeSiteRecordStatusDTO csRecordStatusDTO);

    @Mapping(target = "extId", source="id")
    CoffeeSiteRecordStatusDTO csRecordStatusToCsRecordStatusDto(CoffeeSiteRecordStatus csRecordStatus);
}
