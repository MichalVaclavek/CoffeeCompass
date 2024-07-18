package cz.fungisoft.users.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {
    
    public AuthenticationFacade(/* @Lazy AuthenticationManager authenticationManager */) {
        super();
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
