package cz.fungisoft.coffeecompass.formatters;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.service.NextToMachineTypeService;

@Component
public class NextToMachineTypeFormatter implements Formatter<NextToMachineType>
{
    private NextToMachineTypeService ntmtService;
    
    @Autowired
    public NextToMachineTypeFormatter(NextToMachineTypeService ntmtService) {
        super();
        this.ntmtService = ntmtService;
    }

    @Override
    public String print(NextToMachineType ntmt, Locale locale) {
        return (ntmt != null ? Integer.toString(ntmt.getId()) : "");
    }

    @Override
    public NextToMachineType parse(String text, Locale locale) throws ParseException {
        return ntmtService.findNextToMachineTypeByName(text);
    }
}
