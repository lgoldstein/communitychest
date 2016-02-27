/*
 * 
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 19, 2013 3:59:05 PM
 *
 */
public class ObjectNullMultiple256Record extends SerializationRecord
				implements PubliclyCloneable<ObjectNullMultiple256Record>,
						   ElementEncoder<ObjectNullMultiple256Record>{
	private static final long serialVersionUID = 1996923559485206152L;

	private short _nullCount;

	public ObjectNullMultiple256Record ()
	{
		super(RecordTypeEnumeration.ObjectNullMultiple256);
	}

	public ObjectNullMultiple256Record (InputStream in) throws IOException
	{
		super(RecordTypeEnumeration.ObjectNullMultiple256);

		Object	result=read(in);
		if (result != this)
			throw new StreamCorruptedException("Mismatched read data instance");
	}

	public short getNullCount ()
	{
		return _nullCount;
	}

	public void setNullCount (short nullCount)
	{
		_nullCount = nullCount;
	}

	@Override
	@CoVariantReturn
	public ObjectNullMultiple256Record read (InputStream in) throws IOException
	{
		return getClass().cast(super.read(in));
	}

	@Override
	public void readRecordData (InputStream in) throws IOException
	{
		setNullCount(DataFormatConverter.readUnsignedByte(in));
		logInternal("Count=" + getNullCount());
	}

	@Override
	public void writeRecordData (OutputStream out) throws IOException
	{
		DataFormatConverter.writeUnsignedByte(out, getNullCount());
	}

	@Override
	public ObjectNullMultiple256Record clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	@Override
	public int hashCode ()
	{
		return super.hashCode() + getNullCount();
	}
	
	@Override
	public boolean equals (Object obj)
	{
		if (!super.equals(obj))
			return false;
		if (this == obj)
			return true;
		
		ObjectNullMultiple256Record	other=(ObjectNullMultiple256Record) obj;
		if (getNullCount() == other.getNullCount())
			return true;
		else
			return false;
	}

	@Override
	public String toString ()
	{
		return super.toString() + ";count=" + getNullCount();
	}

}
