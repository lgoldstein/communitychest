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
import java.util.List;
import java.util.Set;

import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 5, 2013 11:13:55 AM
 */
public enum BinaryArrayTypeEnumeration {
	Single(0, false),
	Jagged(1, false),
	Rectangular(2, false),
	SingleOffset(3, true),
	JaggedOffset(4, true),
	RectangularOffset(5, true);
	
	private final byte	_value;
	public final byte getValue()
	{
		return _value;
	}

	private final boolean	_offsetArray;
	public final boolean isOffsetArray ()
	{
		return _offsetArray;
	}
	public final void write (OutputStream out) throws IOException
	{
		if (out == null)
			throw new IOException("No output stream");
		DataFormatConverter.writeUnsignedByte(out, (short) (getValue() & 0xFF));
	}

	public List<Integer> readLowerBounds(int rank, InputStream in) throws IOException
	{
		if ((rank < 0) || (in == null))
			throw new IOException("readLowerBounds(" + rank + ") bad arguments");
		if (isOffsetArray())
			return Collections.emptyList();
		else
			return DataFormatConverter.readSignedInt32List(rank, in);
	}

	BinaryArrayTypeEnumeration(int value, boolean offsetArray)
	{
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE))
			throw new IllegalStateException("Bad value: " + value);

		_value = (byte) (value & 0xFF);
		_offsetArray = offsetArray;
	}

	public static final Set<BinaryArrayTypeEnumeration>	VALUES=
			Collections.unmodifiableSet(EnumSet.allOf(BinaryArrayTypeEnumeration.class));
	
	public static final BinaryArrayTypeEnumeration read(InputStream in) throws IOException
	{
		short	typeValue=DataFormatConverter.readUnsignedByte(in);
		BinaryArrayTypeEnumeration	enumValue=fromValue(typeValue);
		if (enumValue == null)
			throw new StreamCorruptedException("Unknown " + BinaryArrayTypeEnumeration.class.getSimpleName() + " value: " + typeValue);
		
		return enumValue;
	}

	public static final void write (BinaryArrayTypeEnumeration enumValue, OutputStream out) throws IOException
	{
		if (enumValue == null)
			throw new StreamCorruptedException("No enum value to write");
		enumValue.write(out);
	}

	public static final BinaryArrayTypeEnumeration fromValue(int value)
	{
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE))
			return null;
		
		for(BinaryArrayTypeEnumeration recType : VALUES)
		{
			if (value == recType.getValue())
				return recType;
		}
		
		return null;
	}

}
