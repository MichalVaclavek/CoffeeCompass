package cz.fungisoft.coffeecompass.formatters;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;

@Component
public class CoffeeSortsFormatter implements Formatter<CoffeeSort>
{
    private CoffeeSortService coffeeSortService;
    
    @Autowired
    public CoffeeSortsFormatter(CoffeeSortService coffeeSortService) {
        super();
        this.coffeeSortService = coffeeSortService;
    }

    @Override
    public String print(CoffeeSort coffeeSort, Locale locale) {
        return (coffeeSort != null ? Integer.toString(coffeeSort.getId()) : "");
    }

    @Override
    public CoffeeSort parse(String text, Locale locale) throws ParseException {
        return coffeeSortService.findCoffeeSortByName(text);
    }
    
}
