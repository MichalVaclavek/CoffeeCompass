package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.SiteLocationType;

public interface SiteLocationTypeService
{
    public SiteLocationType findSiteLocationType(String siteLocationType);
    public List<SiteLocationType> getAllSiteLocationTypes();
}
