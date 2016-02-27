/*
 * 
 */
package net.community.chest.io.zip;

import java.util.zip.ZipEntry;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 7, 2011 1:29:30 PM
 */
public class FullDataZipEntryComparator extends AbstractZipEntryComparator<ZipEntry> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2777056753310216483L;

	public FullDataZipEntryComparator (boolean ascending)
	{
		super(ZipEntry.class, !ascending);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (ZipEntry v1, ZipEntry v2)
	{
		if (v1 == v2)
			return 0;

		int	nRes=compareByName(v1, v2, true);
		if (nRes != 0)
			return nRes;
		if ((nRes=compareBySize(v1, v2)) != 0)
			return nRes;
		if ((nRes=compareByComment(v1, v2, false)) != 0)
			return nRes;

		return 0;
	}

	public static final FullDataZipEntryComparator	ASCENDING=new FullDataZipEntryComparator(true),
													DESCENDING=new FullDataZipEntryComparator(false);
}
