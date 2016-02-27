/*
 * 
 */
package net.community.chest.win32.core.format.pe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.ArraysUtils;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2009 8:28:51 AM
 */
public class PEHeaderData
			implements Serializable,
					   PubliclyCloneable<PEHeaderData>,
					   ElementEncoder<PEHeaderData> {
	private static final long serialVersionUID = 3218678964977954949L;

	public PEHeaderData ()
	{
		super();
	}

	public PEHeaderData (InputStream inStream) throws IOException {
	    Object result=read(inStream);
	    if (result != this) {
	        throw new StreamCorruptedException("Mismatched re-constructed instance");
	    }
	}

	private COFFFileHeader	_coffHeader;
	public COFFFileHeader getCoffHeader ()
	{
		return _coffHeader;
	}

	public void setCoffHeader (COFFFileHeader coffHeader)
	{
		_coffHeader = coffHeader;
	}

	private OptHeaderStdFields	_stdHeaderFields;
	public OptHeaderStdFields getStdHeaderFields ()
	{
		return _stdHeaderFields;
	}

	public void setStdHeaderFields (OptHeaderStdFields stdHeaderFields)
	{
		_stdHeaderFields = stdHeaderFields;
	}

	private OptHeaderWin32Fields	_winHeaderFields;
	public OptHeaderWin32Fields getWinHeaderFields ()
	{
		return _winHeaderFields;
	}

	public void setWinHeaderFields (OptHeaderWin32Fields winHeaderFields)
	{
		_winHeaderFields = winHeaderFields;
	}

	protected byte[] readSignature (InputStream in) throws IOException
	{
		return readPESignature(in);
	}

	protected void writeSignature (OutputStream out, byte ... sig) throws IOException
	{
		out.write(sig);
	}

	private byte[]	_peSignature;
	public byte[] getSignature ()
	{
		return _peSignature;
	}

	public void setSignature (byte ... peSignature)
	{
		_peSignature = peSignature;
	}

	private List<SectionTableEntry>	_sections;
	public List<SectionTableEntry> getSections ()
	{
		return _sections;
	}

	public void setSections (List<SectionTableEntry> sections)
	{
		_sections = sections;
	}

	protected List<SectionTableEntry> readSections (
			final InputStream	in, final int	numSections) throws IOException
	{
		return SectionTableEntry.readSections(in, numSections);
	}

	protected void writeSections (
			final OutputStream out, final List<SectionTableEntry> sl) throws IOException
	{
		SectionTableEntry.writeSections(out, sl);
	}
	
	public PEHeaderData read (InputStream in, boolean readSections) throws IOException
	{
		setSignature(readSignature(in));

		final COFFFileHeader	coffHdr=new COFFFileHeader(in);
		setCoffHeader(coffHdr);

		final OptHeaderStdFields	stdHdr=new OptHeaderStdFields(in);
		setStdHeaderFields(stdHdr);

		final OptHeaderWin32Fields	winHdr=new OptHeaderWin32Fields(stdHdr.getMagicNumber());
		setWinHeaderFields(winHdr.read(in));

		if (readSections)
			setSections(readSections(in, coffHdr.getNumSections()));
		return this;
	}
	/* NOTE: assumes stream is positioned just before the PE signature bytes
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public PEHeaderData read (InputStream in) throws IOException
	{
		return read(in, true);
	}

	public void clear ()
	{
		setSignature(null);
		setCoffHeader(null);
		setStdHeaderFields(null);
		setWinHeaderFields(null);
		setSections(null);
	}

	public void write (OutputStream out, boolean writeSections) throws IOException
	{
		writeSignature(out, getSignature());
		getCoffHeader().write(out);
		getStdHeaderFields().write(out);

		if (writeSections)
			writeSections(out, getSections());
	}
	/* NOTE: assumes stream is positioned just before the PE signature bytes
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		write(out, true);
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof PEHeaderData))
			return false;
		if (this == obj)
			return true;

		final PEHeaderData	h=(PEHeaderData) obj;
		return AbstractComparator.compareObjects(h.getCoffHeader(), getCoffHeader())
			&& AbstractComparator.compareObjects(h.getStdHeaderFields(), getStdHeaderFields())
			&& AbstractComparator.compareObjects(h.getWinHeaderFields(), getWinHeaderFields())
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return ClassUtil.getObjectHashCode(getCoffHeader())
			 + ClassUtil.getObjectHashCode(getStdHeaderFields())
			 + ClassUtil.getObjectHashCode(getWinHeaderFields())
			 ;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public PEHeaderData clone () throws CloneNotSupportedException
	{
		final PEHeaderData	ret=getClass().cast(super.clone());
		ret.setSignature(ArraysUtils.cloneArray(getSignature()));
		ret.setCoffHeader(ClassUtil.clonePublic(getCoffHeader()));
		ret.setStdHeaderFields(ClassUtil.clonePublic(getStdHeaderFields()));
		ret.setWinHeaderFields(ClassUtil.clonePublic(getWinHeaderFields()));
		return ret;
	}

	public static final byte[]  PE_SIGNATURE={ 'P', 'E', 0, 0 };
    // assumes input stream positioned correctly
    public static final byte[] readPESignature (final InputStream in) throws IOException
    {
        final byte[]    sigVal=new byte[PE_SIGNATURE.length];
        final int       readLen=in.read(sigVal);
        if (readLen != PE_SIGNATURE.length)
            throw new IOException("Failed to read PE signature - expected=" + PE_SIGNATURE.length + "/got=" + readLen + " bytes");

        for (int    sIndex=0; sIndex < PE_SIGNATURE.length; sIndex++)
        {
            if (sigVal[sIndex] != PE_SIGNATURE[sIndex])
                throw new StreamCorruptedException("Bad signature value at index=" + sIndex + " - expected=" + PE_SIGNATURE[sIndex] + "/got=" + sigVal[sIndex]);
        }

        return sigVal;
    }

    public static final void writeSignature (final OutputStream out) throws IOException
    {
        out.write(PE_SIGNATURE);
    }
}
