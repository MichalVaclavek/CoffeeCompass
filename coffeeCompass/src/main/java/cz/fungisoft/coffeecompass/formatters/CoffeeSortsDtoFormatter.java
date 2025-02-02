package cz.fungisoft.coffeecompass.formatters;

import cz.fungisoft.coffeecompass.dto.CoffeeSortDTO;
import cz.fungisoft.coffeecompass.mappers.CoffeeSortMapper;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class CoffeeSortsDtoFormatter implements Formatter<CoffeeSortDTO> {

    private final CoffeeSortService coffeeSortService;

    private final CoffeeSortMapper coffeeSortMapper;

    @Autowired
    public CoffeeSortsDtoFormatter(CoffeeSortService coffeeSortService, CoffeeSortMapper coffeeSortMapper) {
        super();
        this.coffeeSortService = coffeeSortService;
        this.coffeeSortMapper = coffeeSortMapper;
    }

    @Override
    public String print(CoffeeSortDTO coffeeSort, Locale locale) {
        return coffeeSort.getExtId().toString();
    }

    @Override
    public CoffeeSortDTO parse(@NotNull String extId, Locale locale) throws ParseException {
        return coffeeSortMapper.coffeeSorToCoffeeSortDto(coffeeSortService.findCoffeeSortById(extId));
    }
}
