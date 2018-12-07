/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

/**
 * Trida/objekt pro hodncoeni daneho CoffeeSite jednim Userem. Reprezentuje jeden radek
 * z tabulky "hodnoceni".
 * 
 * @author Michal Vaclavek
 */
@Data
@Entity
@Table(name="hodnoceni", schema="coffeecompass")
public class StarsForCoffeeSiteAndUser
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToOne
    @JoinColumn(name = "coffeesite_id")
    private CoffeeSite site;
    
    @OneToOne
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
        this.site = coffeeSite;
        this.user = user;
        this.stars = stars;
    }
}