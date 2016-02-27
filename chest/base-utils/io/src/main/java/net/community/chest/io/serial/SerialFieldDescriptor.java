/*
 * 
 */
package net.community.chest.io.serial;

import java.io.Serializable;

import net.community.chest.lang.EnumUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 22, 2012 11:10:13 AM
 *
 */
public class SerialFieldDescriptor implements Serializable, Cloneable {
	private static final long serialVersionUID = -9133847232664027399L;
	private String	_fieldName;
	private String	_fieldType;
	private FieldTypeDescriptor	_typeDescriptor;
	private int		_fieldIndex;
	
	public SerialFieldDescriptor ()
	{
		super();
	}

	public SerialFieldDescriptor (String name, String type, FieldTypeDescriptor typeDescriptor, int index)
	{
		_fieldName = name;
		_fieldType = type;
		_typeDescriptor = typeDescriptor;
		_fieldIndex = index;
	}

	public String getFieldName ()
	{
		return _fieldName;
	}

	public void setFieldName (String fieldName)
	{
		_fieldName = fieldName;
	}

	public String getFieldType ()
	{
		return _fieldType;
	}

	public void setFieldType (String fieldType)
	{
		_fieldType = fieldType;
	}

	public FieldTypeDescriptor getTypeDescriptor ()
	{
		return _typeDescriptor;
	}

	public void setTypeDescriptor (FieldTypeDescriptor typeDescriptor)
	{
		_typeDescriptor = typeDescriptor;
	}

	public int getFieldIndex ()
	{
		return _fieldIndex;
	}

	public void setFieldIndex (int fieldIndex)
	{
		_fieldIndex = fieldIndex;
	}

	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getFieldName(), true)
			 + StringUtil.getDataStringHashCode(getFieldType(), true)
			 + ClassUtil.getObjectHashCode(getTypeDescriptor())
			 + getFieldIndex()
			 ;
	}

	@Override
	public boolean equals (Object obj)
	{
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;

		SerialFieldDescriptor	other=(SerialFieldDescriptor) obj;
		if ((StringUtil.compareDataStrings(getFieldName(), other.getFieldName(), true) != 0)
		 || (StringUtil.compareDataStrings(getFieldType(), other.getFieldType(), true) != 0)
		 || (EnumUtil.compareValues(getTypeDescriptor(), other.getTypeDescriptor()) != 0)
		 || (getFieldIndex() != other.getFieldIndex()))
			return false;	// debug breakpoint

		return true;
	}

	@Override
	public SerialFieldDescriptor clone () 
	{
		try
		{
			return getClass().cast(super.clone());
		}
		catch(CloneNotSupportedException e) {
			throw new RuntimeException("Failed to clone " + toString() + ": " + e.getMessage());
		}
	}

	@Override
	public String toString ()
	{
		return getFieldName() + "[" + getFieldType() + "/" + getTypeDescriptor() + "]@" + getFieldIndex();
	}
}
