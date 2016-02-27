/*
 * 
 */
package net.community.chest.apache.maven.helpers;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Compares only group and artifact ID(s) (in this order)</P>
 * @author Lyor G.
 * @since Jul 9, 2009 2:37:14 PM
 */
public class DependencyComparator extends AbstractComparator<BaseTargetDetails> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6045534331510602138L;

	public DependencyComparator (boolean ascending)
	{
		super(BaseTargetDetails.class, !ascending);
	}
	/*
	 * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (final BaseTargetDetails v1, final BaseTargetDetails v2)
	{
		final String[]	vals={
				(null == v1) ? null : v1.getGroupId(), (null == v2) ? null : v2.getGroupId(),
				(null == v1) ? null : v1.getArtifactId(), (null == v2) ? null : v2.getArtifactId()
			};
		for (int	vIndex=0; vIndex < vals.length; vIndex += 2)
		{
			final int	nRes=StringUtil.compareDataStrings(vals[vIndex], vals[vIndex+1], true);
			if (nRes != 0)
				return nRes;
		}

		return 0;
	}

	public static final DependencyComparator	ASCENDING=new DependencyComparator(true),
												DESCENDING=new DependencyComparator(false);
}
