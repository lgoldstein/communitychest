/*
 * 
 */
package net.community.chest.jfree.jfreechart.data.time;

import java.util.List;
import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;

import org.jfree.data.time.RegularTimePeriod;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 6, 2009 9:12:12 AM
 */
public class RegularTimePeriodValueStringInstantiator extends AbstractXmlValueStringInstantiator<RegularTimePeriod> {
	public RegularTimePeriodValueStringInstantiator ()
	{
		super(RegularTimePeriod.class);
	}

	public static final String toString (RegularTimePeriod inst)
	{
		if (null == inst)
			return null;
		
		final RegularTimePeriodType	t=RegularTimePeriodType.fromObject(inst);
		if (null == t)
			throw new NoSuchElementException("toString(" + inst + ") unknown type: " + inst.getClass().getName());

		final String		n=t.toString();
		final int			nLen=(null == n) ? 0 : n.length(), numArgs=t.getNumArguments();
		final StringBuilder	sb=new StringBuilder(Math.max(nLen,0) + 4 + Math.max(numArgs, 0) * 8);
		if (nLen > 0)
			sb.append(n);

		sb.append('(');
		try
		{
			t.appendArguments(sb, inst);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
		sb.append(')');

		return sb.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (RegularTimePeriod inst) throws Exception
	{
		return toString(inst);
	}

	// Format is type(v1,v2,...) where values order is year,month,day,hour,minute,second,msec
	public static final RegularTimePeriod fromString (final String s)
	{
		final int	sLen=(null == s) ? 0 : s.length();
		if (sLen <= 0)
			return null;

		final int	nPos=s.indexOf('(');
		if (nPos <= 0)
			throw new IllegalArgumentException("fromString(" + s + ") missing period name");

		final String				n=s.substring(0, nPos);
		final RegularTimePeriodType	t=RegularTimePeriodType.fromString(n);
		if (null == t)
			throw new IllegalArgumentException("fromString(" + s + ") unknown period name: " + n);

		if (nPos >= (sLen-1))
			throw new IllegalArgumentException("fromString(" + s + ") missing period arguments");

		final int	ePos=s.indexOf(')', nPos + 1);
		if (ePos <= nPos)
			throw new IllegalArgumentException("fromString(" + s + ") no period arguments");

		final String		vs=s.substring(nPos+1, ePos);
		final List<String>	vl=StringUtil.splitString(vs, ',');
		return t.fromStringArgs(vl);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public RegularTimePeriod newInstance (String s) throws Exception
	{
		return fromString(StringUtil.getCleanStringValue(s));
	}

	public static final RegularTimePeriodValueStringInstantiator	DEFAULT=
		new RegularTimePeriodValueStringInstantiator();
}
