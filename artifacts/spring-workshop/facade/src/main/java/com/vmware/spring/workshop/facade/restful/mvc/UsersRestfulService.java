package com.vmware.spring.workshop.facade.restful.mvc;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.dto.user.UserDTOList;
import com.vmware.spring.workshop.services.facade.UsersFacade;

/**
 * @author lgoldstein
 */
@Controller("usersRestfulService")
@RequestMapping(AbstractMVCRestfulService.MVC_ACCESS_ROOT + "/users")
public class UsersRestfulService extends AbstractMVCRestfulService<UserDTO, UsersFacade> {
    @Inject
    public UsersRestfulService (final UsersFacade facade) {
        super(UserDTO.class, facade);
    }

    @Override
    protected UserDTOList wrapAsDTOList(Collection<? extends UserDTO> dtoList) {
        return new UserDTOList(dtoList);
    }
}
