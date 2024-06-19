package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Class representing User's comment to the CoffeeSite.
 * 
 * @author Michal Václavek
 */
@Data
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="comment", schema="coffeecompass")
public class Comment implements Serializable {

	private static final long serialVersionUID = -4668072504757454270L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
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
}
