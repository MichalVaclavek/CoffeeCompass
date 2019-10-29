package cz.fungisoft.coffeecompass.controller.models;

import lombok.Data;

/**
 * Class to hold data needed in Form where user is requested to confirm<br>
 * his/her user account.
 * 
 * @author Michal Vaclavek
 *
 */
@Data
public class DeleteUserAccountModel
{
    private Long userId;
    
    private String userName;
    
    // true, when user is deleting it's own account
    // false when ADMIN is deleting another account
    private boolean userToDeleteItself;
    
    /**
     * Should we delete coffee sites created by the user.
     */
    private boolean deleteUsersCoffeeSites = false; // default value false = not to delete
    
    /**
     * Should we delete comments created by the user.
     */
    private boolean deleteUsersComments = false; // default value false = not to delete
}
