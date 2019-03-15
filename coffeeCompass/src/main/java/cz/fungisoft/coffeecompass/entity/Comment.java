package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * Class representing User's comment to the CoffeeSite.
 * 
 * @author Michal Václavek
 */
@Data
@Entity
@Table(name="comment", schema="coffeecompass")
public class Comment implements Serializable
{
	private static final long serialVersionUID = -4668072504757454270L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
    @NotNull
	@Column(name="text", nullable=false)
	private String text;
	
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created", nullable = false)
	private Date created;
	
    @NotNull
    @ManyToOne // jde o pohled ze strany Comment, tedy Many Comments To One CoffeeSite
    @JoinColumn(name = "site_id", nullable = false)
    private CoffeeSite coffeeSite;
	    	
    @NotNull
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
