package com.vmware.spring.workshop.dto.user;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author lgoldstein
 */
@XmlEnum
public enum UserRoleTypeDTO {
    ADMIN,
    CUSTOMER,
    GUEST;

    public static final Set<UserRoleTypeDTO>    VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(UserRoleTypeDTO.class));

}
