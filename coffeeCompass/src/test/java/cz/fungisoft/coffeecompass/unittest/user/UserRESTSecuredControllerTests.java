package cz.fungisoft.coffeecompass.unittest.user;

import cz.fungisoft.coffeecompass.controller.rest.secured.UserControllerSecuredREST;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.service.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


/**
 * Testovani Controller metod pro praci s User.
 * 
 * @author Michal Vaclavek
 *
 */
@RunWith(SpringRunner.class)
public class UserRESTSecuredControllerTests
{
    @MockBean //provided by Spring Context
    private UserService userService;
  
    @MockBean
    private UserSecurityService userSecurityService;
    
    
    private UserControllerSecuredREST usersControllerSecuredREST;
    
    
    @Before
    public void setUp() {
        usersControllerSecuredREST = new UserControllerSecuredREST(userService, userSecurityService);
    }
 
    
    /**
     * To test if all Users are returned when requested. Only ADMIN user can request that.
     * First, mock ADMIN user is logged in. 
     * 
     * @throws Exception
     */
    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        
        UserDTO john = new UserDTO();
        john.setUserName("john");
        john.setEmail("john@vonneuman.com");
        
        UserDTO mary = new UserDTO();
        mary.setUserName("mary");
        mary.setEmail("mary@gun.com");
        
        UserDTO dick = new UserDTO();        
        dick.setUserName("dick");
        dick.setEmail("dick@feynman.com");
            
        List<UserDTO> allUsers = Arrays.asList(dick, mary, john);
     
        given(userService.findAllUsers()).willReturn(allUsers);
        
        ResponseEntity<List<UserDTO>> response = usersControllerSecuredREST.listAllUsers();
        
        verify(userService, VerificationModeFactory.times(1)).findAllUsers();
        assertThat(response.getBody().size()).isEqualTo(allUsers.size());
        assertThat(response.getBody().get(0).getUserName()).isEqualTo("dick");
        
    }
    
}
