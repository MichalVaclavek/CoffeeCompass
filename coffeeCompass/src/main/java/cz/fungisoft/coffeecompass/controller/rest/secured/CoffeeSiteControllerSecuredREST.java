package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import jakarta.validation.Valid;

import cz.fungisoft.coffeecompass.validators.ListInputSize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.exceptions.rest.BadRESTRequestException;
import cz.fungisoft.coffeecompass.exceptions.rest.InvalidParameterValueException;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;

/**
 * Třída/kontroler, který lze použit v případě využití REST rozhraní
 * <p>
 * Základní Controller pro obsluhu požadavků, které se týkají práce s hlavním objektem CoffeeSite.<br>
 * Tj. pro základní CRUD operace a pro vyhledávání CoffeeSites.<br>
 * <br>
 * @author Michal Václavek
 *
 */
@Tag(name = "CoffeeSiteCRUD", description = "Coffee site REST CRUD operations")
@RestController
@Validated
//@RequestMapping("/rest/secured/site")
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/secured/site")
public class CoffeeSiteControllerSecuredREST  {

    private static final Logger log = LoggerFactory.getLogger(CoffeeSiteControllerSecuredREST.class);
    
    private final CoffeeSiteService coffeeSiteService;
    
    private final MessageSource messages;

    private static final String INPUT_SIZE_ERROR_MESSAGE_KEY = "{error.input.coffeesites.list.size}";
    
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
     * Obsluha POST pozadavku pro vytvoreni/vlozeni CoffeeSite do DB.
     * 
     * @Valid zajisti, ze se pred zavolanim metody zvaliduje Coffee Site podle limitu, ktere jsou u atributu coffeeSite
     *
     * @param coffeeSite
     * @return
     */
    @PostMapping("/create") // Mapovani http POST na DB save/INSERT
    public ResponseEntity<CoffeeSiteDTO> insert(@Valid @RequestBody CoffeeSiteDTO coffeeSite, UriComponentsBuilder ucBuilder, Locale locale) {
    
       CoffeeSite cs = coffeeSiteService.save(coffeeSite);
       
       HttpHeaders
               headers = new HttpHeaders();
       if (cs != null) {
           log.info("New Coffee site created.");
           //TODO - why headers?
           headers.setLocation(ucBuilder.path("/rest/site/{id}").buildAndExpand(cs.getId()).toUri());
           return coffeeSiteService.findOneToTransfer(cs.getId()).map(csDTO ->  new ResponseEntity<>(csDTO, HttpStatus.CREATED))
                                                                 .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));

       } else {
           log.error("Coffee site creation failed");
           throw new BadRESTRequestException(messages.getMessage("coffeesite.create.rest.error.general", null, locale));
       }
    }
    
    /**
     * Obsluha POST pozadavku pro vytvoreni vice CoffeeSites v jednom dotazu. Pouzito napr. pri odeslani skupiny CoffeeSites, ktery
     * vytvoril/modifikoval mobilni uzivatel v OFFLINE mode.<br>
     * Obrazky k temto CoffeeSitum lze ukladat postupnym volani {@link ImageControllerSecuredREST#handleFileUpload}
     * 
     * @Valid zajisti, ze se pred zavolanim metody zvaliduje Coffee Site podle limitu, ktere jsou u atributu coffeeSite
     *
     * @param coffeeSites sity k ulozeni
     * @return true if saved successfully
     */
    @PostMapping("/insertCoffeeSites") // Mapovani http POST na DB save/INSERT
    public ResponseEntity<Boolean> insertCoffeeSites(@RequestBody
                                                     @Valid
                                                     @ListInputSize(max = 20)
                                                     List<@Valid CoffeeSiteDTO> coffeeSites, Locale locale) {
    
       if (coffeeSiteService.saveOrUpdate(coffeeSites)) {
           log.info("CoffeeSites inserted.");
           return new ResponseEntity<>(true, HttpStatus.CREATED);
       } else {
           log.error("Coffee sites insertion failed.");
           throw new BadRESTRequestException(messages.getMessage("coffeesite.create.rest.error.general", null, locale));
       }
    }

    /**
     * Obsluha POST pozadavku pro vytvoreni vice CoffeeSites v jednom dotazu. Pouzito napr. pri odeslani skupiny CoffeeSites, ktery
     * vytvoril/modifikoval mobilni uzivatel v OFFLINE mode.<br>
     * V podstate stejne jako zakladni varianta {@link #insertCoffeeSites}, ale tato vraci zpet seznam vytvorenych/updatovanych
     * CoffeeSites.
     *
     * @param coffeeSites sity k ulozeni
     * @return list of saved CoffeeSites if saved successfully or BadRESTRequestException response
     */
    @PostMapping("/insertCoffeeSitesWithResult") // Mapovani http POST na DB save/INSERT
    public ResponseEntity<List<CoffeeSiteDTO>> insertCoffeeSitesAndReturnResult(@RequestBody
                                                                                @Valid
                                                                                @ListInputSize(max = 20)
                                                                                List<@Valid CoffeeSiteDTO> coffeeSites, Locale locale) {

        List<CoffeeSiteDTO> retVal = coffeeSiteService.saveOrUpdateWithResult(coffeeSites);

        if (!retVal.isEmpty()) {
            log.info("CoffeeSites inserted.");
            return new ResponseEntity<>(retVal, HttpStatus.CREATED);
        } else {
            log.error("Coffee sites insertion failed.");
            throw new BadRESTRequestException(messages.getMessage("coffeesite.create.rest.error.general", null, locale));
        }
    }
    

    @PutMapping("/update/{id}") // Mapovani http PUT na DB operaci UPDATE tj. zmena zaznamu c. id polozkou coffeeSite, napr. http://localhost:8080/rest/secured/site/update/2
    public ResponseEntity<CoffeeSiteDTO> updateCoffeeSite(@PathVariable Long id, @Valid @RequestBody CoffeeSiteDTO coffeeSite, Locale locale) {
        
//        coffeeSite.setId(id);
        
        CoffeeSite cs = coffeeSiteService.updateSite(coffeeSite);
        if (cs != null) {
            log.info("Coffee site update successful.");
            return coffeeSiteService.findOneToTransfer(cs.getId()).map(csDTO ->  new ResponseEntity<>(csDTO, HttpStatus.CREATED))
                                                                  .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
            
        } else {
            log.error("Coffee site update failed. Coffee site id {}", id);
            throw new BadRESTRequestException(messages.getMessage("coffeesite.update.rest.error", null, locale));
        }
    }

    @PutMapping("/update/{externalId}") // Mapovani http PUT na DB operaci UPDATE tj. zmena zaznamu c. id polozkou coffeeSite, napr. http://localhost:8080/rest/secured/site/update/2
    public ResponseEntity<CoffeeSiteDTO> updateCoffeeSite(@PathVariable String externalId, @Valid @RequestBody CoffeeSiteDTO coffeeSite, Locale locale) {
        CoffeeSite cs = coffeeSiteService.updateSite(coffeeSite);
        if (cs != null) {
            log.info("Coffee site update successful.");
            return coffeeSiteService.findOneToTransfer(cs.getId()).map(csDTO ->  new ResponseEntity<>(csDTO, HttpStatus.CREATED))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } else {
            log.error("Coffee site update failed. Coffee site external-id {}", externalId);
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
            return new ResponseEntity<>(id, HttpStatus.OK);
        } catch (Exception ex) {
            throw new BadRESTRequestException(messages.getMessage("coffeesite.delete.rest.error", null, locale));
        }
    }
    
    /**
     *  Zpracovani pozadavku na zmenu stavu CoffeeSite do stavu ACTIVE<br>
     *  Pred zmenou do ACTIVE stavu je potreba zkontrolovat, jestli na dane pozici neni jiny jiz ACTIVE CoffeeSite
     */
    @PutMapping("/{id}/activate") 
    public ResponseEntity<CoffeeSiteDTO> activateCoffeeSite(@PathVariable(name = "id") Long id, UriComponentsBuilder ucBuilder, Locale locale) {
        coffeeSiteService.findOneById(id).ifPresent(cs -> {
            if (coffeeSiteService.isLocationAlreadyOccupiedByActiveSite(cs.getZemSirka(), cs.getZemDelka(), 5, cs.getId())) {
                throw new InvalidParameterValueException("CoffeeSite", "latitude/longitude", cs.getZemSirka(), messages.getMessage("coffeesite.create.wrong.location.rest.error", null, locale));
            }
        });
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
        return coffeeSiteService.findOneById(csID).map(cs -> {
            cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, newStatus);
            if (cs == null) {
                throw new BadRESTRequestException(messages.getMessage("coffeesite.status.change.rest.error", null, locale));
            }
            HttpHeaders headers = new HttpHeaders(); //TODO - why headers here?
            log.info("CoffeeSite's status modified. New status: {}", newStatus.getSiteRecordStatus());
            headers.setLocation(ucBuilder.path("/rest/site/{id}").buildAndExpand(cs.getId()).toUri());
            return new ResponseEntity<>(coffeeSiteService.findOneToTransfer(csID).orElseGet(null), HttpStatus.OK);

        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * Method to handle request to get list of CoffeeSites created by logged in user.
     * 
     * @return
     */
    @GetMapping("/mySites") // napr. https://coffeecompass.cz/rest/secured/site/mySites
    public ResponseEntity<List<CoffeeSiteDTO>> sendMySites() {
        List<CoffeeSiteDTO> coffeeSites = coffeeSiteService.findAllFromLoggedInUser();
        
        if (coffeeSites == null || coffeeSites.isEmpty()) {
            log.error("No Coffee site from user found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } 
        
        log.info("All sites from logged-in user retrieved.");
        return new ResponseEntity<>(coffeeSites, HttpStatus.OK);   
    }
    
    /**
     * Method to handle request to send list od CoffeeSites created by logged in user paginated.
     * Only NOT CANCELED sites are returned.
     * 
     * @return
     */
    @GetMapping("/mySitesPaginated/") // napr. https://coffeecompass.cz/rest/secured/site/mySitesPaginated/?size=5&page=1
    public ResponseEntity<Page<CoffeeSiteDTO>> sendMySitesPaginated(@RequestParam("page") Optional<Integer> page,
                                                                    @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(15);
        Page<CoffeeSiteDTO> coffeeSitePage;
        
        coffeeSitePage = coffeeSiteService.findAllNotCancelledFromLoggedInUserPaginated(PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.fromString("DESC"), "createdOn")));
        
        if (coffeeSitePage == null || coffeeSitePage.isEmpty()) {
            log.error("No Coffee site from logged-in user found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } 
        
        log.info("Page n. {} of coffee sites from logged-in user retrieved.", currentPage);
        return new ResponseEntity<>(coffeeSitePage, HttpStatus.OK);   
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
                ? new ResponseEntity<>(numberOfSitesFromUser, HttpStatus.OK)
                : new ResponseEntity<>(0, HttpStatus.NOT_FOUND);
        
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
                ? new ResponseEntity<>(numberOfSitesFromUser, HttpStatus.OK)
                : new ResponseEntity<>(0, HttpStatus.NOT_FOUND);
    }
}
