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
public class CupTypeDTO extends BaseItem {

    private String cupType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CupTypeDTO that = (CupTypeDTO) o;
        return Objects.equals(getExtId(), that.getExtId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExtId());
    }
}
