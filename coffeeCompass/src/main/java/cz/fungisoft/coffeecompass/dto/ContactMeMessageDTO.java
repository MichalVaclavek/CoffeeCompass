package cz.fungisoft.coffeecompass.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * Object/model pro prenos a validaci zakladni entity ContactMeMessage
 * 
 * @author Michal Vaclavek
 *
 */
@Data
public class ContactMeMessageDTO {

    private Integer id;
    
    @Size(max=50)
    private String authorName;
      
    @Email
    @Size(max=60)
    private String email;
     
    @Size(min=1, max=512)
    private String textOfMessage;
}
