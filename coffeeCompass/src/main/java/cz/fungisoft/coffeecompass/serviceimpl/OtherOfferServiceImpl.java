package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.OtherOfferDTO;
import cz.fungisoft.coffeecompass.mappers.OtherOfferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.OfferRepository;
import cz.fungisoft.coffeecompass.service.OtherOfferService;

@Service("otherOfferService")
@Transactional
public class OtherOfferServiceImpl implements OtherOfferService {

    private final OfferRepository offerRepo;

    private final OtherOfferMapper otherOfferMapper;
    
    @Autowired
    public OtherOfferServiceImpl(OfferRepository offerRepo, OtherOfferMapper otherOfferMapper) {
        super();
        this.offerRepo = offerRepo;
        this.otherOfferMapper = otherOfferMapper;
    }

    @Override
    public OtherOffer findOfferByName(String offerName) {
        OtherOffer otherOffer = offerRepo.searchByName(offerName);
        if (otherOffer == null)
            throw new EntityNotFoundException("Other offer name " + offerName + " not found in DB.");
        return otherOffer;
    }

    @Override
    @Cacheable(cacheNames = "otherOffersCache")
    public List<OtherOfferDTO> getAllOtherOffers() {
        return offerRepo.findAll().stream().map(otherOfferMapper::otherOfferToOtherOfferDto).toList();
    }

    @Override
    @Cacheable(cacheNames = "otherOffersCache")
    public OtherOffer findOfferById(Integer id) {
        Optional<OtherOffer> otherOffer = offerRepo.findByLongId(id);
        if (otherOffer.isEmpty())
            throw new EntityNotFoundException("Other offer id " + id + " not found in DB.");
        return otherOffer.get();
    }

    @Override
    @Cacheable(cacheNames = "otherOffersCache")
    public OtherOffer findOfferByExtId(String extId) {
        Optional<OtherOffer> otherOffer = offerRepo.findById(UUID.fromString(extId));
        if (otherOffer.isEmpty())
            throw new EntityNotFoundException("Other offer id " + extId + " not found in DB.");
        return otherOffer.get();
    }
}
