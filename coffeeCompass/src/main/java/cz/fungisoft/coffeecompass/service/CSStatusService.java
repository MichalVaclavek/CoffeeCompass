package cz.fungisoft.coffeecompass.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteStatusDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;

public interface CSStatusService {

    Optional<CoffeeSiteStatus> findCoffeeSiteStatusByName(String coffeeSiteStatus);
    Optional<CoffeeSiteStatus> findCoffeeSiteStatusById(UUID uuid);
    List<CoffeeSiteStatusDTO> getAllCoffeeSiteStatuses();
}
