package cz.fungisoft.coffeecompass.controller.rest.secured;


import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import io.swagger.annotations.Api;

/**
 * Třída/kontroler, který lze použit v případě využití REST rozhraní<br>
 * 
 * Základní Controller pro obsluhu požadavků, které se týkají práce s hlavním objektem CoffeeSite.<br>
 * Tj. pro základní CRUD operace a pro vyhledávání CoffeeSites.<br>
 * Tato verze je urcena pro REST, pro testovaci/prototypovaci ucely je urcena verze CoffeeSiteController, ktera vyuziva system/framework Thymeleaf
 * <br>
 * @author Michal Václavek
 *
 */
@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/rest/secured/site") // vsechny http dotazy v kontroleru maji zacinat timto retezcem
//@RequiredArgsConstructor //TODO dodelat s pouzitim lombok, bez Autowired na fields
public class CoffeeSiteControllerSecuredREST
{
    private static final Logger log = LoggerFactory.getLogger(CoffeeSiteControllerSecuredREST.class);
    
    private CoffeeSiteService coffeeSiteService;
    
    /**
     * Dependency Injection pomoci konstruktoru, neni potreba uvadet @Autowired u atributu, Spring toto umi automaticky.<br>
     * Lze ale uvest u konstruktoru, aby bylo jasne, ze Injection provede Spring.
     * <p>
     * Ale protoze techto servicu bude v tomto Controleru mnoho, bude konstruktor obsahovat pouze jeden<br>
     * parametr se zakladnim Servicem CoffeeSiteService.<br>
     * Ostatni service budou @Autowired jako atributy instance, Spring zaridi injection
     * 
     * @param coffeeSiteService
     */
    @Autowired
    public CoffeeSiteControllerSecuredREST(CoffeeSiteService coffeeSiteService) {
        super();
        this.coffeeSiteService = coffeeSiteService;
    }

   
    /**
     * Obsluha POST pozadavku ze stranky obsahujici formular pro vytvoreni CoffeeSite.
     * 
     * @Valid zajisti, ze se pred zavolanim metody zvaliduje Coffee Site podle limitu, ktere jsou u atributu coffeeSite
     *
     * @param coffeeSite
     * @return
     */
    @PostMapping("/create") // Mapovani http POST na DB save/INSERT
    public ResponseEntity<Long> insert(@Valid @RequestBody CoffeeSiteDTO coffeeSite, UriComponentsBuilder ucBuilder) {
    
       CoffeeSite cs = coffeeSiteService.save(coffeeSite);
       
       HttpHeaders headers = new HttpHeaders();
       if (cs != null) {
           log.info("New Coffee site created.");
           headers.setLocation(ucBuilder.path("/rest/site/{id}").buildAndExpand(cs.getId()).toUri());
           return new ResponseEntity<Long>(cs.getId(), headers, HttpStatus.CREATED);
       }
       else {
           log.error("Coffee site creation failed");
           headers.setLocation(ucBuilder.path("/rest/site/create").buildAndExpand(coffeeSite.getId()).toUri());
           return new ResponseEntity<Long>(0L, headers, HttpStatus.BAD_REQUEST);
       }
    }
    

    @PutMapping("/update/{id}") // Mapovani http PUT na DB operaci UPDATE tj. zmena zaznamu c. id polozkou coffeeSite, napr. http://localhost:8080/rest/secured/site/update/2
    public ResponseEntity<Long> updateRest(@PathVariable Long id, @Valid @RequestBody CoffeeSiteDTO coffeeSite) {
        coffeeSite.setId(id);
        
        //CoffeeSite cs = coffeeSiteService.save(coffeeSite);
        CoffeeSite cs = coffeeSiteService.updateSite(coffeeSite);
        if (cs != null) {
            log.info("Coffee site update successful.");
            //cs = coffeeSiteService.findOneToTransfer(id);
        } else {
            log.error("Coffee site update failed." + coffeeSite.getId());
        }
        
        return (cs == null) ? new ResponseEntity<Long>(0L, HttpStatus.BAD_REQUEST)
                            : new ResponseEntity<Long>(cs.getId(), HttpStatus.CREATED);
    }
    
    /**
     * Smazani CoffeeSite daneho id
     * 
     * @param id
     */
    @DeleteMapping("/delete/{id}") // Mapovani http DELETE na DB operaci delete, napr. http://localhost:8080/rest/secured/site/delete/2
    public ResponseEntity<CoffeeSiteDTO> delete(@PathVariable Long id) {
        coffeeSiteService.delete(id);
        return new ResponseEntity<CoffeeSiteDTO>(HttpStatus.NO_CONTENT);
    }
    
}
