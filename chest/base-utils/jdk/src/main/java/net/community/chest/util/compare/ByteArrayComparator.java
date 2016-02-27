/*
 * 
 */
package net.community.chest.util.compare;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2010 1:15:52 PM
 */
public class ByteArrayComparator extends AbstractComparator<byte[]> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7414213958002480647L;

	public ByteArrayComparator (boolean ascending) throws IllegalArgumentException
	{
		super(byte[].class, !ascending);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (byte[] v1, byte[] v2)
	{
		if (v1 == v2)
			return 0;

		final int	l1=(null == v1) ? 0 : v1.length,
					l2=(null == v2) ? 0 : v2.length;
		if (l1 <= 0)	// push null(s) to end
			return (l2 <= 0) ? 0 : (+1);
		else if (l2 <= 0)
			return (-1);

		final int	cLen=Math.min(l1, l2);
		for (int	vIndex=0; vIndex < cLen; vIndex++)
		{
			final byte	b1=v1[vIndex], b2=v2[vIndex];
			if (b1 != b2)
				return b1 - b2;
		}

		return l1 - l2;	// shortest comes first
	}

	public static final int getHashCode (final byte[] data, final int off, final int len)
	{
		if (len <= 0)
			return 0;

		int	hashCode=0;
		for (int	dIndex=off; dIndex < (off+len); dIndex++)
			hashCode += (data[dIndex] & 0x00FF);
		return hashCode;
	}

	public static final int getHashCode (final byte ... data)
	{
		return getHashCode(data, 0, (null == data) ? 0 : data.length);
	}

	public static final ByteArrayComparator	ASCENDING=new ByteArrayComparator(true),
											DESCENDING=new ByteArrayComparator(false);
}
