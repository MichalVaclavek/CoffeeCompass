package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.SiteLocationType;

public interface SiteLocationTypeService {

    SiteLocationType findSiteLocationType(String siteLocationType);
    List<SiteLocationType> getAllSiteLocationTypes();
}
