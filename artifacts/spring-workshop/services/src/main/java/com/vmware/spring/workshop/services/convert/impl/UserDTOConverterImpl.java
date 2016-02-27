package com.vmware.spring.workshop.services.convert.impl;

import org.springframework.stereotype.Component;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.model.user.User;
import com.vmware.spring.workshop.services.convert.UserDTOConverter;

/**
 * @author lgoldstein
 */
@Component("userDTOConverter")
public class UserDTOConverterImpl extends AbstractDTOConverter<User,UserDTO> implements UserDTOConverter {
	public UserDTOConverterImpl () {
		super(User.class, UserDTO.class);
	}
}
