package com.vmware.spring.workshop.services.facade;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;

import com.vmware.spring.workshop.dto.user.UserDTO;

/**
 * @author lgoldstein
 */
public interface UsersFacade
        extends CommonFacadeActions<UserDTO>,
                AuthenticationManager,
                AuthenticationProvider {
    UserDTO    findByLoginName (String loginName);
}
