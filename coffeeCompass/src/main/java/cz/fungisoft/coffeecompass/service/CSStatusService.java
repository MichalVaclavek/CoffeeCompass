package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;

public interface CSStatusService
{
    public CoffeeSiteStatus findCoffeeSiteStatusByName(String coffeeSiteStatus);
    public List<CoffeeSiteStatus> getAllCoffeeSiteStatuses();
}
