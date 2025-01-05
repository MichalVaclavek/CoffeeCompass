package cz.fungisoft.coffeecompass.formatters;

import cz.fungisoft.coffeecompass.dto.StarsQualityDescriptionDTO;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.mappers.PriceRangeMapper;
import cz.fungisoft.coffeecompass.mappers.StarsQualityDescriptionMapper;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class StarsQualityDescriptionDtoFormatter implements Formatter<StarsQualityDescriptionDTO> {

    private final StarsQualityService starsQualityService;
    private final StarsQualityDescriptionMapper starsQualityDescriptionMapper;


    @Autowired
    public StarsQualityDescriptionDtoFormatter(StarsQualityService starsQualityService, StarsQualityDescriptionMapper starsQualityDescriptionMapper) {
        super();
        this.starsQualityService = starsQualityService;
        this.starsQualityDescriptionMapper = starsQualityDescriptionMapper;
    }

    @Override
    public String print(StarsQualityDescriptionDTO starsQuality, Locale locale) {
        return starsQuality.getQuality();
    }

    @Override
    public StarsQualityDescriptionDTO parse(String text, Locale locale) throws ParseException {
        return starsQualityDescriptionMapper.starsQualityDescriptionTostarsQualityDescriptionDto(starsQualityService.findStarsQualityDescr(text));
    }
}