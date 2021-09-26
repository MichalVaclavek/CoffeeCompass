package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;

public interface CSStatusService {

    CoffeeSiteStatus findCoffeeSiteStatusByName(String coffeeSiteStatus);
    List<CoffeeSiteStatus> getAllCoffeeSiteStatuses();
}
