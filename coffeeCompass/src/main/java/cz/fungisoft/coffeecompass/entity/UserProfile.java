package cz.fungisoft.coffeecompass.entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
 
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="user_profile", schema = "coffeecompass")
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 5515645036753999883L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; 
 
    @Column(name="type", length = 15, unique = true, nullable = false)
    private String type = UserProfileTypeEnum.USER.getUserProfileType();
     
    public UserProfile() {}
    
    public UserProfile(Integer id, String type) {
        this.id = id;
        this.type = type;
    }
    
    public Integer getId() {
        return id;
    }
 
    public void setId(Integer id) {
        this.id = id;
    }
 
    public String getType() {
        return type;
    }
 
    public void setType(String type) {
        this.type = type;
    }
 
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }
 
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof UserProfile other))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (type == null) {
            return other.type == null;
        } else return type.equals(other.type);
    }
 
    @Override
    public String toString() {
        return type;
    }
}