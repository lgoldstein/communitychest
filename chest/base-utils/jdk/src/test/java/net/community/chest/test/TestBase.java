package net.community.chest.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.community.chest.io.ApplicationIOUtils;
import net.community.chest.lang.BooleanResult;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.datetime.Duration;
import net.community.chest.util.datetime.TimeUnits;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 27, 2007 2:47:04 PM
 */
public class TestBase extends ApplicationIOUtils {
	/**
	 * Fills in missing arguments (if any)
	 * @param out output for prompt - if null then prompt not displayed
	 * @param in input reader from which to read the user's responses
	 * @param args original arguments list
	 * @param prompts prompts for each expected (mandatory) argument - may NOT be null
	 * @return filled arguments array - may be the original if all arguments present.
	 * Returns <code>null</code> if QUIT requested
	 */
	public static final String[] resolveTestParameters (
			final PrintStream out, final BufferedReader in, final String[] args, final String ... prompts)
	{
		if ((args != null) && (args.length >= prompts.length))
			return args;

		final String[]	tstArgs=new String[prompts.length];
		int				tstIndex=(null == args) ? 0 : Math.min(args.length, tstArgs.length);
		for (int index=0; index < tstIndex; index++)
			tstArgs[index] = args[index];
		
		while (tstIndex < tstArgs.length)
		{
			final String	val=getNonEmptyValue(out, in, prompts[tstIndex]);
			if (isQuit(val))
				return null;

			tstArgs[tstIndex] = val;
			tstIndex++;
		}
		
		return tstArgs;
	}

	/*----------------------------------------------------------------------*/

	public static final <E extends Enum<E>> void checkEnumCompatibility (
			final PrintStream out, Class<E> eClass, E d1, E d2)
	{
		out.println("checkEnumCompatibility(" + d1 + "/" + d2 + "):");
		final Class<?>	c1=d1.getClass(), c2=d2.getClass();
		if (c1 == c2)
			out.println("\tsame class: " + c1.getSimpleName());
		if (c1.isAssignableFrom(c2))
			out.println("\t" + d1 + "[" + c1.getName() + "] <= " + d2 + "[" + c2.getName() + "]");
		if (c2.isAssignableFrom(c1))
			out.println("\t" + d1 + "[" + c1.getName() + "] => " + d2 + "[" + c2.getName() + "]");
		if (eClass.isAssignableFrom(c1))
			out.println("\t" + eClass.getName() + " <= " + d1 + "[" + c1.getName() + "]");
		if (eClass.isAssignableFrom(c2))
			out.println("\t" + eClass.getName() + " <= " + d2 + "[" + c2.getName() + "]");
	}

	private static String	_unitsChoices	/* =null */;
	public static final synchronized String getTimeUnitsChoices ()
	{
		if (null == _unitsChoices)
		{
			final List<TimeUnits>	vals=TimeUnits.getValues();
			final StringBuilder		sb=new StringBuilder(vals.size() * 8);
			for (final TimeUnits u : vals)
			{
				final String	uName=u.name();
				if (sb.length() > 0)
					sb.append('/');
				sb.append('(').append(u.getFormatChar()).append(')');
				sb.append(uName.substring(1));
			}

			_unitsChoices = sb.toString();

		}

		return _unitsChoices;
	}

	/*----------------------------------------------------------------------*/

	// returns negative value if Quit - otherwise, msec. value interval
	public static final long inputTimeInterval (final PrintStream out, final BufferedReader in, final String orgPrompt)
	{
		final String	prompt=((null == orgPrompt) || (orgPrompt.length() <= 0))
			? "interval (using " + getTimeUnitsChoices() + ")/(Q)uit"
			: orgPrompt
			;
			
		for ( ; ; )
		{
			final String	iVal=getval(out, in, prompt);
			if ((null == iVal) || (iVal.length() <= 0))
				continue;
			if (isQuit(iVal))
				return (-1L);

			try
			{
				final long	intervalMsec=Duration.fromTimespec(iVal);
				if (intervalMsec < 0L)
					continue;

				return intervalMsec;
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}
	}

	/*----------------------------------------------------------------------*/

	public static final <E extends Enum<E>> String getEnumValuesChoices (E defValue, List<? extends E> vals)
	{
		if ((null == vals) || (vals.size() <= 0))
			return null;

		final StringBuilder	sb=new StringBuilder(vals.size() * 16);
		for (final E v : vals)
		{
			final String	vName=(null == v) ? null : v.toString();
			if ((null == vName) || (vName.length() <= 0))
				continue;		// should not happen

			if (sb.length() > 0)
				sb.append('/');

			final boolean	isDefValue=v.equals(defValue);
			sb.append(isDefValue ? '[' : '(')
			  .append(vName.substring(0, 1))
			  .append(isDefValue ? ']' : ')')
			  .append(vName.substring(1))
			  ;
		}

		return (sb.length() <= 0) ? null : sb.toString();
	}	

	/*----------------------------------------------------------------------*/

	public static final <E extends Enum<E>> E inputEnumValue (
			final PrintStream out, final BufferedReader in, final String prompt,
			final boolean okIfNone, final E defValue, final List<? extends E> vals)
	{
		for ( ; ; )
		{
			final String	pName=getval(out, in, prompt);
			if ((null == pName) || (pName.length() <= 0))
			{
				if (okIfNone)
					return defValue;
				continue;
			}

			if (isQuit(pName))
				return null;

			final char	vChar=Character.toUpperCase(pName.charAt(0));
			for (final E v : vals)
			{
				final String	vString=(null == v) ? null : v.toString();
				if ((null == vString) || (vString.length() <= 0))
					continue;		// should not happen

				final char	eChar=Character.toUpperCase(vString.charAt(0));
				if (eChar == vChar)
					return v;
			}
		}
	}

	private static String _bvChoice;
	public static final synchronized String getBooleanValueChoices ()
	{
		if (null == _bvChoice)
			_bvChoice = getEnumValuesChoices(null, BooleanResult.VALUES);

		return _bvChoice;
	}

	/*----------------------------------------------------------------------*/

	// returns null if Quit
	public static final BooleanResult inputBooleanValue (
			final PrintStream out, final BufferedReader in, final String orgPrompt, final BooleanResult defVal /* null=no default */)
	{
		final String	prompt=((null == orgPrompt) || (orgPrompt.length() <= 0))
			? getBooleanValueChoices()
				+ ((defVal != null) ? "[ENTER=" + defVal + "]": "") + "/(Q)uit"
			: orgPrompt
			;
		for ( ; ; )
		{
			final String	ans=getval(out, in, prompt);
			if ((null == ans) || (ans.length() <= 0))
			{
				if (defVal != null)
					return defVal;
				continue;
			}
			if (isQuit(ans))
				return null;

			final char	ch=Character.toLowerCase(ans.charAt(0));
			if ((ch == 't') || (ch == 'y'))
				return BooleanResult.TRUE;
			else if ((ch == 'f') || (ch == 'n'))
				return BooleanResult.FALSE;
		}
	}

	/*----------------------------------------------------------------------*/

	public static final int showByReflection (final PrintStream out, final BufferedReader in, 
			final Object o, final boolean expandSubClasses, final String indent) throws Exception
	{
		final Class<?>	c=(null == o) ? null : o.getClass();
		if (null == c)
			return 0;

		final Map<String,? extends AttributeAccessor>								aMap=
			AttributeMethodType.getAllAccessibleAttributes(c);
		final Collection<? extends Map.Entry<String,? extends AttributeAccessor>>	aSet=
			((null == aMap) || (aMap.size() <= 0)) ? null : aMap.entrySet();
		if ((null == aSet) || (aSet.size() <= 0))
			return 0;

		for (final Map.Entry<String,? extends AttributeAccessor> ae : aSet)
		{
			final String			aName=(null == ae) ? null : ae.getKey();
			final AttributeAccessor	aa=(null == ae) ? null : ae.getValue();
			final Method			gm=(null == aa) ? null : aa.getGetter();
			if ((null == gm) || "class".equalsIgnoreCase(aName))
				continue;

			final Object	av;
			try
			{
				av = gm.invoke(o, AttributeAccessor.EMPTY_OBJECTS_ARRAY);
			}
			catch(Exception e)
			{
				System.err.println(indent + aName + ": " + e.getClass().getName() + ": " + e.getMessage());
				continue;
			}

			final Class<?>	at=(null == av) ? null : av.getClass();
			if (null == at)
				continue;

			if (ClassUtil.isAtomicClass(at))
			{
				out.println(indent + aName + ": " + av);
				continue;
			}

			out.println(indent + aName + ": " + at.getName());
			if (expandSubClasses)
			{
				final String	ans=getval(out, in, "expand " + aName + " value [y]/n/q");
				if (isQuit(ans)) return Integer.MIN_VALUE;

				if ((null == ans) || (ans.length() <= 0) || ('y' == Character.toLowerCase(ans.charAt(0))))
				{
					final int	nErr=
						showByReflection(out, in, av, expandSubClasses, (null == indent) ? "\t" : indent + "\t");
					if (nErr < 0)
						return nErr;
				}
			}
		}

		return 0;
	}

	/*----------------------------------------------------------------------*/

	public static final Integer toInteger (final Number n)
	{
		if (null == n)
			return Integer.valueOf(0);
		if (n instanceof Integer)
			return (Integer) n;
		else
			return Integer.valueOf(n.intValue());
	}

	public static final Long toLong (final Number n)
	{
		if (null == n)
			return Long.valueOf(0L);
		if (n instanceof Long)
			return (Long) n;
		else
			return Long.valueOf(n.longValue());
	}

	/*----------------------------------------------------------------------*/
	// removes any non alpha/digit characters
	public static final String getCleanOptionName (final String s)
	{
		final int		sLen=(null == s) ? 0 : s.length();
		StringBuilder	sb=null;
		int				lastPos=0;
		for (int curPos=0; curPos < sLen; curPos++)
		{
			final char	c=s.charAt(curPos);
			if (((c >= 'A') && (c <= 'Z'))
			 || ((c >= 'a') && (c <= 'z'))
			 || ((c >= '0') && (c <= '9')))
				continue;

			final int	cpyLen=curPos - lastPos;
			if (cpyLen > 0)
			{
				final String	ss=s.substring(lastPos, curPos);
				if (null == sb)
					sb = new StringBuilder(sLen);
				sb.append(ss);
			}

			lastPos = curPos + 1;
		}

		if (null == sb)
			return s;

		final int	cpyLen=sLen - lastPos;
		if (cpyLen > 0)
		{
			final String	ss=s.substring(lastPos);
			sb.append(ss);
		}

		return sb.toString();
	}

	/*----------------------------------------------------------------------*/
	// extracts any (...) delimited value - returns same as input if none found
	public static final String getOptionKeyValue (final String s)
	{
		final int	sLen=(null == s) ? 0 : s.length(),
					sPos=(sLen >= 3) ? s.indexOf('(') : (-1),
					ePos=((sPos >= 0) && (sPos < (sLen-1))) ? s.indexOf(')', sPos + 1) : (-1);
		if ((sPos >= 0) && (ePos > sPos))
			return s.substring(sPos+1, ePos);
		return s;
	}

	/*----------------------------------------------------------------------*/

	public static final String[] cleanArgumentsList (final int numToClean, final String ... args)
	{
		if (numToClean <= 0)
			return args;
		if ((null == args) || (args.length <= numToClean))
			return null;

		final List<String>	l=new ArrayList<String>(args.length - numToClean);
		for (int	aIndex=numToClean; aIndex < args.length; aIndex++)
		{
			final String	a=args[aIndex];
			if ((null == a) || (a.length() <= 0))
				continue;

			l.add(a);
		}

		final int	lSize=l.size();
		if (lSize <= 0)
			return null;

		return l.toArray(new String[lSize]);
	}
}
