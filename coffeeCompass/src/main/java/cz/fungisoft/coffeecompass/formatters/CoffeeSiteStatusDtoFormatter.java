package cz.fungisoft.coffeecompass.formatters;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteStatusDTO;
import cz.fungisoft.coffeecompass.mappers.CoffeeSiteStatusMapper;
import cz.fungisoft.coffeecompass.service.CSStatusService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class CoffeeSiteStatusDtoFormatter implements Formatter<CoffeeSiteStatusDTO> {

    private final CSStatusService csStatusService;
    private final CoffeeSiteStatusMapper coffeeSiteStatusMapper;


    @Autowired
    public CoffeeSiteStatusDtoFormatter(CSStatusService csStatusService, CoffeeSiteStatusMapper coffeeSiteStatusMapper) {
        super();
        this.csStatusService = csStatusService;
        this.coffeeSiteStatusMapper = coffeeSiteStatusMapper;
    }

    @Override
    public String print(CoffeeSiteStatusDTO coffeeSiteStatusDTO, @NotNull Locale locale) {
        return coffeeSiteStatusDTO.getExtId().toString();
    }

    @Override
    public CoffeeSiteStatusDTO parse(@NotNull String text, @NotNull Locale locale) throws ParseException {
        return coffeeSiteStatusMapper.csStatusToCsStatusDto(csStatusService.findCoffeeSiteStatusByName(text).orElseThrow(() -> new ParseException("CoffeeSiteStatus not found: " + text, 0)));
    }
}
