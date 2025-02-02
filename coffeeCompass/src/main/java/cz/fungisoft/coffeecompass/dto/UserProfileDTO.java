package cz.fungisoft.coffeecompass.dto;

import lombok.Data;

import java.util.Objects;

/**
 * Trida pro prenos vybranych informaci o objektu User na clienta. Tzv. DTO objekt.
 * 
 * @author Michal Vaclavek
 */
@Data
public class UserProfileDTO extends BaseItem {

    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfileDTO that = (UserProfileDTO) o;
        return Objects.equals(getExtId(), that.getExtId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExtId());
    }
}
