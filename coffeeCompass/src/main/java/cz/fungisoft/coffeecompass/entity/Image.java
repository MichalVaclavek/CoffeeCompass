package cz.fungisoft.coffeecompass.entity;

import cz.fungisoft.coffeecompass.validators.ImageFileValidatorConstraint;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Class representing an image assigned to CoffeeSite. Contains {@link CoffeeSite} atribute to link the Image and respective CoffeeSite.
 *
 * @author Michal Václavek
 */
@Entity
@Table(name="images", schema="coffeecompass")
@Getter
@Setter
@ToString
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Image image = (Image) o;
		return id != null && Objects.equals(id, image.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
