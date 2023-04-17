package cz.fungisoft.coffeecompass.dto;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Status zaznamu o Coffee situ jako DTO
 * 
 * @author Michal Václavek
 *
 */
@Data
public class CoffeeSiteRecordStatusDTO {
    
    private String status;
}
