/*
 * 
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 6, 2013 2:12:46 PM
 *
 */
public enum RecordTypeEnumeration {
	SerializedStreamHeader(0),
	ClassWithId(1),
	SystemClassWithMembers(2),
	ClassWithMembers(3),
	SystemClassWithMembersAndTypes(4),
	ClassWithMembersAndTypes(5),
	BinaryObjectString(6),
	BinaryArray(7),
	MemberPrimitiveTyped(8),
	MemberReference(9),
	ObjectNull(10),
	MessageEnd(11),
	BinaryLibrary(12),
	ObjectNullMultiple256(13),
	ObjectNullMultiple(14),
	ArraySinglePrimitive(15),
	ArraySingleObject(16),
	ArraySingleString(17),
	MethodCall(21),
	MethodReturn(22);
	
	private final byte	_value;
	public final byte getValue()
	{
		return _value;
	}
	
	public final void write (OutputStream out) throws IOException
	{
		if (out == null)
			throw new IOException("No output stream");
		DataFormatConverter.writeUnsignedByte(out, (short) (getValue() & 0xFF));
	}

	RecordTypeEnumeration(int value)
	{
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE))
			throw new IllegalStateException("Bad value: " + value);
		_value = (byte) (value & 0xFF);
	}
	
	public static final Set<RecordTypeEnumeration>	VALUES=
			Collections.unmodifiableSet(EnumSet.allOf(RecordTypeEnumeration.class));
	
	public static final RecordTypeEnumeration read(InputStream in) throws IOException
	{
		short	typeValue=DataFormatConverter.readUnsignedByte(in);
		RecordTypeEnumeration	enumValue=fromValue(typeValue);
		if (enumValue == null)
			throw new StreamCorruptedException("Unknown " + RecordTypeEnumeration.class.getSimpleName() + " value: " + typeValue);
		
		return enumValue;
	}

	public static final void write (RecordTypeEnumeration enumValue, OutputStream out) throws IOException
	{
		if (enumValue == null)
			throw new StreamCorruptedException("No enum value to write");
		enumValue.write(out);
	}

	public static final RecordTypeEnumeration fromValue(int value)
	{
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE))
			return null;
		
		for(RecordTypeEnumeration recType : VALUES)
		{
			if (value == recType.getValue())
				return recType;
		}
		
		return null;
	}
}
