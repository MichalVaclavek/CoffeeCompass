package cz.fungisoft.coffeecompass.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * Trida pro prenos informace o 1 Commentu k jednomu CoffeeSitu od jednomu uzivatele<br>
 * Pro mapovani na tuto tridu se pouziva zakladni trida/entita Comment<br>
 * Tento objekt je obohacen napr. o informaci o tom, jestli lze dany komentar, nacteny z DB, v aktualnim kontextu smazat nebo ne.<br>
 * Tato trida je pouzita nasledne CoffeeSiteControllerem pro zobrazeni detailnich informaci o CoffeeSitu, kdy pripoji<br>
 * prislusne komentar.
 * 
 * @author Michal Vaclavek
 */
@Data
public class CommentDTO
{
    private Integer id;
    
    private String text;
    
    @JsonFormat(pattern = "dd. MM. yyyy HH:mm")
    private Date created;
    
    private Integer coffeeSiteID;
            
    private String userName;
    
    private boolean canBeDeleted = false; // default value
}
