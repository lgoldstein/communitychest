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
 * @since Feb 19, 2013 4:08:04 PM
 *
 */
public class ClassWithMembersRecord extends AbstractClassWithMembers
				implements PubliclyCloneable<ClassWithMembersRecord>,
						   ElementEncoder<ClassWithMembersRecord>{
	private static final long serialVersionUID = 269364539940948728L;

	private long _libraryId=(-1L);	// the value must be positive

	public ClassWithMembersRecord ()
	{
		super(RecordTypeEnumeration.ClassWithMembers);
	}

	public ClassWithMembersRecord (InputStream in) throws IOException
	{
		super(RecordTypeEnumeration.ClassWithMembers);

		Object	result=read(in);
		if (result != this)
			throw new StreamCorruptedException("Mismatched read data instance");
	}

	public long getLibraryId ()
	{
		return _libraryId;
	}

	public void setLibraryId (long libraryId)
	{
		_libraryId = libraryId;
	}

	@Override
	@CoVariantReturn
	public ClassWithMembersRecord read (InputStream in) throws IOException
	{
		return getClass().cast(super.read(in));
	}

	@Override
	public void readRecordData (InputStream in) throws IOException
	{
		super.readRecordData(in);
		setLibraryId(DataFormatConverter.readUnsignedInt32(in));
	}

	@Override
	public void writeRecordData (OutputStream out) throws IOException
	{
		super.writeRecordData(out);
		DataFormatConverter.writeUnsignedInt32(out, getLibraryId());
	}

	@Override
	@CoVariantReturn
	public ClassWithMembersRecord clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}

	@Override
	public int hashCode ()
	{
		return super.hashCode()
			+ (int) getLibraryId()
			;
	}

	@Override
	public boolean equals (Object obj)
	{
		if (!super.equals(obj))
			return false;
		if (this == obj)
			return true;

		ClassWithMembersRecord	other=(ClassWithMembersRecord) obj;
		if (getLibraryId() == other.getLibraryId())
			return true;
		else
			return false;
	}

	@Override
	public String toString ()
	{
		return super.toString()
			+ ";libId=" + getLibraryId()
			;
	}

}
