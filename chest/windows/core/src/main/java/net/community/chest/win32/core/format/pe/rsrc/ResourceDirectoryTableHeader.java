/*
 * 
 */
package net.community.chest.win32.core.format.pe.rsrc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.io.encode.hex.Hex;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2009 2:26:12 PM
 */
public class ResourceDirectoryTableHeader
				implements Serializable,
						   PubliclyCloneable<ResourceDirectoryTableHeader>,
						   ElementEncoder<ResourceDirectoryTableHeader> {
	private static final long serialVersionUID = -3776967108010057489L;
	/**
	 * The default name of the COFF section for the resources
	 */
	public static final String	DEFAULT_RESOURCE_SECTION_NAME=".rsrc";

	public ResourceDirectoryTableHeader ()
	{
		super();
	}

	private int	_characteristics;
	public int getCharacteristics ()
	{
		return _characteristics;
	}

	public void setCharacteristics (int characteristics)
	{
		_characteristics = characteristics;
	}

	private long	_timeDateStamp;
	public long getTimeDateStamp ()
	{
		return _timeDateStamp;
	}

	public void setTimeDateStamp (long timeDateStamp)
	{
		_timeDateStamp = timeDateStamp;
	}

	private int	_majorVersion;
	public int getMajorVersion ()
	{
		return _majorVersion;
	}

	public void setMajorVersion (int majorVersion)
	{
		_majorVersion = majorVersion;
	}

	private int	_minorVersion;
	public int getMinorVersion ()
	{
		return _minorVersion;
	}

	public void setMinorVersion (int minorVersion)
	{
		_minorVersion = minorVersion;
	}

	private int	_numOfNameEntries;
	public int getNumOfNameEntries ()
	{
		return _numOfNameEntries;
	}

	public void setNumOfNameEntries (int numOfNameEntries)
	{
		_numOfNameEntries = numOfNameEntries;
	}

	private int	_numOfIDEntries;
	public int getNumOfIDEntries ()
	{
		return _numOfIDEntries;
	}

	public void setNumOfIDEntries (int numOfIDEntries)
	{
		_numOfIDEntries = numOfIDEntries;
	}

	private List<ResourceDirectoryEntry>	_nameEntries;
	public List<ResourceDirectoryEntry> getNameEntries ()
	{
		return _nameEntries;
	}

	public void setNameEntries (List<ResourceDirectoryEntry> nameEntries)
	{
		_nameEntries = nameEntries;
	}

	protected List<ResourceDirectoryEntry> readNameEntries (
			final InputStream in, final int numEntries) throws IOException
	{
		return ResourceDirectoryEntry.readEntries(in, numEntries);
	}

	protected void writeNameEntries (
			final OutputStream out, final List<ResourceDirectoryEntry> el) throws IOException
	{
		ResourceDirectoryEntry.writeEntries(out, el);
	}

	private List<ResourceDirectoryEntry>	_idEntries;
	public List<ResourceDirectoryEntry> getIdEntries ()
	{
		return _idEntries;
	}

	public void setIdEntries (List<ResourceDirectoryEntry> idEntries)
	{
		_idEntries = idEntries;
	}

	protected List<ResourceDirectoryEntry> readIdEntries (
			final InputStream in, final int numEntries) throws IOException
	{
		return ResourceDirectoryEntry.readEntries(in, numEntries);
	}

	protected void writeIdEntries (
			final OutputStream out, final List<ResourceDirectoryEntry> el) throws IOException
	{
		ResourceDirectoryEntry.writeEntries(out, el);
	}

	public ResourceDirectoryTableHeader read (final InputStream in, final boolean readEntries) throws IOException
	{
		setCharacteristics(DataFormatConverter.readSignedInt32(in));
		setTimeDateStamp(DataFormatConverter.readUnsignedInt32(in));
		setMajorVersion(DataFormatConverter.readUnsignedInt16(in));
		setMinorVersion(DataFormatConverter.readUnsignedInt16(in));

		final int	numNames=DataFormatConverter.readUnsignedInt16(in);
		setNumOfNameEntries(numNames);

		final int	numIDs=DataFormatConverter.readUnsignedInt16(in);
		setNumOfIDEntries(numIDs);

		if (readEntries)
		{
			setNameEntries(readNameEntries(in, numNames));
			setIdEntries(readIdEntries(in, numIDs));
		}

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public ResourceDirectoryTableHeader read (final InputStream in) throws IOException
	{
		return read(in, true);
	}

	public void clear ()
	{
		setCharacteristics(0);
		setTimeDateStamp(0);
		setMajorVersion(0);
		setMinorVersion(0);

		setNumOfNameEntries(0);
		setNumOfIDEntries(0);

		setNameEntries(null);
		setIdEntries(null);
	}

	public ResourceDirectoryTableHeader (InputStream in) throws IOException
	{
		final Object	o=read(in);
		if (o != this)
			throw new StreamCorruptedException("Mismatched read enitites");
	}

	public void write (final OutputStream out, final boolean writeEntries) throws IOException
	{
		DataFormatConverter.writeSignedInt32(out, getCharacteristics());
		DataFormatConverter.writeUnsignedInt32(out, getTimeDateStamp());
		DataFormatConverter.writeUnsignedInt16(out, getMajorVersion());
		DataFormatConverter.writeUnsignedInt16(out, getMinorVersion());
		DataFormatConverter.writeUnsignedInt16(out, getNumOfNameEntries());
		DataFormatConverter.writeUnsignedInt16(out, getNumOfIDEntries());

		if (writeEntries)
		{
			writeNameEntries(out, getNameEntries());
			writeIdEntries(out, getIdEntries());
		}
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		write(out, true);
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public ResourceDirectoryTableHeader clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof ResourceDirectoryTableHeader))
			return false;
		if (this == obj)
			return true;

		final ResourceDirectoryTableHeader	h=(ResourceDirectoryTableHeader) obj;
		return (h.getCharacteristics() == getCharacteristics())
			&& (h.getMajorVersion() == getMajorVersion())
			&& (h.getMinorVersion() == getMinorVersion())
			&& (h.getNumOfIDEntries() == getNumOfIDEntries())
			&& (h.getNumOfNameEntries() == getNumOfNameEntries())
			&& (h.getTimeDateStamp() == getTimeDateStamp())
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return getCharacteristics()
			 + getMajorVersion()
			 + getMinorVersion()
			 + getNumOfIDEntries()
			 + getNumOfNameEntries()
			 + NumberTables.getLongValueHashCode(getTimeDateStamp())
		;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final long			tsVal=getTimeDateStamp();			
		final DateFormat	dtf=(0L == tsVal) ? null : DateFormat.getDateTimeInstance();
		final String		ct;
		if (dtf != null)
		{
			synchronized(dtf)
			{
				ct = dtf.format(new Date(DataFormatConverter.toJVMTimestamp(tsVal)));
			}
		}
		else
			ct = String.valueOf(tsVal);

		return "flags=0x" + Hex.toString(getCharacteristics(), true)
		 	+ "/version="+ getMajorVersion() + "." + getMinorVersion()
		 	+ "/#IDs=" + getNumOfIDEntries()
		 	+ "/#names=" + getNumOfNameEntries()
		 	+ "/time=" + ct
			;
	}
}
