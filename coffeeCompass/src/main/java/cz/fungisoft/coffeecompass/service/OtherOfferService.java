package cz.fungisoft.coffeecompass.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.OtherOfferDTO;
import cz.fungisoft.coffeecompass.entity.OtherOffer;

public interface OtherOfferService {

    OtherOffer findOfferByExtId(String extId);
    List<OtherOfferDTO> getAllOtherOffers();

    Optional<OtherOffer> findOtherOfferById(UUID id);
}
