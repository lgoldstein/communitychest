package com.vmware.spring.workshop.dto.user;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.vmware.spring.workshop.dto.DTOList;

/**
 * @author lgoldstein
 */
@XmlRootElement(name="usersList")
@XmlType(name="usersList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class UserDTOList extends DTOList<UserDTO> {
    private static final long serialVersionUID = 8576659706059429436L;

    public UserDTOList() {
        super();
    }

    public UserDTOList(Collection<? extends UserDTO> c) {
        super(c);
    }

    public UserDTOList(int initialCapacity) {
        super(initialCapacity);
    }

}
