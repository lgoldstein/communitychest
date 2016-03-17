package com.vmware.spring.workshop.model.user;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author lgoldstein
 */
public enum UserRoleType {
    ADMIN,
    CUSTOMER,
    GUEST;

    public static final Set<UserRoleType>    VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(UserRoleType.class));
}
