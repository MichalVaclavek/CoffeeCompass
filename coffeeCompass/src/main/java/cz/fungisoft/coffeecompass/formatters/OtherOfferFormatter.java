package cz.fungisoft.coffeecompass.formatters;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.service.OtherOfferService;

/**
 * Mozna nebude vubec potreba? Melo by byt nutne pro internacionalizaci, kdy thymeleaf musi hledat
 * v .properties souborech.
 * 
 * @author Michal
 *
 */
@Component
public class OtherOfferFormatter implements Formatter<OtherOffer> {

    private final OtherOfferService offerService;
    
    @Autowired
    public OtherOfferFormatter(OtherOfferService offerService) {
        super();
        this.offerService = offerService;
    }

    @Override
    public String print(OtherOffer offer, Locale arg1) {
        return Integer.toString(offer.getLongId());
    }

    @Override
    public OtherOffer parse(String text, Locale arg1) throws ParseException {        
        return offerService.findOfferByName(text);
    }
}
