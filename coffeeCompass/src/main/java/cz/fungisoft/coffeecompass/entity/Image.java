package cz.fungisoft.coffeecompass.entity;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;

import org.springframework.web.multipart.MultipartFile;

import cz.fungisoft.coffeecompass.validators.ImageFileValidatorConstraint;
import lombok.Data;

/**
 * Class representing an image assigned to CoffeeSite. Contains {@link CoffeeSite} atribute to link the Image and respective CoffeeSite.
 *
 * @author Michal Václavek
 */
@Entity
@Table(name="images", schema="coffeecompass")
@Data
public class Image implements Serializable
{	
	private static final long serialVersionUID = 4976306313068414171L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	  
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffeesite_id")
    private CoffeeSite coffeeSite;
	
	public void setCoffeeSite(CoffeeSite coffeeSite) {
	    this.coffeeSite = coffeeSite;
	    this.coffeeSiteID = this.coffeeSite.getId();
	}
	
	/**
	 * Used to transfer coffeeSite id between different Views/Forms in case only Image object is handled by the Form
	 */
	@Transient
	private Long coffeeSiteID = 0L;
	
	@Column(name = "saved_on", nullable = false)
	private Timestamp savedOn;
		
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
		setCoffeeSite(cfSite);
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
