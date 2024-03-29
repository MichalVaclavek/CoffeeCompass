/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Jmeno dodavatele automatu nebo jmeno podniku.
 * 
 * @author Michal Vaclavek
 *
 */
@Data
@Entity
@javax.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="dodavatel_nebo_jmeno_podniku", schema="coffeecompass")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Size(max = 85) // Validace vstupu, pocet znaku
    @Column(name = "jmeno_podniku_dodavatele", unique = true)
    private String nameOfCompany;
    
    public void setNameOfCompany(String dodavatelPodnik) {
        this.nameOfCompany = dodavatelPodnik.trim();
    }
    
    @Override
    public String toString() {
        return nameOfCompany;
    }
}
