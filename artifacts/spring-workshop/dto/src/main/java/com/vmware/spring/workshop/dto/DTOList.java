package com.vmware.spring.workshop.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A JAXB solution for returning DTO(s) list
 * @author lgoldstein
 */
@XmlRootElement(name="dto-list")
@XmlType(name="dto-list")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public abstract class DTOList<DTO> extends ArrayList<DTO> {
	private static final long serialVersionUID = -6913196696092841250L;

	protected DTOList() {
		super();
	}

	protected DTOList(int initialCapacity) {
		super(initialCapacity);
	}

	protected DTOList(Collection<? extends DTO> c) {
		super(c);
	}

	@XmlElement(name="dtoInstance", nillable=true)
	public List<DTO> getDTOList () {
		return this;
	}
	
	public void setDTOList (List<DTO> dtoList) {
		if (size() > 0)
			clear();

		if (dtoList != null)
			addAll(dtoList);
	}
}
