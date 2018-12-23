package cz.fungisoft.coffeecompass.entity;

import java.io.IOException;
//import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.web.multipart.MultipartFile;
//import org.apache.tomcat.util.http.fileupload.FileUpload;

import lombok.Data;
/*
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.hibernate.validator.constraints.NotEmpty;
*/

/**
 * Class representing an image assigned to CoffeeSite. Contains {@link CoffeeSite} atribute to link the Image and CoffeeSite.
 *
 * @author Michal Václavek - added JPA Hibernate
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
	
//    @NotNull
	@Column(name = "saved_on", nullable = false)
	private Timestamp savedOn;
		
	@NotEmpty
	@Column(name="file_name", nullable=false)
	private String fileName;
	
	/**
	 * File object used in FileUploadController and ImageFileStorageServiceImpl
	 */
	@Transient
	private MultipartFile file;
	
	public void setFile(MultipartFile file) {
	    this.file = file;
	    this.fileName = file.getOriginalFilename();
	}
	
	@Column(name="data", nullable=false)
	private byte[] imageBytes;
	
	public Image() {}
	
	public Image(CoffeeSite cfSite, MultipartFile imageFile) throws IOException
	{
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
