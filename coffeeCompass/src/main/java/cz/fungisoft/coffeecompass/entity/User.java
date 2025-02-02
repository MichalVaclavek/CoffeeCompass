package cz.fungisoft.coffeecompass.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import cz.fungisoft.coffeecompass.controller.models.AuthProviders;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


/**
 * Zakladni trida/entita/model pro uchovani udaju o uzivateli.
 * 
 * @author Michal Vaclavek
 */
@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="user", schema="coffeecompass")
public class User extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -9006499187256143209L;

    @NotNull
    @Column(name="username", unique=true, nullable=false)
    private String userName;
     
    @Column(name="passwd") // can be empty or null for user's logedin via social network oauth2 providers
    private String password;
         
    @Column(name="first_name")
    private String firstName;
 
    @Column(name="last_name")
    private String lastName;
 
    @Column(name="email")
    private String email;
 
    @NotNull
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_to_user_profile", schema = "coffeecompass",
               joinColumns = { @JoinColumn(name = "user_id") }, 
               inverseJoinColumns = { @JoinColumn(name = "user_profile_id") })
    private Set<UserProfile> userProfiles = new HashSet<>();
 
    @NotNull
    @Column(name="created_on")
    private LocalDateTime createdOn;
       
    @Column(name="updated_on")
    private LocalDateTime updatedOn;
       
    @Column(name="created_sites")
    private Integer createdSites;
        
    @Column(name="updated_sites")
    private Integer updatedSites;
    
    @Column(name="deleted_sites")
    private Integer deletedSites;
    
    @Column(name = "registration_email_confirmed")
    private boolean registerEmailConfirmed = false;
    
    @NotNull
    @Enumerated // V DB je cislovano od indexu 0, protoze i enum type se v defaultnim pripade cisluje od 0.
    @Column(name="auth_provider_id", columnDefinition = "smallint")
    private AuthProviders authProvider = AuthProviders.LOCAL; // default value for new user
    
    @Column(name = "banned")
    private boolean banned = false;
    
    @Column(name = "enabled")
    private boolean enabled = false;

    @Override
    public String toString() {
        return "User [id=" + id + ", ssoId=" + userName
                + ", firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + "]";
    }

    public boolean isRegisterEmailConfirmed() {
        return this.registerEmailConfirmed;
    }

}