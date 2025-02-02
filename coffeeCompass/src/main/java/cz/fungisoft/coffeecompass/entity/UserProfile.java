package cz.fungisoft.coffeecompass.entity;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name="user_profile", schema = "coffeecompass")
public class UserProfile extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 5515645036753999883L;

    @Column(name="type", length = 15, unique = true, nullable = false)
    private String type = UserProfileTypeEnum.USER.getUserProfileType();
     
    @Override
    public String toString() {
        return type;
    }
}