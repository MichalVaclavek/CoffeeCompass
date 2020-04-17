package cz.fungisoft.coffeecompass.controller.rest.secured;


import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.exceptions.rest.BadRESTRequestException;
import cz.fungisoft.coffeecompass.exceptions.rest.InvalidParameterValueException;
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
public class CoffeeSiteControllerSecuredREST
{
    private static final Logger log = LoggerFactory.getLogger(CoffeeSiteControllerSecuredREST.class);
    
    private CoffeeSiteService coffeeSiteService;
    
    private MessageSource messages;
    
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
    public CoffeeSiteControllerSecuredREST(CoffeeSiteService coffeeSiteService, MessageSource messages) {
        super();
        this.coffeeSiteService = coffeeSiteService;
        this.messages = messages;
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
    public ResponseEntity<CoffeeSiteDTO> insert(@Valid @RequestBody CoffeeSiteDTO coffeeSite, UriComponentsBuilder ucBuilder, Locale locale) {
    
       CoffeeSite cs = coffeeSiteService.save(coffeeSite);
       
       HttpHeaders headers = new HttpHeaders();
       if (cs != null) {
           log.info("New Coffee site created.");
           headers.setLocation(ucBuilder.path("/rest/site/{id}").buildAndExpand(cs.getId()).toUri());
           CoffeeSiteDTO csDTO = coffeeSiteService.findOneToTransfer(cs.getId());
           
           return (csDTO == null) ? new ResponseEntity<CoffeeSiteDTO>(HttpStatus.BAD_REQUEST)
                                  : new ResponseEntity<CoffeeSiteDTO>(csDTO, HttpStatus.CREATED);
       }
       else {
           log.error("Coffee site creation failed");
           //headers.setLocation(ucBuilder.path("/rest/site/create").buildAndExpand(coffeeSite.getId()).toUri());
           //return new ResponseEntity<Long>(0L, headers, HttpStatus.BAD_REQUEST); "Chyba při vytváření lokace"
           throw new BadRESTRequestException(messages.getMessage("coffeesite.create.rest.error.general", null, locale));
       }
    }
    

    @PutMapping("/update/{id}") // Mapovani http PUT na DB operaci UPDATE tj. zmena zaznamu c. id polozkou coffeeSite, napr. http://localhost:8080/rest/secured/site/update/2
    public ResponseEntity<CoffeeSiteDTO> updateRest(@PathVariable Long id, @Valid @RequestBody CoffeeSiteDTO coffeeSite, UriComponentsBuilder ucBuilder, Locale locale) {
        
        coffeeSite.setId(id);
        
        CoffeeSite cs = coffeeSiteService.updateSite(coffeeSite);
        if (cs != null) {
            log.info("Coffee site update successful.");
            CoffeeSiteDTO csDTO = coffeeSiteService.findOneToTransfer(cs.getId());
            
            return (csDTO == null) ? new ResponseEntity<CoffeeSiteDTO>(HttpStatus.NOT_FOUND)
                                   : new ResponseEntity<CoffeeSiteDTO>(csDTO, HttpStatus.CREATED);
            
        } else {
            log.error("Coffee site update failed." + coffeeSite.getId());
            //throw new ResourceNotFoundException("CoffeeSite", "siteId", id);
            throw new BadRESTRequestException(messages.getMessage("coffeesite.update.rest.error", null, locale));
        }
    }
    
    /**
     * Smazani CoffeeSite daneho id
     * 
     * @param id
     */
    @DeleteMapping("/delete/{id}") // Mapovani http DELETE na DB operaci delete, napr. http://localhost:8080/rest/secured/site/delete/2
    public ResponseEntity<Long> delete(@PathVariable Long id, Locale locale) {
        try {
            coffeeSiteService.delete(id);
            return new ResponseEntity<Long>(id, HttpStatus.OK);
        } catch (Exception ex) {
            //return new ResponseEntity<Long>(id, HttpStatus.BAD_REQUEST);
            throw new BadRESTRequestException(messages.getMessage("coffeesite.delete.rest.error", null, locale));
        }
    }
    
    /**
     *  Zpracovani pozadavku na zmenu stavu CoffeeSite do stavu ACTIVE<br>
     *  Pred zmenou do ACTIVE stavu je ptreba zkontrolovat, jestli na dane pozici neni jiny jiz ACTIVE CoffeeSite
     */
    @PutMapping("/{id}/activate") 
    public ResponseEntity<CoffeeSiteDTO> activateCoffeeSite(@PathVariable(name = "id") Long id, UriComponentsBuilder ucBuilder, Locale locale) {
        CoffeeSite cs = coffeeSiteService.findOneById(id);
        if (coffeeSiteService.isLocationAlreadyOccupiedByActiveSite(cs.getZemSirka(), cs.getZemDelka(), 5, cs.getId())) {
            //new ResponseEntity<Long>(0L, HttpStatus.BAD_REQUEST);
            throw new InvalidParameterValueException("CoffeeSite", "latitude/longitude", cs.getZemSirka(), messages.getMessage("coffeesite.create.wrong.location.rest.error", null, locale));
        }
        return modifyStatus(id, CoffeeSiteRecordStatusEnum.ACTIVE, ucBuilder, locale);
    }
    
    /**
     *  Zpracovani pozadavku na zmenu stavu CoffeeSite do stavu INACTIVE<br>
     */
    @PutMapping("/{id}/deactivate") 
    public ResponseEntity<CoffeeSiteDTO> deactivateCoffeeSite(@PathVariable(name = "id") Long id, UriComponentsBuilder ucBuilder, Locale locale) {
        return modifyStatus(id, CoffeeSiteRecordStatusEnum.INACTIVE, ucBuilder, locale);
    }

    /**
     *  Zpracovani pozadavku na zmenu stavu CoffeeSite do stavu CANCELED<br>
     */
    @PutMapping("/{id}/cancel") 
    public ResponseEntity<CoffeeSiteDTO> cancelStatusSite(@PathVariable(name = "id") Long id, UriComponentsBuilder ucBuilder, Locale locale) {
        return modifyStatus(id, CoffeeSiteRecordStatusEnum.CANCELED, ucBuilder, locale);
    }

    
    /**
     * Pomocna metoda zdruzujici Controller prikazy pro některé modifikace stavu CoffeeSitu
     */
    private ResponseEntity<CoffeeSiteDTO> modifyStatus(Long csID, CoffeeSiteRecordStatusEnum newStatus, UriComponentsBuilder ucBuilder, Locale locale) {
        CoffeeSite cs = coffeeSiteService.findOneById(csID);
        cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, newStatus);
        if (cs == null) {
            throw new BadRESTRequestException(messages.getMessage("coffeesite.status.change.rest.error", null, locale));    
        } else {
            HttpHeaders headers = new HttpHeaders();
            log.info("CoffeeSite's status modified. New status: " + newStatus.getSiteRecordStatus());
            headers.setLocation(ucBuilder.path("/rest/site/{id}").buildAndExpand(cs.getId()).toUri());
            CoffeeSiteDTO csDTO = coffeeSiteService.findOneToTransfer(csID);
            
            return (csDTO == null) ? new ResponseEntity<CoffeeSiteDTO>(HttpStatus.NOT_FOUND)
                                   : new ResponseEntity<CoffeeSiteDTO>(csDTO, HttpStatus.OK);
        }
    }
    
    /**
     * Method to handle request to show CoffeeSites created by logged in user.
     * 
     * @return
     */
    @GetMapping("/mySites") // napr. https://coffeecompass.cz/rest/secured/site/mySites
    public ResponseEntity<List<CoffeeSiteDTO>> showMySites() {
        List<CoffeeSiteDTO> coffeeSites = coffeeSiteService.findAllFromLoggedInUser();
        
        if (coffeeSites == null || coffeeSites.size() == 0) {
            log.error("No Coffee site from user found.");
            return new ResponseEntity<List<CoffeeSiteDTO>>(HttpStatus.NOT_FOUND);
        } 
        
        log.info("All sites from logged-in user retrieved.");
        return new ResponseEntity<List<CoffeeSiteDTO>>(coffeeSites, HttpStatus.OK);   
    }
    
    /**
     * Method to handle request to get number of all CoffeeSites created by logged in user.
     * 
     * @return
     */
    @GetMapping("/mySitesNumber") // napr. https://coffeecompass.cz/rest/secured/site/mySitesNumber
    public ResponseEntity<Integer> getNumberOfMySites() {
        Integer numberOfSitesFromUser = coffeeSiteService.getNumberOfSitesFromLoggedInUser();
        log.info("Number of sites from logged-in user retrieved.");
        return (numberOfSitesFromUser != null)
                ? new ResponseEntity<Integer>(numberOfSitesFromUser, HttpStatus.OK)
                : new ResponseEntity<Integer>(0, HttpStatus.NOT_FOUND);
        
    }
    
    /**
     * Method to handle request to get number of all CoffeeSites created by logged in user,
     * which are not in CANCELED state.
     * 
     * @return
     */
    @GetMapping("/mySitesNotCanceledNumber") // napr. https://coffeecompass.cz/rest/secured/site/mySitesNotCanceledNumber
    public ResponseEntity<Integer> getNumberOfMySitesNotCanceled() {
        Integer numberOfSitesFromUser = coffeeSiteService.getNumberOfSitesNotCanceledFromLoggedInUser();
        log.info("Number of not canceled sites from logged-in user retrieved.");
        return (numberOfSitesFromUser != null)
                ? new ResponseEntity<Integer>(numberOfSitesFromUser, HttpStatus.OK)
                : new ResponseEntity<Integer>(0, HttpStatus.NOT_FOUND);
        
    }
    
}
