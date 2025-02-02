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
public class OtherOfferDTO extends BaseItem {

    private String otherOffer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OtherOfferDTO that = (OtherOfferDTO) o;
        return Objects.equals(getExtId(), that.getExtId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExtId());
    }
}
