package cz.fungisoft.coffeecompass.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Status zaznamu o Coffee situ jako DTO
 * 
 * @author Michal Václavek
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CompanyDTO extends BaseItem {

    private String nameOfCompany;
}
