/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Class representing a "contact-me" message sent to web admin via respective UI form.
 * 
 * @author Michal Václavek
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name="contact_me_message", schema="coffeecompass")
public class ContactMeMessage implements Serializable {

	private static final long serialVersionUID = -2996183922875213246L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
	
    @Column(name="author_name", length = 50, nullable = false)
    private String authorName;
	  
	@NotNull
	@Column(name = "created_at") 
	private LocalDateTime createdTime;
	
    @Column(name="author_email", length = 60)
    private String email;
	 
    @Column(name="message", length = 512, nullable = false)
    private String textOfMessage;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ContactMeMessage that = (ContactMeMessage) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
