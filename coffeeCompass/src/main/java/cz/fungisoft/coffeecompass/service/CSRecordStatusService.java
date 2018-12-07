package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;

public interface CSRecordStatusService
{
    public CoffeeSiteRecordStatus findCSRecordStatusByName(String coffeeSiteRecordStatus);
    public CoffeeSiteRecordStatus findCSRecordStatus(CoffeeSiteRecordStatusEnum coffeeSiteRecordStatus);
    public List<CoffeeSiteRecordStatus> getAllCSRecordStatuses();
}
