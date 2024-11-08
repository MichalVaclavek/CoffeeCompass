package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.SiteLocationTypeDTO;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface SiteLocationTypeMapper {

    @Mapping(target = "id", source="extId")
    SiteLocationType siteLocationTypeDtoToSiteLocationType(SiteLocationTypeDTO siteLocationTypeDTO);

    @Mapping(target = "extId", source="id")
    SiteLocationTypeDTO siteLocationTypeToSiteLocationTypeDto(SiteLocationType siteLocationType);
}
