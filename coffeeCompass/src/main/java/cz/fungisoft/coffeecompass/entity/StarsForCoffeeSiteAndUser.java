package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Objects;

/**
 * Trida/objekt pro hodncoeni daneho CoffeeSite jednim Userem. Reprezentuje jeden radek
 * z tabulky "hodnoceni".
 * 
 * @author Michal Vaclavek
 */
@Getter
@Setter
@ToString
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="hodnoceni", schema="coffeecompass")
public class StarsForCoffeeSiteAndUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @NotNull
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    @JoinColumn(name = "coffeesite_id", nullable = false)
    private CoffeeSite coffeeSite;
    
    @OneToOne
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinColumn(name = "stars_id")
    private StarsQualityDescription stars;
    
    /**
     * Default konstruktor
     */
    public StarsForCoffeeSiteAndUser() {
    }
    
    /**
     * Konstruktor. Obvykle je k dispozici CoffeeSite a User ktery ho hodnoti a pocet hvezdicek.
     * 
     * @param coffeeSite
     * @param user
     * @param stars
     */
    public StarsForCoffeeSiteAndUser(CoffeeSite coffeeSite, User user, StarsQualityDescription stars) {
        this.coffeeSite = coffeeSite;
        this.user = user;
        this.stars = stars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        StarsForCoffeeSiteAndUser that = (StarsForCoffeeSiteAndUser) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}