package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.StarsQualityDescriptionDTO;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface StarsQualityDescriptionMapper {

    @Mapping(target = "id", source="extId")
    StarsQualityDescription starsQualityDescriptiontDtoToStarsQualityDescription(StarsQualityDescriptionDTO starsQualityDescriptionDTO);

    @Mapping(target = "extId", source="id")
    StarsQualityDescriptionDTO starsQualityDescriptionTostarsQualityDescriptionDto(StarsQualityDescription starsQualityDescription);
}
