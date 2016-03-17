package com.vmware.spring.workshop.facade.support.impl;

import java.beans.IntrospectionException;

import org.springframework.stereotype.Component;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.facade.support.UserDTOImportExport;

/**
 * @author lgoldstein
 */
@Component("userDTOImportExport")
public class UserDTOImportExportImpl
        extends AbstractCSVImportExportImpl<UserDTO>
        implements UserDTOImportExport {
    public UserDTOImportExportImpl () throws IntrospectionException {
        super(UserDTO.class);
    }
}
