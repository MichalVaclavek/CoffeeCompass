package cz.fungisoft.coffeecompass.unittest.user;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Testuje "Repository" vrstvu pro objekt/entitu User.
 * 
 * @author Michal Vaclavek
 *
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest // automaticky vytvori propojeni na H2 in-memory DB, ktera je uvedena v pom.xml dependency
@ActiveProfiles("test") // pro HSQL db pouzit @ActiveProfiles("testhsql")
class UserRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;
 
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    private UserProfile userProfUser;
    private UserProfile userProfAdmin;
    
    @BeforeEach
    public void setUp() {
        // Vytvorit zaznamy do UserProfile DB        
        userProfUser = new UserProfile();
        userProfUser.setType("USER");
        entityManager.persist(userProfUser);
               
        userProfAdmin = new UserProfile();
        userProfAdmin.setType("ADMIN");
        entityManager.persist(userProfAdmin);

        entityManager.flush();      
    }
    
    
    @Test
    public void whenFindByName_thenReturnUser() {
        User newUser = new User();
        
        newUser.setUserName("pauld");
        newUser.setFirstName("Paul");
        newUser.setLastName("Dirac");
        
        String emailAddr = "paul@dirac.fy";
        newUser.setEmail(emailAddr);
        newUser.setPassword("pozitron");
        newUser.setCreatedOn(LocalDateTime.now());
        
        Set<UserProfile> userProfiles = new HashSet<UserProfile>();
        userProfiles.add(userProfUser);
        newUser.setUserProfiles(userProfiles);    
               
        entityManager.persist(newUser);
        entityManager.flush();
     
        // when
        Optional<User> found = usersRepository.searchByUsername("pauld");
     
        // then
        assertTrue(found.isPresent());
        
        if (found.isPresent()) {
            assertThat(found.get().getUserName())
               .isEqualTo(newUser.getUserName());
            
            assertThat(found.get().getPassword())
               .isEqualTo(newUser.getPassword());
        }
    }

    @Test
    public void whenGetTop5Users_thenCountOnlyActiveSitesCreatedByUser() {
        CoffeeSiteRecordStatus activeStatus = new CoffeeSiteRecordStatus();
        activeStatus.setStatus(CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.ACTIVE.getSiteRecordStatus());
        entityManager.persist(activeStatus);

        CoffeeSiteRecordStatus inactiveStatus = new CoffeeSiteRecordStatus();
        inactiveStatus.setStatus(CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum.INACTIVE.getSiteRecordStatus());
        entityManager.persist(inactiveStatus);

        User firstUser = createUser("firstUser");
        User secondUser = createUser("secondUser");
        entityManager.persist(firstUser);
        entityManager.persist(secondUser);

        entityManager.persist(createCoffeeSite("first active 1", firstUser, activeStatus));
        entityManager.persist(createCoffeeSite("first active 2", firstUser, activeStatus));
        entityManager.persist(createCoffeeSite("first inactive", firstUser, inactiveStatus));
        entityManager.persist(createCoffeeSite("second active", secondUser, activeStatus));

        entityManager.flush();

        List<DBReturnPair> topUsers = usersRepository.getTop5Users();

        assertThat(topUsers).hasSize(2);
        assertThat(topUsers.get(0).getStrVal()).isEqualTo("firstUser");
        assertThat(topUsers.get(0).getIntVal()).isEqualTo(2L);
        assertThat(topUsers.get(1).getStrVal()).isEqualTo("secondUser");
        assertThat(topUsers.get(1).getIntVal()).isEqualTo(1L);
    }

    private User createUser(String userName) {
        User user = new User();
        user.setUserName(userName);
        user.setFirstName(userName);
        user.setLastName("test");
        user.setEmail(userName + "@example.com");
        user.setPassword("password");
        user.setCreatedOn(LocalDateTime.now());
        user.setUserProfiles(Set.of(userProfUser));
        user.setEnabled(true);
        return user;
    }

    private CoffeeSite createCoffeeSite(String siteName, User user, CoffeeSiteRecordStatus recordStatus) {
        CoffeeSite coffeeSite = new CoffeeSite();
        coffeeSite.setSiteName(siteName);
        coffeeSite.setCreatedOn(LocalDateTime.now());
        coffeeSite.setOriginalUser(user);
        coffeeSite.setRecordStatus(recordStatus);
        return coffeeSite;
    }
}
