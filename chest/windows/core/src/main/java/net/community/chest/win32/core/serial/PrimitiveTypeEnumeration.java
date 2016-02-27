/*
 * 
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 7, 2013 4:09:54 PM
 *
 */
public enum PrimitiveTypeEnumeration {
	BooleanType(1) {
		@Override
		public Boolean readValue(InputStream in) throws IOException
		{
			final byte	value=DataFormatConverter.readByte(in);
			return Boolean.valueOf(value != 0);
		}		

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Boolean))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeByte(out, ((Boolean) value).booleanValue() ? (byte) 1 : 0);
		}
	},
	ByteType(2) {
		@Override
		public Byte readValue(InputStream in) throws IOException
		{
			return Byte.valueOf(DataFormatConverter.readByte(in));
		}		

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Number))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeByte(out, ((Number) value).byteValue());
		}
	},
	CharType(3),
	ReservedType4(4),
	DecimalType(5),
	DoubleType(6)  {
		@Override
		public Double readValue(InputStream in) throws IOException
		{
			return Double.valueOf(DataFormatConverter.readDouble(in));
		}

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Number))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeDouble(out, ((Number) value).doubleValue());
		}
	},
	Int16Type(7) {
		@Override
		public Short readValue(InputStream in) throws IOException
		{
			return Short.valueOf(DataFormatConverter.readSignedInt16(in));
		}		

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Number))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeSignedInt16(out, ((Number) value).shortValue());
		}
	},
	Int32Type(8) {
		@Override
		public Integer readValue(InputStream in) throws IOException
		{
			return Integer.valueOf(DataFormatConverter.readSignedInt32(in));
		}		

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Number))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeSignedInt32(out, ((Number) value).intValue());
		}
	},
	Int64Type(9) {
		@Override
		public Long readValue(InputStream in) throws IOException
		{
			return Long.valueOf(DataFormatConverter.readSignedInt64(in));
		}

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Number))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeInt64(out, ((Number) value).longValue());
		}
	},
	SByteType(10) {
		@Override
		public Short readValue(InputStream in) throws IOException
		{
			return Short.valueOf(DataFormatConverter.readUnsignedByte(in));
		}

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Number))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeUnsignedByte(out, ((Number) value).shortValue());
		}
	},
	SingleType(11),
	TimeSpanType(12),
	DateTimeType(13) {
		@Override
		public Date readValue(InputStream in) throws IOException
		{
			return SerializationFormatConverter.readDateValue(in);
		}		

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Date))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			SerializationFormatConverter.writeDateValue(out, (Date) value);
		}
	},
	UInt16Type(14) {
		@Override
		public Integer readValue(InputStream in) throws IOException
		{
			return Integer.valueOf(DataFormatConverter.readUnsignedInt16(in));
		}		

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Number))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeUnsignedInt16(out, ((Number) value).intValue());
		}
	},
	UInt32Type(15) {
		@Override
		public Long readValue(InputStream in) throws IOException
		{
			return Long.valueOf(DataFormatConverter.readUnsignedInt32(in));
		}		
		
		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Number))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeUnsignedInt32(out, ((Number) value).longValue());
		}
	},
	UInt64Type(16) {
		@Override
		public Long readValue(InputStream in) throws IOException
		{
			return Long.valueOf(DataFormatConverter.readSignedInt64(in));
		}		

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			if (!(value instanceof Number))
				throw new StreamCorruptedException("Mismatched type: " + value);
			
			DataFormatConverter.writeInt64(out, ((Number) value).longValue());
		}
	},
	NullType(17) {
		@Override
		public Object readValue(InputStream in) throws IOException
		{
			if (in == null)
				throw new IOException("No input stream provided");
			return Void.TYPE;
		}
	},
	StringType(18) {
		@Override
		public String readValue(InputStream in) throws IOException
		{
			return SerializationFormatConverter.readLengthPrefixedString(in);
		}
		

		@Override
		public void writeValue(OutputStream out, Object value) throws IOException
		{
			if (out == null)
				throw new IOException("No output stream provided");
			
			SerializationFormatConverter.writeLengthPrefixedString(out, (value == null) ? "" : value.toString());
		}
	};

	private final byte	_value;
	public final byte getValue()
	{
		return _value;
	}

	public Object readValue(InputStream in) throws IOException
	{
		if (in == null)
			throw new IOException("No input stream provided");
		throw new StreamCorruptedException("read(" + name() + ") not implemented");
	}

	public void writeValue(OutputStream out, Object value) throws IOException
	{
		if (out == null)
			throw new IOException("No output stream provided");
		if (value == null)
			throw new StreamCorruptedException("Null(s) N/A");
		
		throw new StreamCorruptedException("write(" + name() + ")[" + value + "] not implemented");
	}

	public final void write (OutputStream out) throws IOException
	{
		if (out == null)
			throw new IOException("No output stream");
		DataFormatConverter.writeUnsignedByte(out, (short) (getValue() & 0xFF));
	}

	PrimitiveTypeEnumeration(int value)
	{
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE))
			throw new IllegalStateException("Bad value: " + value);
		_value = (byte) (value & 0xFF);
	}

	public static final Set<PrimitiveTypeEnumeration>	VALUES=
			Collections.unmodifiableSet(EnumSet.allOf(PrimitiveTypeEnumeration.class));
	
	public static final PrimitiveTypeEnumeration read(InputStream in) throws IOException
	{
		short	typeValue=DataFormatConverter.readUnsignedByte(in);
		PrimitiveTypeEnumeration	enumValue=fromValue(typeValue);
		if (enumValue == null)
			throw new StreamCorruptedException("Unknown type " + PrimitiveTypeEnumeration.class.getSimpleName() + " value: " + typeValue);
		
		return enumValue;
	}

	public static final void write (PrimitiveTypeEnumeration enumValue, OutputStream out) throws IOException
	{
		if (enumValue == null)
			throw new StreamCorruptedException("No enum value to write");
		enumValue.write(out);
	}

	public static final PrimitiveTypeEnumeration fromValue(int value)
	{
		if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE))
			return null;
		
		for(PrimitiveTypeEnumeration recType : VALUES)
		{
			if (value == recType.getValue())
				return recType;
		}
		
		return null;
	}
}
