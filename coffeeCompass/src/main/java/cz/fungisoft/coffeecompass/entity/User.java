package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

 
/**
 * Zakladni trida/entita/model pro uchovani udaju o uzivateli.
 * 
 * @author Michal Vaclavek
 *
 */
@Entity
@Table(name="user", schema="coffeecompass")
public class User implements Serializable
{ 
    private static final long serialVersionUID = -9006499187256143209L;

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;
 
    @NotNull
    @Column(name="username", unique=true, nullable=false)
    private String userName;
     
    @NotNull
    @Column(name="passwd", nullable=false)
    private String password;
         
    @Column(name="first_name")
    private String firstName;
 
    @Column(name="last_name")
    private String lastName;
 
    @Column(name="email", nullable=true)
    private String email;
 
    @NotNull
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_to_user_profile", schema = "coffeecompass",
               joinColumns = { @JoinColumn(name = "user_id") }, 
               inverseJoinColumns = { @JoinColumn(name = "user_profile_id") })
    private Set<UserProfile> userProfiles = new HashSet<UserProfile>();
 
    @NotNull
    @Column(name="created_on")
    private Timestamp createdOn;
       
    @Column(name="updated_on")
    private Timestamp updatedOn;
       
    @Column(name="created_sites")
    private Integer createdSites;
        
    @Column(name="updated_sites")
    private Integer updatedSites;
    
    @Column(name="deleted_sites")
    private Integer deletedSites;
    
    /*
    @OneToMany(mappedBy="user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;
    */
    
    public Integer getId() {
        return id;
    }
 
    public void setId(Integer id) {
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
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }
 
    @Override
    public String toString() {
        return "User [id=" + id + ", ssoId=" + userName 
                + ", firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + "]";
    }
    
    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }
  
    public Timestamp getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Timestamp updatedOn) {
        this.updatedOn = updatedOn;
    }
    
    public Integer getCreatedSites() {
        return createdSites;
    }

    public void setCreatedSites(Integer created_sites) {
        this.createdSites = created_sites;
    }

    public Integer getUpdatedSites() {
        return updatedSites;
    }

    public void setUpdatedSites(Integer updated_sites) {
        this.updatedSites = updated_sites;
    }

    public Integer getDeletedSites() {
        return deletedSites;
    }

    public void setDeletedSites(Integer deleted_sites) {
        this.deletedSites = deleted_sites;
    }
}