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
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * Typ lokality kde se vyskytuje CoffeeSite. Napr. nadrazi, obchodni centrum atp.
 * 
 * @author Michal Václavek
 */
@Data
@Entity
@Table(name="typ_lokality", schema="coffeecompass")
public class SiteLocationType
{
    /* ======= INSTANCES VARIABLES ======== */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @NotNull // Validace vstupu, nesmi byt null
    @Column(name = "lokalita", unique=true, length=55)
    private String locationType;
    
    @Override
    public String toString() {
        return locationType;
    }
}
