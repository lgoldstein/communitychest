/*
 * 
 */
package net.community.chest.net.jni;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 23, 2009 9:24:14 AM
 */
public class Sockaddr implements Cloneable {
	private short	_family;
	public short getFamily ()
	{
		return _family;
	}

	public void setFamily (short family)
	{
		_family = family;
	}

	public static final int	SA_DATA_LEN=14;
	private byte[]	_data;
	public byte[] getData ()
	{
		return _data;
	}

	public void setData (byte[] data)
	{
		_data = data;
	}
	// NOTE !!! specifies only the length of valid data in the data buffer
	private int	_len;
	public int getDataLength ()
	{
		return _len;
	}

	public void setDataLength (int len)
	{
		_len = len;
	}

	public Sockaddr (short family, byte[] data, int len)
	{
		_family = family;
		_data = data;
		_len = len;
	}
	
	public Sockaddr (short family, byte ... data)
	{
		this(family, data, (null == data) ? 0 : data.length);
	}

	public Sockaddr ()
	{
		super();
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Sockaddr clone () throws CloneNotSupportedException
	{
		final Sockaddr	a=getClass().cast(super.clone());
		final byte[]	d=a.getData();
		if (d != null)
			a.setData(d.clone());
		return a;
	}
}
