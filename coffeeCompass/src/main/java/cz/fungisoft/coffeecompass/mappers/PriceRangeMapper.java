package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.PriceRangeDTO;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PriceRangeMapper {

    @Mapping(target = "id", source="extId")
    PriceRange priceRangeDtoToPriceRange(PriceRangeDTO priceRangeDTO);

    @Mapping(target = "extId", source="id")
    PriceRangeDTO priceRangeToPriceRangeDto(PriceRange priceRange);
}
