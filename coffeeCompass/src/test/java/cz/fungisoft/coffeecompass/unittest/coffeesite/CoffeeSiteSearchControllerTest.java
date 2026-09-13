package cz.fungisoft.coffeecompass.unittest.coffeesite;

import cz.fungisoft.coffeecompass.controller.CoffeeSiteSearchController;
import cz.fungisoft.coffeecompass.controller.models.CoffeeSiteSearchCriteriaModel;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoffeeSiteSearchControllerTest {

    @Test
    void searchSitesWithCoordinatesAndEmptyCityUsesGeoSearchWithoutCityFilter() {
        CoffeeSiteService coffeeSiteService = Mockito.mock(CoffeeSiteService.class);
        CoffeeSortService coffeeSortService = Mockito.mock(CoffeeSortService.class);
        CoffeeSiteSearchController controller = new CoffeeSiteSearchController(coffeeSiteService, coffeeSortService);

        CoffeeSiteSearchCriteriaModel searchCriteria = new CoffeeSiteSearchCriteriaModel();
        searchCriteria.setLat1(50.157848038149226);
        searchCriteria.setLon1(14.749703421976092);
        searchCriteria.setCityName("");
        searchCriteria.setRange(5000L);

        when(coffeeSiteService.findAllWithinCircleWithCSStatusAndCoffeeSort(
                eq(searchCriteria.getLat1()),
                eq(searchCriteria.getLon1()),
                eq(searchCriteria.getRange()),
                eq(""),
                isNull()
        )).thenReturn(List.of());

        controller.searchSitesWithStatusAndCoffeeSort(
                searchCriteria,
                new BeanPropertyBindingResult(searchCriteria, "searchCriteria")
        );

        verify(coffeeSiteService).findAllWithinCircleWithCSStatusAndCoffeeSort(
                eq(searchCriteria.getLat1()),
                eq(searchCriteria.getLon1()),
                eq(searchCriteria.getRange()),
                eq(""),
                isNull()
        );
        verify(coffeeSiteService, never()).findAllWithinCircleAndCityWithCSStatusAndCoffeeSort(
                Mockito.anyDouble(),
                Mockito.anyDouble(),
                Mockito.anyLong(),
                Mockito.any(),
                Mockito.anyString()
        );
    }
}
