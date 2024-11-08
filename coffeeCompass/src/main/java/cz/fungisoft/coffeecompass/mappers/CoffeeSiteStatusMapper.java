package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteStatusDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CoffeeSiteStatusMapper {

    @Mapping(target = "id", source="extId")
    CoffeeSiteStatus csStatusDtoToCsStatus(CoffeeSiteStatusDTO cStatusDTO);

    @Mapping(target = "extId", source="id")
    CoffeeSiteStatusDTO csStatusToCsStatusDto(CoffeeSiteStatus csRecordStatus);
}
