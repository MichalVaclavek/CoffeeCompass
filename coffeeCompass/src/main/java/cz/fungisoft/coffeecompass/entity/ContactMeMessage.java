/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * Class representing a "contact-me" message sent to web admin via respective UI form.
 * 
 * @author Michal Václavek
 */
@Data
@Entity
@Table(name="contact_me_message", schema="coffeecompass")
public class ContactMeMessage implements Serializable
{
	private static final long serialVersionUID = -2996183922875213246L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
	
    @Column(name="author_name", length = 50, nullable = false)
    private String authorName;
	  
	@NotNull
	@Column(name = "created_at") 
	private Timestamp createdTime;
	
    @Column(name="author_email", length = 60)
    private String email;
	 
    @Column(name="message", length = 512, nullable = false)
    private String textOfMessage;

}
