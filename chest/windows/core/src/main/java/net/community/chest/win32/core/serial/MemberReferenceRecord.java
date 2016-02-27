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
 * @since Jan 7, 2013 4:37:04 PM
 *
 */
public class MemberReferenceRecord extends SerializationRecord
	   implements PubliclyCloneable<MemberReferenceRecord>,
	   			  ElementEncoder<MemberReferenceRecord> {
	private static final long serialVersionUID = 5637623745581385976L;

	private long _reference;

	public MemberReferenceRecord ()
	{
		super(RecordTypeEnumeration.MemberReference);
	}

	public MemberReferenceRecord (InputStream in) throws IOException
	{
		super(RecordTypeEnumeration.MemberReference);

		Object	result=read(in);
		if (result != this)
			throw new StreamCorruptedException("Mismatched read data instance");
	}

	public long getReference ()
	{
		return _reference;
	}

	public void setReference (long reference)
	{
		_reference = reference;
	}

	@Override
	@CoVariantReturn
	public MemberReferenceRecord read (InputStream in) throws IOException
	{
		return getClass().cast(super.read(in));
	}

	@Override
	public void readRecordData (InputStream in) throws IOException
	{
		final long	refId=DataFormatConverter.readUnsignedInt32(in);
		if (refId <= 0L)
			throw new StreamCorruptedException("Illegal reference value: " + refId);
		setReference(refId);
		logInternal("Reference=" + refId);
	}

	@Override
	public void writeRecordData (OutputStream out) throws IOException
	{
		DataFormatConverter.writeUnsignedInt32(out, getReference());
	}

	@Override
	public MemberReferenceRecord clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	@Override
	public int hashCode ()
	{
		return super.hashCode()
		    + (int) getReference();
	}
	
	@Override
	public boolean equals (Object obj)
	{
		if (!super.equals(obj))
			return false;
		if (this == obj)
			return true;
		
		MemberReferenceRecord	other=(MemberReferenceRecord) obj;
		if (getReference() == other.getReference())
			return true;
		else
			return false;
	}

	@Override
	public String toString ()
	{
		return super.toString() + "@" + getReference();
	}
}
