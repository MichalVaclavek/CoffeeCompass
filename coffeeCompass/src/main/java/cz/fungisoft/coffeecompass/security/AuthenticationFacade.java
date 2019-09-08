package cz.fungisoft.coffeecompass.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade implements IAuthenticationFacade
{
//    private AuthenticationManager authenticationManager;
    
    public AuthenticationFacade(/* @Lazy AuthenticationManager authenticationManager */) {
        super();
//        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

}
