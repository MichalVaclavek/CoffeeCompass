package cz.fungisoft.coffeecompass.formatters;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.service.StarsQualityService;

@Component
public class StarsQualityDescriptionFormatter implements Formatter<StarsQualityDescription> {

    private final StarsQualityService starsQualityService;
    
    @Autowired
    public StarsQualityDescriptionFormatter(StarsQualityService starsQualityService) {
        super();
        this.starsQualityService = starsQualityService;
    }

    @Override
    public String print(StarsQualityDescription starsQuality, Locale locale) {
        return Integer.toString(starsQuality.getNumOfStars());
    }

    @Override
    public StarsQualityDescription parse(String text, Locale locale) throws ParseException {
        return starsQualityService.findStarsQualityByExtId(text);
    }
}