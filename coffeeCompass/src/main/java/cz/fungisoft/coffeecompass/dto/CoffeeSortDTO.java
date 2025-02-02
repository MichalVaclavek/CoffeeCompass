package cz.fungisoft.coffeecompass.dto;

import lombok.Data;

import java.util.Objects;

/**
 * Status zaznamu o Coffee situ jako DTO
 * 
 * @author Michal Václavek
 *
 */
@Data
public class CoffeeSortDTO extends BaseItem {

    private String coffeeSort;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoffeeSortDTO that = (CoffeeSortDTO) o;
        return Objects.equals(getExtId(), that.getExtId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExtId());
    }
}
