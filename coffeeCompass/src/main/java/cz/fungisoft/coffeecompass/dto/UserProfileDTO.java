package cz.fungisoft.coffeecompass.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Trida pro prenos vybranych informaci o objektu User na clienta. Tzv. DTO objekt.
 * 
 * @author Michal Vaclavek
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserProfileDTO extends BaseItem {

    private String type;
}
