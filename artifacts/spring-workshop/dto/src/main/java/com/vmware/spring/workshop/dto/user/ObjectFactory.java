package com.vmware.spring.workshop.dto.user;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * @author lgoldstein
 */
@XmlRegistry
public final class ObjectFactory {
	public ObjectFactory() {
		super();
	}

	public UserDTO createUserDTO () {
		return new UserDTO();
	}
	
	public UserDTOList createUserDTOList () {
		return new UserDTOList();
	}
}
