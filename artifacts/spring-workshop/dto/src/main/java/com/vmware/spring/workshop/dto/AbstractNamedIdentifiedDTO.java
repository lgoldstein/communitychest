package com.vmware.spring.workshop.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author lgoldstein
 */
@XmlType(name="namedDTO")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class AbstractNamedIdentifiedDTO extends AbstractIdentifiedDTO implements NamedDTO {
	private static final long serialVersionUID = -2997504295370612540L;

	private String	_name;
	public AbstractNamedIdentifiedDTO() {
		super();
	}

	@Override
	@XmlElement(name="name",required=true,nillable=false)
	public String getName() {
		return _name;
	}

	@Override
	public void setName(String name) {
		_name = name;
	}

}
