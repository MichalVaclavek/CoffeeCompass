package cz.fungisoft.coffeecompass.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import cz.fungisoft.coffeecompass.dto.UserDataDTO;
import cz.fungisoft.coffeecompass.entity.User;

 
public interface UserService
{     
    UserDataDTO findByIdToTransfer(Integer id);
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    User findById(Integer id);
    
    UserDataDTO findByUserNameToTransfer(String userName);
    User findByUserName(String userName);
    User findByEmail(String email);
     
    User saveUser(User user);
    
    // Pro ulozeni nove vytvoreneho usera z DTO objektu ve formulari
    User save(UserDataDTO registration);
    UserDataDTO updateUser(UserDataDTO user);
    User updateUser(User user);    
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    void deleteUserBySSO(String sso);
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    void deleteUserById(Integer id);
 
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    List<UserDataDTO> findAllUsers();
    
    boolean isUserNameUnique(Integer id, String sso);
    boolean isEmailUnique(Integer id, String email);
    
    boolean hasADMINRole(User user);
    boolean hasADMINorDBARole(User user);
    boolean hasDBARole(User user);
    
    /**
     * A method to return true, if the logged-in subscriber is same
     * as User
     * 
     * @param user - a User account to evaluate/compare against logged-in User
     * @return true, if the logged-in subscriber is same as "user"
     */
    boolean isLoggedInUserToManageItself(User user);
    
    /**
     * Method to return true if currently logged-in user has ADMIN role
     * @return
     */
    boolean isADMINloggedIn();

    User getCurrentLoggedInUser();
    
}
