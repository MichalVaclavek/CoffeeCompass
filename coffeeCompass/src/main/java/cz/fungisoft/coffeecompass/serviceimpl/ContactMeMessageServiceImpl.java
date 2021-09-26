package cz.fungisoft.coffeecompass.serviceimpl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.configuration.ConfigProperties;
import cz.fungisoft.coffeecompass.dto.ContactMeMessageDTO;
import cz.fungisoft.coffeecompass.entity.ContactMeMessage;
import cz.fungisoft.coffeecompass.repository.ContactMeMessageRepository;
import cz.fungisoft.coffeecompass.service.IContactMeMessageService;
import cz.fungisoft.coffeecompass.service.email.ISendEmailService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;

@Service("contactMeMessageService")
@Transactional
@Log4j2
public class ContactMeMessageServiceImpl implements IContactMeMessageService {

    private final ContactMeMessageRepository contactMeMessageRepo;
    
    private final MapperFacade mapperFacade;
    
    private final ISendEmailService sendMeEmailService;
    
    @Autowired
    private ConfigProperties config;
    
    @Autowired
    public ContactMeMessageServiceImpl(ContactMeMessageRepository contactMeMessageRepo, MapperFacade mapperFacade, ISendEmailService sendMeEmailService) {
        super();
        this.contactMeMessageRepo = contactMeMessageRepo;
        this.mapperFacade = mapperFacade;
        this.sendMeEmailService = sendMeEmailService;
    }

    @Override
    public ContactMeMessage saveContactMeMessage(String message, String authorName, String email) {
        if ( (message == null) || message.isEmpty())
            throw new IllegalArgumentException("Empty message text is not allowed!");
        
        ContactMeMessage cmMessage = new ContactMeMessage();
        cmMessage.setCreatedTime(new Timestamp(new Date().getTime()));
        cmMessage.setAuthorName(authorName);
        cmMessage.setEmail(email);
        cmMessage.setTextOfMessage(message);
       
        contactMeMessageRepo.save(cmMessage);
        
        log.info("Contact me message from user e-mail {} saved.", email);
        
        return cmMessage;
    }

    @Override
    public List<ContactMeMessage> getAllFromAuthor(String authorName) {
        return contactMeMessageRepo.getAllFromAuthor(authorName);
    }

    @Override
    public List<ContactMeMessage> getAllFromDate(Date fromDate) {
        return contactMeMessageRepo.getAllFromDate(new Timestamp(fromDate.getTime()));
    }

    @Override
    public List<ContactMeMessage> getAll() {
        return contactMeMessageRepo.findAll();
    }

    @Override
    public ContactMeMessage saveContactMeMessage(ContactMeMessage message) {
        sendMeEmailService.sendMeSimpleEmail(message.getAuthorName(), message.getEmail(), config.getContactMeEmailFrom(), config.getContactMeEmailTo(), message.getTextOfMessage());
        log.info("Contact me message from user e-mail {} sent to me.", message.getEmail());
        return contactMeMessageRepo.save(message);
    }

    @Override
    public ContactMeMessage saveContactMeMessage(ContactMeMessageDTO message) {
        ContactMeMessage cmmToSave = mapperFacade.map(message, ContactMeMessage.class);
        cmmToSave.setCreatedTime(new Timestamp(new Date().getTime()));
        return saveContactMeMessage(cmmToSave);
    }
}
