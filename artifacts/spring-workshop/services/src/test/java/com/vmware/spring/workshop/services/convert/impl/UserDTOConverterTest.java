package com.vmware.spring.workshop.services.convert.impl;

import org.junit.Test;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.dto.user.UserRoleTypeDTO;
import com.vmware.spring.workshop.model.user.User;
import com.vmware.spring.workshop.model.user.UserRoleType;

/**
 * @author lgoldstein
 */
public class UserDTOConverterTest extends AbstractDTOConverterTestSupport<User,UserDTO> {
    private final UserDTOConverterImpl    _converter=new UserDTOConverterImpl();
    public UserDTOConverterTest() {
        super();
    }

    @Test
    public void testModel2DTOConversion () {
        checkModel2DTOConversion(createMockUser(), _converter);
    }

    @Test
    public void testDTO2ModelConversion () {

        final User    data=checkDTO2ModelConversion(createMockUserDTO(), _converter);
        assertEquals("Mismatched reconstructed version", 0, data.getVersion());
    }

    static final User createMockUser () {
        final User    data=new User();
        final long    nanoTime=System.nanoTime(), msecTime=System.currentTimeMillis();
        data.setId(Long.valueOf(nanoTime));
        data.setVersion((int) msecTime);
        data.setName("user#" + data.getId());
        data.setHomeAddress(msecTime + "/" + nanoTime);
        data.setLoginName("7365@" + nanoTime);
        data.setPassword("3777347@" + nanoTime);
        data.setRole(UserRoleType.CUSTOMER);
        return data;
    }

    static final UserDTO createMockUserDTO () {
        final UserDTO    dto=new UserDTO();
        final long        nanoTime=System.nanoTime(), msecTime=System.currentTimeMillis();
        dto.setId(Long.valueOf(nanoTime));
        dto.setName("dto#" + dto.getId());
        dto.setHomeAddress(msecTime + "/" + nanoTime);
        dto.setLoginName("1690@" + nanoTime);
        dto.setPassword("1704@" + nanoTime);
        dto.setRole(UserRoleTypeDTO.ADMIN);
        return dto;
    }
}
