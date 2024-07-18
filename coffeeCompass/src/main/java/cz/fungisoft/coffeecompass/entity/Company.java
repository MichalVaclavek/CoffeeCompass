/**
 * 
 */
package cz.fungisoft.coffeecompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Objects;

/**
 * Jmeno dodavatele automatu nebo jmeno podniku.
 * 
 * @author Michal Vaclavek
 *
 */
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@jakarta.persistence.Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="dodavatel_nebo_jmeno_podniku", schema="coffeecompass")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Company company = (Company) o;
        return id != null && Objects.equals(id, company.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
