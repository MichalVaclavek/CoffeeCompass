package cz.fungisoft.coffeecompass.dto;

import java.sql.Timestamp;
import java.util.Set;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.Email;

import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.validators.PasswordMatches;
import cz.fungisoft.coffeecompass.validators.ValidEmail;
import lombok.Data;

/**
 * Trida pro prenos vybranych informaci o objektu User na clienta. Tzv. DTO objekt.
 * 
 * @author Michal Vaclavek
 */
@Data
@PasswordMatches
public class UserDataDTO
{
    private int id; 
     
    @Size(min=3, max=30)
    private String userName;
         
    @Size(max=30)
    private String firstName;
 
    @Size(max=50)
    private String lastName;
 
//    @Email // Can be empty as it cannot be changed by ADMIN during editing
    @ValidEmail
    @Size(max=64)
    private String email;
    
    // Can be empty as the USER or ADMIN don't want to change the password. Is evaluated in UserController
    private String password;

    // Can be empty as the USER or ADMIN don't want to change the password. Is evaluated in UserController
    private String confirmPassword;
    
    private Set<UserProfile> userProfiles;
    
    @JsonFormat(pattern = "dd. MM. yyyy HH:mm")
    private Timestamp createdOn;
    
    private Integer createdSites;
    private Integer updatedSites;
    private Integer deletedSites;   
    
    /**
     * To evaluate if the user can be modified.
     */
    private boolean hasADMINRole;
    
    /**
     * True if this user is to be managed by itself
     */
    public boolean isToManageItself = true; // default value. Used in case of a new User to be created
    
    private boolean enabled;
    
    private boolean banned;
    
    private boolean registerEmailConfirmed;

}
