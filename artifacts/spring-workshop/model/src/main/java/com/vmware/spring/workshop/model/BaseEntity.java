package com.vmware.spring.workshop.model;

import java.io.Serializable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author lgoldstein
 */
public abstract class BaseEntity implements Serializable {
	private static final long serialVersionUID = 3271759098212557258L;

	protected BaseEntity() {
		super();
	}

	protected String[] excludedFields() {
		return ArrayUtils.EMPTY_STRING_ARRAY;
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
}
