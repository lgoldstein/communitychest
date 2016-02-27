/*
 * 
 */
package net.community.chest.io.serial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 22, 2012 9:53:47 AM
 */
public class SerializedClassDescriptor implements Serializable, Cloneable {
	private static final long serialVersionUID = 5411897214459446896L;

	private String className;
	private long serialVerUID;
	private Collection<SerializedClassFlag> flagSet;
	private List<SerialFieldDescriptor> fieldDescriptors;

	public SerializedClassDescriptor () {
		super();
	}

	public SerializedClassDescriptor (String name, long value, Collection<SerializedClassFlag> flags) {
		className = name;
		serialVerUID = value;
		flagSet = flags;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String name) {
		className = name;
	}

	public long getSerialVerUID() {
		return serialVerUID;
	}

	public void setSerialVerUID(long value) {
		serialVerUID = value;
	}

	public Collection<SerializedClassFlag> getFlags () {
		return flagSet;
	}

	public void setFlags (Collection<SerializedClassFlag> flags) {
		flagSet = flags;
	}

	public List<SerialFieldDescriptor> getFields () {
		return fieldDescriptors;
	}

	public List<SerialFieldDescriptor> addField (String name, String type, FieldTypeDescriptor typeDescriptor, int index) {
		return addField(new SerialFieldDescriptor(name, type, typeDescriptor, index));
	}

	public List<SerialFieldDescriptor> addField (SerialFieldDescriptor field) {
		if (field == null) {
			throw new IllegalArgumentException("No field data");
		}

		if (fieldDescriptors == null) {
			fieldDescriptors = new ArrayList<SerialFieldDescriptor>();
		}

		fieldDescriptors.add(field);
		return fieldDescriptors;
	}

	public void setFields (List<SerialFieldDescriptor> fields) {
		fieldDescriptors = fields;
	}

	@Override
	public int hashCode() {
		return StringUtil.getDataStringHashCode(getClassName(), true);
	}

	// NOTE !!! compares only the class name
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		
		SerializedClassDescriptor	other=(SerializedClassDescriptor) obj;
		if (StringUtil.compareDataStrings(getClassName(), other.getClassName(), true) != 0)
			return false;	// debug breakpoint

		return true;
	}

	@Override
	public SerializedClassDescriptor clone() {
		try {
			return getClass().cast(super.clone());
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Unexpected clone exception for " + toString() + ": " + e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return getClassName() + "[" + getSerialVerUID() + "]: " + getFlags();
	}
}
