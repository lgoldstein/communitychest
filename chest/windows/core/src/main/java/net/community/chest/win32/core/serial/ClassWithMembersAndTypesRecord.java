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
 * @since Jan 7, 2013 3:17:48 PM
 *
 */
public class ClassWithMembersAndTypesRecord extends AbstractClassWithMembersAndTypes
	   implements PubliclyCloneable<ClassWithMembersAndTypesRecord>,
	   			  ElementEncoder<ClassWithMembersAndTypesRecord> {
	private static final long serialVersionUID = 7713261695057708634L;

	private long _libraryId=(-1L);	// the value must be positive

	public ClassWithMembersAndTypesRecord ()
	{
		super(RecordTypeEnumeration.ClassWithMembersAndTypes);
	}

	public ClassWithMembersAndTypesRecord (InputStream in) throws IOException
	{
		super(RecordTypeEnumeration.ClassWithMembersAndTypes);

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
	public ClassWithMembersAndTypesRecord read (InputStream in) throws IOException
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
	public ClassWithMembersAndTypesRecord clone () throws CloneNotSupportedException
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

		ClassWithMembersAndTypesRecord	other=(ClassWithMembersAndTypesRecord) obj;
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
