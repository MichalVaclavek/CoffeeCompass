package cz.fungisoft.coffeecompass.entity;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * Class to hold info about tokens assigned by FIrebase to user's device.
 * This info is obtained from user's mobile device when requesting topic
 * to subscribe.
 * 
 * @author Michal V.
 *
 */
@Data
@Entity
@Table(name="user_firebase_token", schema="coffeecompass")
public class DeviceFirebaseToken {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    
    @NotNull
    @Column(name="firebase_token", nullable=false)
    private String firebaseToken;
    
    /**
     * Can be with or without user id. Firebase token is aassigned when the mob. application is initialized,
     * thou the user may not be logged-in
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = true) 
    private User user;
    
    // Set of topics ordered/subscribed by this Firebase token/user
    @ManyToMany(fetch= FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(name = "subscription_token_to_topic", schema="coffeecompass",
                    joinColumns = { @JoinColumn(name = "firebase_token_id") }, 
                       inverseJoinColumns = { @JoinColumn(name = "firebase_topic_id") })
    private Set<FirebaseTopic> topics = new HashSet<>();
    
    /**
     * Default consctructor needed for Hibernate
     */
    public DeviceFirebaseToken() {
    }
    
    public DeviceFirebaseToken(String token) {
        this.firebaseToken = token;
    }
    
    public DeviceFirebaseToken(String token, User user) {
        this(token);
        this.user = user;
    }
}
