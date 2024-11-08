package cz.fungisoft.test.image2.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author Michal Václavek
 */
@Entity
@Table(name = "image_file", schema="images")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ImageFileSet implements Serializable {

	@Serial
	private static final long serialVersionUID = 4976306313068414172L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	@JsonIgnore
	private Long id;

	@Column(name = "ext_id", nullable = false)
	private String extId;

	@CreationTimestamp(source = SourceType.DB)
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "saved_on", nullable = false)
	private LocalDateTime savedOn;

	@NotEmpty
	@Column(name="original_file_name", nullable=false)
	private String originalFileName;

	@Column(name="file_name_hd", nullable=false)
	private String fileNameHd;

	@Column(name="thumbnail_small_name")
	private String thumbnailSmallName;

	@Column(name="thumbnail_mid_name")
	private String thumbnailMidName;

	@Column(name="thumbnail_large_name")
	private String thumbnailLargeName;

	@Column(name="description")
	private String description;

	@Column(name="image_type")
	private String imageType;

	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name = "image_object_id", nullable = false)
	@JsonIgnore
	private ImageObject imageObject;

	@Transient
	@JsonIgnore
	private MultipartFile file;
	
	public void setFile(MultipartFile file) {
	    this.file = file;
	    this.originalFileName = file.getOriginalFilename();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		ImageFileSet imageFileSet = (ImageFileSet) o;
		return id != null && Objects.equals(id, imageFileSet.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
