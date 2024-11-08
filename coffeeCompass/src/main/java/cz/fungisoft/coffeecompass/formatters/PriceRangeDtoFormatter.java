package cz.fungisoft.coffeecompass.formatters;

import cz.fungisoft.coffeecompass.dto.PriceRangeDTO;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.mappers.PriceRangeMapper;
import cz.fungisoft.coffeecompass.service.PriceRangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class PriceRangeDtoFormatter implements Formatter<PriceRangeDTO> {

    private final PriceRangeService priceRangeService;

    private final PriceRangeMapper priceRangeMapper;

    @Autowired
    public PriceRangeDtoFormatter(PriceRangeService priceRangeService, PriceRangeMapper priceRangeMapper) {
        super();
        this.priceRangeService = priceRangeService;
        this.priceRangeMapper = priceRangeMapper;
    }


    @Override
    public String print(PriceRangeDTO priceRange, Locale locale) {
//        return priceRange.getExtId().toString();
        return priceRange.getPriceRange();
    }

    @Override
    public PriceRangeDTO parse(String text, Locale locale) throws ParseException {
        return priceRangeMapper.priceRangeToPriceRangeDto(priceRangeService.findPriceRangeByString(text));
    }
}
