/*
 * 
 */
package net.community.chest.win32.core.format.pe.rsrc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2009 3:45:57 PM
 */
public class ResourceDirectoryString
					implements Serializable,
							   PubliclyCloneable<ResourceDirectoryString>,
							   ElementEncoder<ResourceDirectoryString> {
	private static final long serialVersionUID = -4247165312727727991L;

	public ResourceDirectoryString ()
	{
		super();
	}

	private int	_length;
	public int getLength ()
	{
		return _length;
	}

	public void setLength (int length)
	{
		_length = length;
	}

	private String	_text;
	public String getText ()
	{
		return _text;
	}

	public void setText (String text)
	{
		_text = text;
	}

	public static final String readDefaultText (final InputStream in, final int len) throws IOException
	{
		if (len <= 0)
			return "";

		if ((len & 1) != 0)
			throw new StreamCorruptedException("readDefaultText(len=" + len + ") not WORD aligned");

		final byte[]	bytes=new byte[len];
		final int		readLen=in.read(bytes);
		if (readLen != len)
			throw new StreamCorruptedException("readDefaultText(len=" + len + ") got only " + readLen + " bytes");

		return DataFormatConverter.toUnicodeString(bytes);
	}

	protected String readText (final InputStream in, final int len) throws IOException
	{
		return readDefaultText(in, len);
	}

	protected void writeText (final OutputStream out, final int len, final String s) throws IOException
	{
		// TODO implement this
		throw new StreamCorruptedException("writeText(" + s + ")[" + len + "]=>" + out + " - N/A");
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public ResourceDirectoryString read (InputStream in) throws IOException
	{
		final int	len=DataFormatConverter.readUnsignedInt16(in);
		setLength(len);
		setText(readText(in, len));
		return this;
	}

	public void clear ()
	{
		setLength(0);
		setText(null);
	}

	public ResourceDirectoryString (InputStream in) throws IOException
	{
		final Object	o=read(in);
		if (o != this)
			throw new StreamCorruptedException("Mismatched read entries");
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		final int	len=getLength();
		DataFormatConverter.writeUnsignedInt16(out, len);
		writeText(out, len, getText());
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public ResourceDirectoryString clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof ResourceDirectoryString))
			return false;
		if (this == obj)
			return true;

		final ResourceDirectoryString	s=(ResourceDirectoryString) obj;
		return (s.getLength() == getLength())
			&& (0 == StringUtil.compareDataStrings(s.getText(), getText(), true))
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return getLength() + StringUtil.getDataStringHashCode(getText(), true);
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final String	t=getText();
		return (null == t) ? "" : t;
	}
}
