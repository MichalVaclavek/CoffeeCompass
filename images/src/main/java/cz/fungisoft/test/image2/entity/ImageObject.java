package cz.fungisoft.test.image2.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Relation between Image files and external object this image files relate to.
 *
 * @author Michal Václavek
 */
@Entity
@Table(name = "image_object", schema="images")
@NamedQueries({
		@NamedQuery(name = "getByExternalObjectId", query = "select i from ImageObject i where i.externalObjectId = :externalObjectId"),
		@NamedQuery(name = "getByExternalObjectId2", query = "select i from ImageObject i where upper(i.externalObjectId) like upper(:externalObjectId)")})
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ImageObject implements Serializable {

	@Serial
	private static final long serialVersionUID = 4976306313078414172L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	@JsonIgnore
	private Long id;

	@NotEmpty
	@Column(name="external_object_id", nullable=false)
	private String externalObjectId;

	@OneToMany(mappedBy = "imageObject", fetch = FetchType.LAZY,
			   cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<ImageFileSet> objectImages = new ArrayList<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		ImageObject that = (ImageObject) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
