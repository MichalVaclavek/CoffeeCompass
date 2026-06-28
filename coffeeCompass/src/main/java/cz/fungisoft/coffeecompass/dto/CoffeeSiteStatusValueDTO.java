package cz.fungisoft.coffeecompass.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Jedna mozna hodnota provozniho statusu CoffeeSitu (statusZarizeni), kterou lze nastavit.<br>
 * Obsahuje nazev enum hodnoty (pouzitelny jako hodnota parametru pri volani endpointu pro zmenu
 * statusu) a lokalizovane popisky v cestine a anglictine.
 *
 * @author Michal Vaclavek
 */
@Data
@AllArgsConstructor
public class CoffeeSiteStatusValueDTO {

    /** Nazev enum hodnoty, napr. "INSERVICE". Pouziva se jako hodnota parametru 'status' pri zmene statusu. */
    private String status;

    /** Cesky popisek, napr. "V provozu". */
    private String valueCz;

    /** Anglicky popisek, napr. "In service". */
    private String valueEn;
}
