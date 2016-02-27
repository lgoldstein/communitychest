package com.vmware.spring.workshop.facade.restful.resteasy;

import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.dto.user.UserDTOList;
import com.vmware.spring.workshop.services.facade.UsersFacade;

/**
 * @author lgoldstein
 */
@Path(AbstractResteasyRestfulService.RESTEASY_ACCESS_ROOT + "/users")
@Produces({ MediaType.APPLICATION_XML })
@Service("usersResteasyService")
public class UsersResteasyService extends AbstractResteasyRestfulService<UserDTO, UsersFacade> {
	@Inject
	public UsersResteasyService (final UsersFacade facade) {
		super(UserDTO.class, facade);
	}

	@Override
	protected UserDTOList wrapAsDTOList(Collection<? extends UserDTO> dtoList) {
		return new UserDTOList(dtoList);
	}
}
