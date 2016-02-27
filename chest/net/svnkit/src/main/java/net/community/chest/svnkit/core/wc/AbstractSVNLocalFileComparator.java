/*
 * 
 */
package net.community.chest.svnkit.core.wc;

import java.io.File;
import java.util.Comparator;

import net.community.chest.util.compare.InstancesComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 11:33:18 AM
 */
public class AbstractSVNLocalFileComparator
			extends AbstractSVNLocalCopyDataComparator {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6546629407119778956L;
	private final Comparator<File>	_c;
	public final Comparator<File> getComparator ()
	{
		return _c;
	}

	protected AbstractSVNLocalFileComparator (Comparator<File> c, boolean reverseMatch)
	{
		super(reverseMatch);

		_c = c;
	}

	public int compareFiles (File f1, File f2)
	{
		final Comparator<File>	c=getComparator();
		if (c != null)
			return c.compare(f1, f2);
		else
			return InstancesComparator.compareGeneralObjects(File.class, f1, f2);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (SVNLocalCopyData v1, SVNLocalCopyData v2)
	{
		return compareFiles((null == v1) ? null : v1.getFile(), (null == v2) ? null : v2.getFile());
	}
}
