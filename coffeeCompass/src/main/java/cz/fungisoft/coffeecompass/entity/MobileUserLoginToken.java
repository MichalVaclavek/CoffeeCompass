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
 * Entity holding token String, User, Device ID and expiryDate for validating
 * user, who is loging-in from mobile app. 
 * 
 * @author Michal Vaclavek
 *
 */
@Entity
@Table(name="mobile_user_login_token", schema = "coffeecompass")
public class MobileUserLoginToken
{
    private static final int EXPIRATION_MINUTES = 60 * 168; // 168 hours as default, i.e. one week
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
     
    @NotNull
    @Column(name="token", nullable=false)
    private String token;
    
    @NotNull
    @Column(name="device_id", nullable=false)
    private String deviceId;
    
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    
    @Column(name="expiry_date")
    private Date expiryDate;
    
    
    /**
     * Default contructor, required by Hibernate
     */
    public MobileUserLoginToken() {
    }


    /**
     * Standard constructor
     * 
     * @param token2
     * @param user2
     */
    public MobileUserLoginToken(String token, String deviceId, User user) {
        this.token = token;
        this.user = user;
        this.deviceId = deviceId;
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
    
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceID) {
        this.deviceId = deviceID;
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
