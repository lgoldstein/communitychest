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
@XmlRootElement(name="banksList")
@XmlType(name="banksList")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class BankDTOList extends DTOList<BankDTO> {
	private static final long serialVersionUID = -2962602828095909394L;

	public BankDTOList() {
		super();
	}

	public BankDTOList(int initialCapacity) {
		super(initialCapacity);
	}

	public BankDTOList(Collection<? extends BankDTO> c) {
		super(c);
	}

}
