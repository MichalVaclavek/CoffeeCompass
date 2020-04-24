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
 * Entity holding token String, User, and expiryDate for verification of the user's 
 * email address. 
 * 
 * @author Michal Vaclavek
 *
 */
@Entity
@Table(name="user_verification_token", schema = "coffeecompass")
public class UserEmailVerificationToken
{
    private static final int EXPIRATION_MINUTES = 60 * 24; // 24 hours as default
    
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
    public UserEmailVerificationToken() {
    }


    /**
     * Standard constructor
     * 
     * @param token2
     * @param user2
     */
    public UserEmailVerificationToken(String token2, User user2) {
        this.token = token2;
        this.user = user2;
        setExpiryDate(calculateExpiryDate(EXPIRATION_MINUTES)); // default expiry date
    }

    
    /* Getters and Seters */
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
