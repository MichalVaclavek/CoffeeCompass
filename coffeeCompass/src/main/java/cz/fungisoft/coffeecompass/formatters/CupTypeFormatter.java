package cz.fungisoft.coffeecompass.formatters;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.service.CupTypeService;

@Component
public class CupTypeFormatter implements Formatter<CupType> {

    private final CupTypeService cupTypeService;
    
    @Autowired
    public CupTypeFormatter(CupTypeService cupTypeService) {
        super();
        this.cupTypeService = cupTypeService;
    }

    @Override
    public String print(CupType cupType, Locale locale) {
        return Integer.toString(cupType.getLongId());
    }

    @Override
    public CupType parse(String text, Locale locale) throws ParseException {
        return cupTypeService.findCupTypeByName(text);
    }
}
