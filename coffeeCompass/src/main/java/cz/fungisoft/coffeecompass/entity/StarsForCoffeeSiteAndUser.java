package cz.fungisoft.coffeecompass.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Trida/objekt pro hodncoeni daneho CoffeeSite jednim Userem. Reprezentuje jeden radek
 * z tabulky "hodnoceni".
 * 
 * @author Michal Vaclavek
 */
@Data
@Entity
@javax.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="hodnoceni", schema="coffeecompass")
public class StarsForCoffeeSiteAndUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

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
}