package cz.fungisoft.coffeecompass.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Object/model pro prenos a validaci zakladni entity ContactMeMessage
 * 
 * @author Michal Vaclavek
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContactMeMessageDTO extends BaseItem {

    @Size(max=50)
    private String authorName;
      
    @Email
    @Size(max=60)
    private String email;
     
    @Size(min=1, max=512)
    private String textOfMessage;
}
