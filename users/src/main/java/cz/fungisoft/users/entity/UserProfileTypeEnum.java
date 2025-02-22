package cz.fungisoft.users.entity;

import java.io.Serializable;

/**
 * Enum pro ruzne User profile
 * 
 * @author Michal Vaclavek
 */
public enum UserProfileTypeEnum implements Serializable {

    USER("USER"),
    DBA("DBA"),
    ADMIN("ADMIN"),
    TEST("TEST");
     
    private final String userProfileType;
     
    UserProfileTypeEnum(String userProfileType) {
        this.userProfileType = userProfileType;
    }
     
    public String getUserProfileType() {
        return userProfileType;
    }
}
