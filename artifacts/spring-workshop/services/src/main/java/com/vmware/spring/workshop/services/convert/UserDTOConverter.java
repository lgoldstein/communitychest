package com.vmware.spring.workshop.services.convert;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.model.user.User;

/**
 * @author lgoldstein
 */
public interface UserDTOConverter extends DTOConverter<User, UserDTO> {
    // nothing extra
}
