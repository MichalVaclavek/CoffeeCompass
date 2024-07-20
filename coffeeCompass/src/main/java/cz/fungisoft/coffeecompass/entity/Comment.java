package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Class representing User's comment to the CoffeeSite.
 * 
 * @author Michal Václavek
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name="comment", schema="coffeecompass")
public class Comment implements Serializable {

	private static final long serialVersionUID = -4668072504757454270L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
    @NotNull
	@Column(name="text", nullable=false)
	private String text;
	
    @NotNull
	@Column(name = "created", nullable = false)
	private LocalDateTime created;
	
    @NotNull
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne // jde o pohled ze strany Comment, tedy Many Comments To One CoffeeSite
    @JoinColumn(name = "site_id", nullable = false)
    private CoffeeSite coffeeSite;
	    	
    @NotNull
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Comment comment = (Comment) o;
        return id != null && Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
