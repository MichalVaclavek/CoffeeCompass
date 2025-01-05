package cz.fungisoft.coffeecompass.formatters;

import cz.fungisoft.coffeecompass.dto.CupTypeDTO;
import cz.fungisoft.coffeecompass.mappers.CupTypeMapper;
import cz.fungisoft.coffeecompass.service.CupTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class CupTypeDtoFormatter implements Formatter<CupTypeDTO> {

    private final CupTypeService cupTypeService;
    private final CupTypeMapper cupTypeMapper;

    @Autowired
    public CupTypeDtoFormatter(CupTypeService cupTypeService, CupTypeMapper cupTypeMapper) {
        super();
        this.cupTypeService = cupTypeService;
        this.cupTypeMapper = cupTypeMapper;
    }

    @Override
    public String print(CupTypeDTO cupType, Locale locale) {
        return cupType.getExtId().toString();
    }

    @Override
    public CupTypeDTO parse(String text, Locale locale) throws ParseException {
        return cupTypeMapper.cupTypeToCupTypeDto(cupTypeService.findCupTypeByName(text));
    }
}
