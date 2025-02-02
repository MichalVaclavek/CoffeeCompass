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
    @Cacheable(cacheNames = "otherOffersCache")
    public List<OtherOfferDTO> getAllOtherOffers() {
        return offerRepo.findAll().stream().map(otherOfferMapper::otherOfferToOtherOfferDto).toList();
    }

    @Override
    @Cacheable(cacheNames = "otherOffersCache")
    public OtherOffer findOfferByExtId(String extId) {
        Optional<OtherOffer> otherOffer = offerRepo.findById(UUID.fromString(extId));
        return otherOffer.orElseThrow(() -> new EntityNotFoundException("Other offer id " + extId + " not found in DB."));
    }
}
