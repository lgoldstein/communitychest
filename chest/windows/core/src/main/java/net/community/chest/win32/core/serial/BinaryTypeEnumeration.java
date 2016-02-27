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

import net.community.chest.CoVariantReturn;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 7, 2013 3:49:51 PM
 *
 */
public enum BinaryTypeEnumeration {
	PrimitiveType(0) {
		@Override
		@CoVariantReturn
		public PrimitiveTypeEnumeration readAdditionalInfo(InputStream in) throws IOException
		{
			return PrimitiveTypeEnumeration.read(in);
		}
		
		@Override
		public void writeAdditionalInfo(OutputStream out, Object extraInfo) throws IOException
		{
			if (extraInfo instanceof PrimitiveTypeEnumeration)
				((PrimitiveTypeEnumeration) extraInfo).write(out);
			else
				throw new StreamCorruptedException("writeAdditionalInfo(" + name() + ")[" + extraInfo + "] unexpected object type");
		}
	},
	StringType(1),
	ObjectType(2),
	SystemClassType(3) {
		@Override
		@CoVariantReturn
		public String readAdditionalInfo(InputStream in) throws IOException
		{
			return SerializationFormatConverter.readLengthPrefixedString(in);
		}

		@Override
		public void writeAdditionalInfo(OutputStream out, Object extraInfo) throws IOException
		{
			if (extraInfo instanceof String)
				SerializationFormatConverter.writeLengthPrefixedString(out, (String) extraInfo);
			else
				throw new StreamCorruptedException("writeAdditionalInfo(" + name() + ")[" + extraInfo + "] unexpected object type");
		}
	},
	ClassType(4) {
		@Override
		@CoVariantReturn
		public ClassTypeInfo readAdditionalInfo(InputStream in) throws IOException
		{
			return new ClassTypeInfo(in);
		}
		
		@Override
		public void writeAdditionalInfo(OutputStream out, Object extraInfo) throws IOException
		{
			if (extraInfo instanceof ClassTypeInfo)
				((ClassTypeInfo) extraInfo).write(out);
			else
				throw new StreamCorruptedException("writeAdditionalInfo(" + name() + ")[" + extraInfo + "] unexpected object type");
		}
	},
	ObjectArrayType(5),
	StringArrayType(6),
	PrimitiveArrayType(7) {
		@Override
		@CoVariantReturn
		public PrimitiveTypeEnumeration readAdditionalInfo(InputStream in) throws IOException
		{
			return PrimitiveTypeEnumeration.read(in);
		}
		
		@Override
		public void writeAdditionalInfo(OutputStream out, Object extraInfo) throws IOException
		{
			if (extraInfo instanceof PrimitiveTypeEnumeration)
				((PrimitiveTypeEnumeration) extraInfo).write(out);
			else
				throw new StreamCorruptedException("writeAdditionalInfo(" + name() + ")[" + extraInfo + "] unexpected object type");
		}
	};
	
	private final byte	_value;
	public final byte getValue()
	{
		return _value;
	}
	
	public Object readAdditionalInfo(InputStream in) throws IOException
	{
		if (in == null)
			throw new IOException("No input stream");
		return this;
	}

	public void writeAdditionalInfo(OutputStream out, Object extraInfo) throws IOException
	{
		if (out == null)
			throw new IOException("writeAdditionalInfo(" + name() + ")[" + extraInfo + "] no input stream");
		if (extraInfo != null)
			return;	// debug breakpoint
	}

	public final void write (OutputStream out) throws IOException
	{
		if (out == null)
			throw new IOException("No output stream");
		DataFormatConverter.writeUnsignedByte(out, (short) (getValue() & 0xFF));
	}

	BinaryTypeEnumeration(int value)
	{
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE))
			throw new IllegalStateException("Bad value: " + value);
		_value = (byte) (value & 0xFF);
	}

	public static final Set<BinaryTypeEnumeration>	VALUES=
			Collections.unmodifiableSet(EnumSet.allOf(BinaryTypeEnumeration.class));
	
	public static final BinaryTypeEnumeration read(InputStream in) throws IOException
	{
		short	typeValue=DataFormatConverter.readUnsignedByte(in);
		BinaryTypeEnumeration	enumValue=fromValue(typeValue);
		if (enumValue == null)
			throw new StreamCorruptedException("Unknown " + BinaryTypeEnumeration.class.getSimpleName() + " value: " + typeValue);
		
		return enumValue;
	}

	public static final void write (BinaryTypeEnumeration enumValue, OutputStream out) throws IOException
	{
		if (enumValue == null)
			throw new StreamCorruptedException("No enum value to write");
		enumValue.write(out);
	}

	public static final BinaryTypeEnumeration fromValue(int value)
	{
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE))
			return null;
		
		for(BinaryTypeEnumeration recType : VALUES)
		{
			if (value == recType.getValue())
				return recType;
		}
		
		return null;
	}
}
