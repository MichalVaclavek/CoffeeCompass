package cz.fungisoft.coffeecompass.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * Class representing an image DTO object for uploading images of the CoffeeSite from web form
 *
 * @author Michal Václavek
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ImageDTO extends BaseItem implements Serializable {

	private MultipartFile file;

	@Size(max=64)
	private String externalObjectId;

	@Size(max=254)
	private String description;

	@Size(max=16)
	private String imageType;
}
