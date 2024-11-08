package cz.fungisoft.coffeecompass.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Trida pro prenos informace o 1 Commentu k jednomu CoffeeSitu od jednomu uzivatele<br>
 * Pro mapovani na tuto tridu se pouziva zakladni trida/entita Comment<br>
 * Tento objekt je obohacen napr. o informaci o tom, jestli lze dany komentar, nacteny z DB, v aktualnim kontextu smazat nebo ne.<br>
 * Tato trida je pouzita nasledne CoffeeSiteControllerem pro zobrazeni detailnich informaci o CoffeeSitu, kdy pripoji<br>
 * prislusny komentar.<br>
 * Obsahuje i hodnoceni lokace v podobe poctu Stars, ktery uzivatel k dane lokaci vlozil. Vyuzije se k zobrazovani hodnoceni
 * daneho uzivatele spolu s jeho komentarem.
 * 
 * @author Michal Vaclavek
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentDTO extends BaseItem {
    
    @Size(max=512)
    private String text;
    
    public void setText(String text) {
        this.text = text.trim();
    }
    
    @JsonFormat(pattern = "dd.MM. yyyy HH:mm", timezone="Europe/Prague")
    private LocalDateTime created;
    
    private UUID coffeeSiteId;
            
    private String userName;
    
    // to allow better mapping from Comment to CommentDTO, which included starsFromUser
    // its easier to serach fro user by its id
    private UUID userId;
    
    private boolean canBeDeleted = false; // default value
    
    private int starsFromUser = 0; // default means no stars entered
}
