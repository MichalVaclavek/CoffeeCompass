package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.SiteLocationTypeDTO;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;

public interface SiteLocationTypeService {

    SiteLocationType findSiteLocationType(String siteLocationType);
    List<SiteLocationTypeDTO> getAllSiteLocationTypes();
}
