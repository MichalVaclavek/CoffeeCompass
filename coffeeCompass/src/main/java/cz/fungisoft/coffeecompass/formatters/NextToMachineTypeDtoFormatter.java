package cz.fungisoft.coffeecompass.formatters;

import cz.fungisoft.coffeecompass.dto.NextToMachineTypeDTO;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.mappers.NextToMachineTypeMapper;
import cz.fungisoft.coffeecompass.service.NextToMachineTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class NextToMachineTypeDtoFormatter implements Formatter<NextToMachineTypeDTO> {

    private final NextToMachineTypeService ntmtService;

    private final NextToMachineTypeMapper ntmtMapper;

    @Autowired
    public NextToMachineTypeDtoFormatter(NextToMachineTypeService ntmtService, NextToMachineTypeMapper ntmtMapper) {
        super();
        this.ntmtService = ntmtService;
        this.ntmtMapper = ntmtMapper;
    }

    @Override
    public String print(NextToMachineTypeDTO ntmt, Locale locale) {
//        return ntmt.getExtId().toString();
        return ntmt.getNextToMachineType();
    }

    @Override
    public NextToMachineTypeDTO parse(String text, Locale locale) throws ParseException {
        return ntmtMapper.nextToMachineTypeToNextToMachineTypeDto(ntmtService.findNextToMachineTypeByName(text));
    }
}
