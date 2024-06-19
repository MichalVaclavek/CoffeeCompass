package cz.fungisoft.coffeecompass.entity;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import jakarta.validation.constraints.NotEmpty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.web.multipart.MultipartFile;

import cz.fungisoft.coffeecompass.validators.ImageFileValidatorConstraint;
import lombok.Data;

/**
 * Class representing an image assigned to CoffeeSite. Contains {@link CoffeeSite} atribute to link the Image and respective CoffeeSite.
 *
 * @author Michal Václavek
 */
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="images", schema="coffeecompass")
@Data
public class Image implements Serializable {

	private static final long serialVersionUID = 4976306313068414171L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	public void setCoffeeSiteID(Long coffeeSiteId) {
	    this.coffeeSiteID = coffeeSiteId;
	}
	
	/**
	 * Used to transfer coffeeSite id between different Views/Forms in case only Image object is handled by the Form
	 */
	@Column(name = "coffeesite_id")
	private Long coffeeSiteID = 0L;
	
	@Column(name = "saved_on", nullable = false)
	private LocalDateTime savedOn;
		
	@NotEmpty
	@Column(name="file_name", nullable=false)
	private String fileName;
	
	/**
	 * File object used in FileUploadController and ImageFileStorageServiceImpl
	 */
	@Transient
	@ImageFileValidatorConstraint
	private MultipartFile file;
	
	public void setFile(MultipartFile file) {
	    this.file = file;
	    this.fileName = file.getOriginalFilename();
	}
	
	@Column(name="data", nullable=false)
	private byte[] imageBytes;
	
	public Image() {}
	
	public Image(CoffeeSite cfSite, MultipartFile imageFile) throws IOException {
		setCoffeeSiteID(cfSite.getId());
		setFileName(imageFile.getOriginalFilename());
		setImageBytes(imageFile.getBytes());
	}
	
	/**
	 * "Helping" constructor to allow saving image file to the DB during testing.
	 * 
	 * @param directory
	 * @param imageFile
	 */
	/*
	public Image(Directory directory, Path imageFile)
	{
		setDirectory(directory);
		setFileName(imageFile.getFileName().toString());

		byte[] data;
		try
		{
			data = Files.readAllBytes(imageFile);
			setBytes(data);
		} catch (IOException e)
		{
			System.err.println("Error loading image from file " + imageFile.getFileName());
		}		
	}
	*/
}
