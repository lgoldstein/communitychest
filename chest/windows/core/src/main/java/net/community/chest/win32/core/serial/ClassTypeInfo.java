/*
 * 
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 7, 2013 4:20:15 PM
 *
 */
public class ClassTypeInfo implements ElementEncoder<ClassTypeInfo>, PubliclyCloneable<ClassTypeInfo>, Serializable {
	private static final long serialVersionUID = 744018906038342406L;
	private long	_libraryId=(-1L);
	private String _name;
	
	public ClassTypeInfo ()
	{
		super();
	}

	public ClassTypeInfo (InputStream in) throws IOException
	{
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

	public String getName ()
	{
		return _name;
	}

	public void setName (String name)
	{
		_name = name;
	}

	@Override
	public ClassTypeInfo read (InputStream in) throws IOException
	{
		setName(SerializationFormatConverter.readLengthPrefixedString(in));
		setLibraryId(DataFormatConverter.readUnsignedInt32(in));
		return this;
	}

	@Override
	public void write (OutputStream out) throws IOException
	{
		SerializationFormatConverter.writeLengthPrefixedString(out, getName());
		DataFormatConverter.writeUnsignedInt32(out, getLibraryId());
	}

	@Override
	public ClassTypeInfo clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}

	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getName(), true)
			+ (int) getLibraryId()
			;
	}
	
	@Override
	public boolean equals (Object obj)
	{
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		
		ClassTypeInfo	other=(ClassTypeInfo) obj;
		if ((StringUtil.compareDataStrings(getName(), other.getName(), true) == 0)
		 && (getLibraryId() == other.getLibraryId()))
			return true;
		else
			return false;
	}
	
	@Override
	public String toString ()
	{
		return getName() + "@" + getLibraryId();
	}
}
