package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteRecordStatusDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import org.mapstruct.Mapper;

@Mapper
public interface CoffeeSiteRecordStatusMapper {

    CoffeeSiteRecordStatus csRecordStatusDtoToCsRecordStatus(CoffeeSiteRecordStatusDTO csRecordStatusDTO);
    CoffeeSiteRecordStatusDTO csRecordStatusToCsRecordStatusDTO(CoffeeSiteRecordStatus csRecordStatus);
}
