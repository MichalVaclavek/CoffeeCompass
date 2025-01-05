package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.OtherOfferDTO;
import cz.fungisoft.coffeecompass.entity.OtherOffer;

public interface OtherOfferService {

    OtherOffer findOfferByName(String offer);
    OtherOffer findOfferByExtId(String extId);
    List<OtherOfferDTO> getAllOtherOffers();
}
