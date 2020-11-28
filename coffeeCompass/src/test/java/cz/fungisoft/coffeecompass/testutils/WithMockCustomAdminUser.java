package cz.fungisoft.coffeecompass.testutils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * Default test ADMIN user to be used in Controller tests for User object.
 * This user will be loged-in if using @WithMockCustomAdminUser annotation on test method.
 * (for example in UserControllerMvcTest class)
 * 
 * @author Michal Vaclavek
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomAdminUser
{
    String userName() default "admin";

    String email() default "richard@feynman.edu";
    
    String[] roles() default {"ADMIN"};

    String password() default "adminpassword";
}
