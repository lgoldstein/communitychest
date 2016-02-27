/*
 * 
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.community.chest.io.file.FileIOUtils;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 6, 2013 2:53:02 PM
 */
public final class SerializationFormatConverter {
	public static final int MAX_PREFIX_STRING_LEN_BYTES=5;
	public static final long NANOS_PER_TICK=100L;
	public static final int	MAX_LENGTH_PREFIXED_STRING_LIST_LENGTH=0xFFFF;
	public static final int MAX_LENGTH_PREFIXED_STRING_LENGTH=0xFFFF;

	private SerializationFormatConverter ()
	{
		throw new UnsupportedOperationException("No instance");
	}

	public static final List<String> readLengthPrefixedStringList(InputStream in, int count) throws IOException
	{
		if (count <= 0)
			return Collections.emptyList();
		
		/*
		 * NOTE: the standard does not impose this restriction but we
		 * place it here in case the received value is corrupted due to
		 * malformed parsing of the serialization stream.
		 */
		if (count > MAX_LENGTH_PREFIXED_STRING_LIST_LENGTH)
			throw new StreamCorruptedException("Unreasonable string list count: " + count);
		
		List<String>	result=new ArrayList<String>(count);
		for (int index=0; index < count; index++)
		{
			String s=readLengthPrefixedString(in);
			result.add(s);
		}
		
		return result;
	}

	public static final String readLengthPrefixedString(InputStream in) throws IOException
	{
		int	strLen=readLengthPrefixedStringLength(in);
		if (strLen < 0)
			throw new StreamCorruptedException("Bad variable string length: " + strLen);
		
		if (strLen == 0)
			return "";

		/*
		 * NOTE: the standard does not impose this restriction but we
		 * place it here in case the received value is corrupted due to
		 * malformed parsing of the serialization stream.
		 */
		if (strLen > MAX_LENGTH_PREFIXED_STRING_LENGTH)
			throw new StreamCorruptedException("Unreasonable string length: " + strLen);

		byte[]	strBytes=new byte[strLen];
		FileIOUtils.readFully(in, strBytes);
		return new String(strBytes, "UTF-8");
	}
	
	public static final int readLengthPrefixedStringLength(InputStream in) throws IOException
	{
		int	length=0;
		for (int i=0, shiftSize=0; i < MAX_PREFIX_STRING_LEN_BYTES; i++, shiftSize += 7)
		{
			short	lenValue=DataFormatConverter.readUnsignedByte(in);
			int		lenData=lenValue & 0x7F;	// only 7 bits are valid
			if (shiftSize > 0)
				lenData <<= shiftSize;
			length += lenData;
			if ((lenValue & 0x80) == 0)	// check if more length bits
				break;
		}
		
		return length;
	}

	public static final Date readDateValue(InputStream in) throws IOException
	{
		final long	ticks=DataFormatConverter.readSignedInt64(in);
		final long	millis=ticksToMillis(ticks);
		return new Date(millis);
	}

	public static final byte[] writeDateValue(OutputStream out, Date d) throws IOException
	{
		if (d == null)
			throw new StreamCorruptedException("No date value");
		
		return writeDateValue(out, d.getTime());
	}

	public static final byte[] writeDateValue(OutputStream out, long millis) throws IOException
	{
		final long	ticks=millisToTicks(millis);
		return DataFormatConverter.writeInt64(out, ticks);
	}

	public static final long millisToTicks(long millis)
	{
		final long	nanos=TimeUnit.MILLISECONDS.toNanos(millis);
		return nanosToTicks(nanos);
	}

	public static final long ticksToMillis(long ticks)
	{
		final long	nanos=ticksToNanos(ticks);
		return TimeUnit.NANOSECONDS.toMillis(nanos);
	}

	public static final long nanosToTicks(long nanos)
	{
		return nanos / NANOS_PER_TICK;
	}

	public static final long ticksToNanos(long ticks)
	{
		return ticks * NANOS_PER_TICK;
	}

	public static final void writeLengthPrefixedStringList(OutputStream out, Collection<String> l) throws IOException
	{
		if ((l == null) || l.isEmpty())
			return;
		
		for (String s : l)
			writeLengthPrefixedString(out, s);
	}

	public static final void writeLengthPrefixedString(OutputStream out, String s) throws IOException
	{
		if (out == null)
			throw new IOException("No output stream");
		throw new StreamCorruptedException("writeLengthPrefixedString(" + s + ") N/A");
	}
}
