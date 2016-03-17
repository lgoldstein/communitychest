package com.vmware.spring.workshop.dto.banking;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.vmware.spring.workshop.dto.DTOList;

/**
 * @author lgoldstein
 */
@XmlRootElement(name="branchesList")
@XmlType(name="branchesList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class BranchDTOList extends DTOList<BranchDTO> {
    private static final long serialVersionUID = -1389880648531400996L;

    public BranchDTOList() {
        super();
    }

    public BranchDTOList(int initialCapacity) {
        super(initialCapacity);
    }

    public BranchDTOList(Collection<? extends BranchDTO> c) {
        super(c);
    }

}
