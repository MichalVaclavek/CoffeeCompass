package cz.fungisoft.coffeecompass.entity;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;

@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name="user_profile", schema = "coffeecompass")
public class UserProfile extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 5515645036753999883L;

//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name="id")
//    private Integer longId;
 
    @Column(name="type", length = 15, unique = true, nullable = false)
    private String type = UserProfileTypeEnum.USER.getUserProfileType();
     
    public UserProfile() {}


//    public UserProfile(Integer id, String type) {
//        this.longId = id;
//        this.type = type;
//    }
    
//    public Integer getLongId() {
//        return longId;
//    }
//
//    public void setLongId(Integer id) {
//        this.longId = id;
//    }
 
    public String getType() {
        return type;
    }
 
    public void setType(String type) {
        this.type = type;
    }
 
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((id == null) ? 0 : id.hashCode());
//        result = prime * result + ((type == null) ? 0 : type.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (!(obj instanceof UserProfile other))
//            return false;
//        if (id == null) {
//            if (other.id != null)
//                return false;
//        } else if (!id.equals(other.id))
//            return false;
//        if (type == null) {
//            return other.type == null;
//        } else return type.equals(other.type);
//    }
 
    @Override
    public String toString() {
        return type;
    }
}