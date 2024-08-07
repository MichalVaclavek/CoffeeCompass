package cz.fungisoft.users.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


/**
 * Zakladni trida/entita/model pro uchovani udaju o uzivateli.
 * 
 * @author Michal Vaclavek
 */
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="user", schema="coffeecompass")
public class User implements Serializable {

    private static final long serialVersionUID = -9006499187256143209L;
    
    public User() {
        super();
        this.registerEmailConfirmed = false;
        this.banned = false;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
 
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
    private boolean registerEmailConfirmed;
    
    @NotNull
    @Enumerated // V DB je cislovano od indexu 0, protoze i enum type se v defaultnim pripade cisluje od 0.
    @Column(name="auth_provider_id", columnDefinition = "smallint")
    private AuthProviders authProvider = AuthProviders.LOCAL; // default value for new user
    
    @Column(name = "banned")
    private boolean banned;
    
    @Column(name = "enabled")
    private boolean enabled = false;
    
    /*
    @OneToMany(mappedBy="user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;
    */
    
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }
 
    public String getUserName(){
        return userName;
    }
 
    public void setUserName(String userName) {
        this.userName = userName;
    }
 
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
 
    public String getFirstName() {
        return firstName;
    }
 
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
 
    public String getLastName() {
        return lastName;
    }
 
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
 
    public String getEmail() {
        return email;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }
 
    public Set<UserProfile> getUserProfiles() {
        return userProfiles;
    }
 
    public void setUserProfiles(Set<UserProfile> userProfiles) {
        this.userProfiles = userProfiles;
    }
 
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }
 
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof User))
            return false;
        
        User other = (User) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        
        if (userName == null) {
            return other.userName == null;
        } else return userName.equals(other.userName);
    }
 
    @Override
    public String toString() {
        return "User [id=" + id + ", ssoId=" + userName 
                + ", firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + "]";
    }
    
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
  
    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
    
    public Integer getCreatedSites() {
        return createdSites;
    }

    public void setCreatedSites(Integer createdSites) {
        this.createdSites = createdSites;
    }

    public Integer getUpdatedSites() {
        return updatedSites;
    }

    public void setUpdatedSites(Integer updatedSites) {
        this.updatedSites = updatedSites;
    }

    public Integer getDeletedSites() {
        return deletedSites;
    }

    public void setDeletedSites(Integer deletedSites) {
        this.deletedSites = deletedSites;
    }
    
    public boolean isRegisterEmailConfirmed() {
        return registerEmailConfirmed;
    }

    public void setRegisterEmailConfirmed(boolean registerEmailConfirmed) {
        this.registerEmailConfirmed = registerEmailConfirmed;
    }
    
    public boolean getRegisterEmailConfirmed() {
        return this.registerEmailConfirmed;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public AuthProviders getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProviders authProvider) {
        this.authProvider = authProvider;
    }
}