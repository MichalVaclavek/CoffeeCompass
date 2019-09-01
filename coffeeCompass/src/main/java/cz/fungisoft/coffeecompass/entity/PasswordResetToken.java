/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author Michal Václavek, by Baeldung.com 
 *
 */
@Entity
@Table(name="password_reset_token", schema = "coffeecompass")
public class PasswordResetToken {
  
    private static final int EXPIRATION_MINUTES = 60 * 1;
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  
    @NotNull
    @Column(name="token", nullable=false)
    private String token;
  
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
  
    @Column(name="expiry_date")
    private Date expiryDate;
    

    /**
     * Default contructor, required by Hibernate
     */
    public PasswordResetToken() {
    }


    /**
     * Standard constructor
     * 
     * @param token2
     * @param user2
     */
    public PasswordResetToken(String token2, User user2) {
        this.token = token2;
        this.user = user2;
        setExpiryDate(calculateExpiryDate(EXPIRATION_MINUTES)); // default expiry date
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }


    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        
        if (expiryTimeInMinutes < 1) {
            expiryTimeInMinutes = EXPIRATION_MINUTES;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
    
}
