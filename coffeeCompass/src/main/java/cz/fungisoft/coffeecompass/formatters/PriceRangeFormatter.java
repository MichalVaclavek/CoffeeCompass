package cz.fungisoft.coffeecompass.formatters;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.service.PriceRangeService;

@Component
public class PriceRangeFormatter implements Formatter<PriceRange>
{
    private PriceRangeService priceRangeService;
    
    @Autowired
    public PriceRangeFormatter(PriceRangeService priceRangeService) {
        super();
        this.priceRangeService = priceRangeService;
    }

    @Override
    public String print(PriceRange priceRange, Locale locale) {
        return (priceRange != null ? Integer.toString(priceRange.getId()) : "");
    }

    @Override
    public PriceRange parse(String text, Locale locale) throws ParseException {
        return priceRangeService.findPriceRangeByString(text);
    }
}
