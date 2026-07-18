package cz.fungisoft.coffeecompass.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Status zaznamu o Coffee situ jako DTO
 * 
 * @author Michal Václavek
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CoffeeSiteStatusDTO extends BaseItem {

    public CoffeeSiteStatusDTO() {
        super();
    }

    public CoffeeSiteStatusDTO(UUID extId, String status, String statusCz) {
        super(extId);
        this.status = status;
        this.statusCz = statusCz;
    }

    private String status;

    private String statusCz;
}
