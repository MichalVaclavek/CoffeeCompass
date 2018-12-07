package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.OtherOffer;

public interface OtherOfferService
{
    public OtherOffer findOfferByName(String offer);
    public OtherOffer findOfferById(Integer id);
    public List<OtherOffer> getAllOtherOffers();
}
