/*
 * 
 */
package net.community.chest.io.serial;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 22, 2012 12:55:39 PM
 *
 */
public enum FieldTypeDescriptor {
	BYTE('B', Byte.TYPE),
	CHAR('C', Character.TYPE),
	DOUBLE('D', Double.TYPE),
	FLOAT('F', Float.TYPE),
	INTEGER('I', Integer.TYPE),
	LONG('J', Long.TYPE),
	SHORT('S', Short.TYPE),
	BOOLEAN('Z', Boolean.TYPE),
	ARRAY('[', Object[].class, "[]"),
	OBJECT('L', Object.class);

	public static final Set<FieldTypeDescriptor>	VALUES=
			Collections.unmodifiableSet(EnumSet.allOf(FieldTypeDescriptor.class));

	public static final FieldTypeDescriptor fromCharOrTypeName (String typeName) {
		if ((typeName == null) || (typeName.length() <= 0)) {
			return null;
		}

		if (typeName.length() == 1) {
			return fromChar(typeName.charAt(0));
		} else {
			return fromTypeName(typeName);
		}
	}

	public static final FieldTypeDescriptor fromTypeName (String typeName) {
		if ((typeName == null) || (typeName.length() <= 0)) {
			return null;
		}

		for (FieldTypeDescriptor d : VALUES) {
			if (StringUtil.compareDataStrings(typeName, d.getTypeName(), false) == 0)
				return d;
		}

		return null;
	}

	public static final FieldTypeDescriptor fromChar (char ch) {
		for (FieldTypeDescriptor d : VALUES) {
			if (d.getTypeChar() == ch)
				return d;
		}

		return null;
	}

	private final char	_typeChar;
	public char getTypeChar () {
		return _typeChar;
	}

	private final Class<?>	_typeClass;
	public Class<?> getTypeClass () {
		return _typeClass;
	}

	private final String	_typeName;
	public String getTypeName () {
		return _typeName;
	}

	FieldTypeDescriptor (char typeChar, Class<?> typeClass) {
		this(typeChar, typeClass, typeClass.getName());
	}

	FieldTypeDescriptor (char typeChar, Class<?> typeClass, String typeName) {
		_typeChar = typeChar;
		_typeClass = typeClass;
		_typeName = typeName;
	}
}
