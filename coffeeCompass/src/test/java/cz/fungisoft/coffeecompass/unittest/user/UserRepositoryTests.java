package cz.fungisoft.coffeecompass.unittest.user;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Testuje "Repository" vrstvu pro objektu User.
 * 
 * @author Michal Vaclavek
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest // automaticky vytvori propojeni na H2 in-memory DB, ktera je uvedena v pom.xml dependency
@ActiveProfiles("test") // pro HSQL db pouzit @ActiveProfiles("testhsql")
public class UserRepositoryTests
{
    @Autowired
    private TestEntityManager entityManager;
 
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    private UserProfile userProfUser;
    private UserProfile userProfAdmin;
    
    @Before
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
        newUser.setCreatedOn(new Timestamp(new Date().getTime()));
        
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

}
