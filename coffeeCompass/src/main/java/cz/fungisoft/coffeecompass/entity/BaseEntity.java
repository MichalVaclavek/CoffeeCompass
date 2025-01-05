package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {

    public BaseEntity(UUID id) {
        this.id = id;
    }

    @Id
    @Column(name="external_id", length = 36, updatable = false, nullable = false)
    @GeneratedValue
    protected UUID id;


    public boolean isNew() {
        return this.id == null;
    }
}
