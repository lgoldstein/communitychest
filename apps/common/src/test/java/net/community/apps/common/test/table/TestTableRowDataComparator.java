/*
 * 
 */
package net.community.apps.common.test.table;

import net.community.chest.lang.math.LongsComparator;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 22, 2009 10:57:00 AM
 */
public class TestTableRowDataComparator extends AbstractComparator<TestTableRowData> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2445461350937242065L;

	public TestTableRowDataComparator ()
	{
		super(TestTableRowData.class, false);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (TestTableRowData v1, TestTableRowData v2)
	{
		if (v1 == v2)
			return 0;
		else if (null == v1)
			return (+1);
		else if (null == v2)
			return (-1);

		final int	s1=v1.size(), s2=v2.size();
		if (s1 != s2)
			return s1 - s2;

		return LongsComparator.compare(v1.getTimestamp(), v2.getTimestamp());
	}

	public static final TestTableRowDataComparator	DEFAULT=new TestTableRowDataComparator();
}
