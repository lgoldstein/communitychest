package com.vmware.spring.workshop.services.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.AuthenticationException;

import com.vmware.spring.workshop.model.user.User;

/**
 * @author lgoldstein
 */
public interface CleartextAuthenticationProvider extends AuthenticationProvider {
    User authenticate (String username, String password) throws AuthenticationException;
}
