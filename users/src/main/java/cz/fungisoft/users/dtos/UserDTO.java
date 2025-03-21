package cz.fungisoft.users.dtos;

import java.time.LocalDateTime;

/**
 * Trida pro prenos vybranych informaci o objektu User na clienta. Tzv. DTO objekt.
 * 
 * @author Michal Vaclavek
 */
@Data
@PasswordMatches
public class UserDTO {

    private Long id; 
     
    @Size(min=3, max=30)
    private String userName;
    
    public void setUserName(String userName) {
        if (userName != null) {
            this.userName = userName.trim();
        }
    }
         
    @Size(max=30)
    private String firstName;
 
    @Size(max=50)
    private String lastName;
 
    @ValidEmail(canbeempty = true) // e-mail is not obligatory, can be empty
    @Size(max=64)
    private String email;
    
    public void setEmail(String email) {
        if (email != null) {
            this.email = email.trim();
        }
    }
    
    // Can be empty as the USER or ADMIN don't want to change the password. Is evaluated in UserController
    @JsonIgnore
    private String password;

    // Can be empty as the USER or ADMIN don't want to change the password. Is evaluated in UserController
    @JsonIgnore
    private String confirmPassword;
    
    private Set<UserProfile> userProfiles;
    
    private String authProvider;
    
    @JsonFormat(pattern = "dd.MM. yyyy HH:mm", timezone="Europe/Prague")
    private LocalDateTime createdOn;
    
    private Integer createdSites;
    private Integer updatedSites;
    private Integer deletedSites;   
    
    /**
     * To evaluate if the user can be modified.
     */
    @JsonIgnore
    private boolean hasADMINRole;
    
    /**
     * True if this user is to be managed by itself
     */
    public boolean toManageItself = false; // needed for case, when userProfile is edited by ADMIN user
    
    @JsonIgnore
    private boolean enabled;
    
    @JsonIgnore
    private boolean banned;
    
    @JsonIgnore
    private boolean registerEmailConfirmed;
}
