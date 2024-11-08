package cz.fungisoft.coffeecompass.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * 
 * 
 * @author Michal Václavek, by Baeldung.com 
 *
 */
@Entity
@Table(name="password_reset_token", schema = "coffeecompass")
public class PasswordResetToken {
  
    private static final int EXPIRATION_MINUTES = 60; // 1 hour validity
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
  
    @NotNull
    @Column(name="token", nullable=false)
    private String token;
  
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "uuid_user")
    private User user;
  
    @Column(name="expiry_date")
    private LocalDateTime expiryDate;
    

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


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }


    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    private LocalDateTime calculateExpiryDate(int expiryTimeInMinutes) {
        
        if (expiryTimeInMinutes < 1) {
            expiryTimeInMinutes = EXPIRATION_MINUTES;
        }
        return LocalDateTime.now().plus(expiryTimeInMinutes, ChronoUnit.MINUTES);
    }
}
