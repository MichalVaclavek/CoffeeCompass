package cz.fungisoft.users.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

public interface IAuthenticationFacade {

    Authentication getAuthentication();
    SecurityContext getContext();
}
