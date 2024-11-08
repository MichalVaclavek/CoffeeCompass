package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteRecordStatusDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;

public interface CSRecordStatusService {

    CoffeeSiteRecordStatus findCSRecordStatusByName(String coffeeSiteRecordStatus);
    CoffeeSiteRecordStatus findCSRecordStatus(CoffeeSiteRecordStatusEnum coffeeSiteRecordStatus);
    List<CoffeeSiteRecordStatusDTO> getAllCSRecordStatuses();
}
