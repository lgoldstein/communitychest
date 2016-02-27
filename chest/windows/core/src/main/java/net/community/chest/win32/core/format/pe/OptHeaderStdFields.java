/*
 * 
 */
package net.community.chest.win32.core.format.pe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

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
 * @since Jun 16, 2009 7:39:53 AM
 */
public class OptHeaderStdFields
				implements Serializable,
						   PubliclyCloneable<OptHeaderStdFields>,
						   ElementEncoder<OptHeaderStdFields> {
	private static final long serialVersionUID = -1547157327800551702L;
	public OptHeaderStdFields ()
	{
		super();
	}

	private short	_magicNumber;
	public short getMagicNumber ()
	{
		return _magicNumber;
	}

	public void setMagicNumber (short magicNumber)
	{
		_magicNumber = magicNumber;
	}

	private short	_majorLinkerVersion;
	public short getMajorLinkerVersion ()
	{
		return _majorLinkerVersion;
	}

	public void setMajorLinkerVersion (short majorLinkerVersion)
	{
		_majorLinkerVersion = majorLinkerVersion;
	}

	private short	_minorLinkerVersion;
	public short getMinorLinkerVersion ()
	{
		return _minorLinkerVersion;
	}

	public void setMinorLinkerVersion (short minorLinkerVersion)
	{
		_minorLinkerVersion = minorLinkerVersion;
	}

	private long	_sizeOfCode;
	public long getSizeOfCode ()
	{
		return _sizeOfCode;
	}

	public void setSizeOfCode (long sizeOfCode)
	{
		_sizeOfCode = sizeOfCode;
	}

	private long	_sizeOfInitializedData;
	public long getSizeOfInitializedData ()
	{
		return _sizeOfInitializedData;
	}

	public void setSizeOfInitializedData (long sizeOfInitializedData)
	{
		_sizeOfInitializedData = sizeOfInitializedData;
	}

	private long	_sizeOfUninitializedData;
	public long getSizeOfUninitializedData ()
	{
		return _sizeOfUninitializedData;
	}

	public void setSizeOfUninitializedData (long sizeOfUninitializedData)
	{
		_sizeOfUninitializedData = sizeOfUninitializedData;
	}

	private long	_addressOfEntryPoint;
	public long getAddressOfEntryPoint ()
	{
		return _addressOfEntryPoint;
	}

	public void setAddressOfEntryPoint (long addressOfEntryPoint)
	{
		_addressOfEntryPoint = addressOfEntryPoint;
	}

	private long	_baseOfCode;
	public long getBaseOfCode ()
	{
		return _baseOfCode;
	}

	public void setBaseOfCode (long baseOfCode)
	{
		_baseOfCode = baseOfCode;
	}
	// valid only for PE32 format
	private long	_baseOfData;
	public long getBaseOfData ()
	{
		return _baseOfData;
	}

	public void setBaseOfData (long baseOfData)
	{
		_baseOfData = baseOfData;
	}

	public void clear ()
	{
		setMagicNumber((short) 0);
		setMajorLinkerVersion((short) 0);
		setMinorLinkerVersion((short) 0);
		setSizeOfCode(0L);
		setSizeOfInitializedData(0L);
		setSizeOfUninitializedData(0L);
		setAddressOfEntryPoint(0L);
		setBaseOfCode(0L);
		setBaseOfData(0L);
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public OptHeaderStdFields clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public OptHeaderStdFields read (InputStream in) throws IOException
	{
		final short	mn=DataFormatConverter.readSignedInt16(in);
		setMagicNumber(mn);
		setMajorLinkerVersion(DataFormatConverter.readUnsignedByte(in));
		setMinorLinkerVersion(DataFormatConverter.readUnsignedByte(in));
		setSizeOfCode(DataFormatConverter.readUnsignedInt32(in));
		setSizeOfInitializedData(DataFormatConverter.readUnsignedInt32(in));
		setSizeOfUninitializedData(DataFormatConverter.readUnsignedInt32(in));
		setAddressOfEntryPoint(DataFormatConverter.readUnsignedInt32(in));
		setBaseOfCode(DataFormatConverter.readUnsignedInt32(in));

		if (PEFormatDetails.PE32_MAGIC_NUMBER == mn)
			setBaseOfData(DataFormatConverter.readUnsignedInt32(in));
		else if (mn != PEFormatDetails.PE32PLUS_MAGIC_NUMBER)
			throw new StreamCorruptedException("Unknown magic number: 0x" + Hex.toString(mn, true));

		return this;
	}

	public OptHeaderStdFields (InputStream in) throws IOException
	{
		final Object	o=read(in);
		if (o != this)
			throw new StreamCorruptedException("Mismatched read data");
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		final short	mn=getMagicNumber();
		DataFormatConverter.writeSignedInt16(out, mn);
		DataFormatConverter.writeUnsignedByte(out, getMajorLinkerVersion());
		DataFormatConverter.writeUnsignedByte(out, getMinorLinkerVersion());
		DataFormatConverter.writeUnsignedInt32(out, getSizeOfCode());
		DataFormatConverter.writeUnsignedInt32(out, getSizeOfInitializedData());
		DataFormatConverter.writeUnsignedInt32(out, getSizeOfUninitializedData());
		DataFormatConverter.writeUnsignedInt32(out, getAddressOfEntryPoint());
		DataFormatConverter.writeUnsignedInt32(out, getBaseOfCode());

		if (PEFormatDetails.PE32_MAGIC_NUMBER == mn)
			DataFormatConverter.writeUnsignedInt32(out, getBaseOfData());
		else if (mn != PEFormatDetails.PE32PLUS_MAGIC_NUMBER)
			throw new StreamCorruptedException("Unknown magic number: 0x" + Hex.toString(mn, true));
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
	    if (obj == null)
	        return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
		    return false;

		final OptHeaderStdFields	h=(OptHeaderStdFields) obj;
		final short					hm=h.getMagicNumber(), tm=getMagicNumber();
		if (hm != tm)
			return false;

		if (PEFormatDetails.PE32_MAGIC_NUMBER == hm)
		{
			if (h.getBaseOfData() != getBaseOfData())
				return false;
		}

		return (h.getAddressOfEntryPoint() == getAddressOfEntryPoint())
			&& (h.getBaseOfCode() == getBaseOfCode())
			&& (h.getMajorLinkerVersion() == getMajorLinkerVersion())
			&& (h.getMinorLinkerVersion() == getMinorLinkerVersion())
			&& (h.getSizeOfCode() == getSizeOfCode())
			&& (h.getSizeOfInitializedData() == getSizeOfInitializedData())
			&& (h.getSizeOfUninitializedData() == getSizeOfUninitializedData())
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final short	mn=getMagicNumber();
		return mn
			 + getMajorLinkerVersion()
			 + getMinorLinkerVersion()
			 + NumberTables.getLongValueHashCode(getSizeOfCode())
			 + NumberTables.getLongValueHashCode(getSizeOfInitializedData())
			 + NumberTables.getLongValueHashCode(getSizeOfUninitializedData())
			 + NumberTables.getLongValueHashCode(getAddressOfEntryPoint())
			 + NumberTables.getLongValueHashCode(getBaseOfCode())
			 + NumberTables.getLongValueHashCode((PEFormatDetails.PE32_MAGIC_NUMBER == mn) ? getBaseOfData() : 0L)
			 ;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final short	mn=getMagicNumber();
		return "magic=0x" + Hex.toString(mn, true) + "[" + OptHeaderType.fromMagicNumber(mn) + "]"
		 	+ "/version=" + getMajorLinkerVersion() + "." + getMinorLinkerVersion()
		 	+ "/code-size=" + getSizeOfCode()
		 	+ "/init-size=" + getSizeOfInitializedData()
		 	+ "/uninit-size=" + getSizeOfUninitializedData()
		 	+ "/entry-point=0x" + Hex.toString((int) getAddressOfEntryPoint(), true)
		 	+ "/codebase=0x" + Hex.toString((int) getBaseOfCode(), true)
		 	+ ((PEFormatDetails.PE32_MAGIC_NUMBER == mn) ? "/basedata=0x" + Hex.toString((int) getBaseOfData(),true) : "")
		 	;
	}
}
