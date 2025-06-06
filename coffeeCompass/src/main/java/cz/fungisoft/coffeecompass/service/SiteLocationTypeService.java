package cz.fungisoft.coffeecompass.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.SiteLocationTypeDTO;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;

public interface SiteLocationTypeService {

    SiteLocationType findSiteLocationType(String siteLocationType);
    Optional<SiteLocationType> findSiteLocationTypeById(UUID id);
    List<SiteLocationTypeDTO> getAllSiteLocationTypes();
}
