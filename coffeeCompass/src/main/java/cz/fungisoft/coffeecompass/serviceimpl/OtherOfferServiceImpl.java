package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.OfferRepository;
import cz.fungisoft.coffeecompass.service.OtherOfferService;

@Service("otherOfferService")
@Transactional
public class OtherOfferServiceImpl implements OtherOfferService {

    private OfferRepository offerRepo;
    
    @Autowired
    public OtherOfferServiceImpl(OfferRepository offerRepo) {
        super();
        this.offerRepo = offerRepo;
    }

    @Override
    public OtherOffer findOfferByName(String offerName) {
        OtherOffer otherOffer = offerRepo.searchByName(offerName);
        if (otherOffer == null)
            throw new EntityNotFoundException("Other offer name " + offerName + " not found in DB.");
        return otherOffer;
    }

    @Override
    public List<OtherOffer> getAllOtherOffers() {
        return offerRepo.findAll();
    }

    @Override
    public OtherOffer findOfferById(Integer id) {
        OtherOffer otherOffer = offerRepo.findById(id).orElse(null);
        if (otherOffer == null)
            throw new EntityNotFoundException("Other offer id " + id + " not found in DB.");
        return otherOffer;
    }
}
