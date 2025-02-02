package cz.fungisoft.coffeecompass.formatters;

import cz.fungisoft.coffeecompass.dto.OtherOfferDTO;
import cz.fungisoft.coffeecompass.mappers.OtherOfferMapper;
import cz.fungisoft.coffeecompass.service.OtherOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Mozna nebude vubec potreba? Melo by byt nutne pro internacionalizaci, kdy thymeleaf musi hledat
 * v .properties souborech.
 * 
 * @author Michal
 *
 */
@Component
public class OtherOfferDtoFormatter implements Formatter<OtherOfferDTO> {

    private final OtherOfferService offerService;
    private final OtherOfferMapper otherOfferMapper;

    @Autowired
    public OtherOfferDtoFormatter(OtherOfferService offerService, OtherOfferMapper otherOfferMapper) {
        super();
        this.offerService = offerService;
        this.otherOfferMapper = otherOfferMapper;
    }

    @Override
    public String print(OtherOfferDTO offer, Locale locale) {
        return offer.getExtId().toString();
    }

    @Override
    public OtherOfferDTO parse(String extId, Locale locale) throws ParseException {
        return otherOfferMapper.otherOfferToOtherOfferDto(offerService.findOfferByExtId(extId));
    }
}
