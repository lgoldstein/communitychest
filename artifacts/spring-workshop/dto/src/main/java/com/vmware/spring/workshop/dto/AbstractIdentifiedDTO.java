package com.vmware.spring.workshop.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author lgoldstein
 */
@XmlType(name="identifiedDTO")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public abstract class AbstractIdentifiedDTO implements Serializable, IdentifiedDTO {
	private static final long serialVersionUID = 4573669247742546427L;

	private Long	_id;
	public AbstractIdentifiedDTO() {
		super();
	}

	@Override
	@XmlAttribute(name="id")
	public Long getId() {
		return _id;
	}

	@Override
	public void setId(Long id) {
		_id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		if (!EqualsBuilder.reflectionEquals(this, obj, excludedFields()))
			return false;	// debug breakpoint
		return true;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, excludedFields());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	protected String[] excludedFields () {
		return ArrayUtils.EMPTY_STRING_ARRAY;
	}
}
