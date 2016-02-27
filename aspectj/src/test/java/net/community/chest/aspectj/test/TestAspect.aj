/*
 * 
 */
package net.community.chest.aspectj.test;

import java.text.DateFormat;
import java.util.Date;

import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.LongsComparator;

import org.aspectj.lang.JoinPoint;

/**
 * @author Lyor G.
 * @since Jul 19, 2010 8:55:02 AM
 *
 */
public aspect TestAspect {
	// add a hidden field
	private long TestObject.creationDate=System.currentTimeMillis();

	// add Comparable implementation
	declare parents: TestObject implements Comparable<TestObject>;
	public int TestObject.compareTo (TestObject other)
	{
		if (this == other)
			return 0;

		if (null == other)
			return (-1);

		final String	tv=this.getValue(), ov=other.getValue();
		final int		nRes=StringUtil.compareDataStrings(tv, ov, true);
		if (nRes != 0)
			return nRes;

		return LongsComparator.compare(this.creationDate, other.creationDate);
	}

	protected static final String adjustReturnValue (
			final JoinPoint jp, final Object orgRetval)
	{
		final TestObject	o=(TestObject) jp.getTarget();
		final Date			d=new Date(o.creationDate);
		final DateFormat	df=DateFormat.getDateTimeInstance();
		final String		dv;
		synchronized(df)
		{
			dv = df.format(d);
		}

		return String.valueOf(orgRetval) + " - " + dv;
	}

	// replace "toString()" result
	String around(TestObject anObject)
		: execution(String TestObject.toString()) && target(anObject)
	{
		return adjustReturnValue(thisJoinPoint, proceed(anObject));
	}
}
