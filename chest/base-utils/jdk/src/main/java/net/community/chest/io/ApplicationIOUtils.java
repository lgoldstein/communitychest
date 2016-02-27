/*
 * 
 */
package net.community.chest.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Some useful standard I/O application utilities</P>
 * 
 * @author Lyor G.
 * @since Jun 25, 2009 1:18:36 PM
 */
public class ApplicationIOUtils {
	protected ApplicationIOUtils ()
	{
		super();
	}

	public static final boolean isQuit (final String s)
	{
		return "q".equalsIgnoreCase(s) || "quit".equalsIgnoreCase(s);
	}

	public static final String getval (final PrintStream out, final BufferedReader in, final String prompt)
	{
		if (out != null)
			out.print("Enter " + prompt + ": ");
		try
		{
			return in.readLine();
		}
		catch(IOException e)
		{
			return e.getClass().getName() + ": " + e.getMessage();
		}
	}
	/**
	 * Repeats prompting the user until a non-empty value is entered
	 * @param out output for prompt - if null then prompt not displayed
	 * @param in input reader from which to read the user's response
	 * @param prompt prompt to display before waiting for response
	 * @return read value
	 */
	public static final String getNonEmptyValue (PrintStream out, BufferedReader in, String prompt)
	{
		while (true)
		{
			final String	val=getval(out, in, prompt);
			if ((null == val) || (val.length() <= 0))
				continue;
			else
				return val;
		}
	}

	private static BufferedReader	_stdin	/* =null */;
	public static final synchronized BufferedReader getStdin ()
	{
		if (null == _stdin)
			_stdin = new BufferedReader(new InputStreamReader(System.in));
		return _stdin;
	}

	/*----------------------------------------------------------------------*/

	// returns null for Quit
	public static final Calendar inputDateTimeValue (final PrintStream out, final BufferedReader in, final String prompt, final Calendar defValue)
	{
		for ( ; ; )
		{
			final String	dtvType=getval(out, in, prompt + " type (D)ate/[T]imestamp/(Q)uit=empty value");
			if (isQuit(dtvType))
				return null;

			final char		typeChar=((null == dtvType) || (dtvType.length() <= 0)) ? '\0' : Character.toUpperCase(dtvType.charAt(0));
			final String	typeFmt;
			if (('\0' == typeChar) || ('T' == typeChar))
				typeFmt = "dd/MM/yyyy HH:mm:ss";
			else if ('D' == typeChar)
				typeFmt = "dd/MM/yyyy";
			else
				typeFmt = null;
			if ((null == typeFmt) || (typeFmt.length() <= 0))
				continue;

			final DateFormat	dtf=new SimpleDateFormat(typeFmt);
			final Calendar		defReturn=(null == defValue) ? Calendar.getInstance() : defValue;
			final String		defString=dtf.format(defReturn.getTime()),
								dtvPrompt=prompt + " value (" + typeFmt + ")[ENTER=" + defString + "] or (Q)";

			for ( ; ; )
			{
				final String	dtvValue=getval(out, in, dtvPrompt);
				if (isQuit(dtvValue))
					return null;

				if ((null == dtvValue) || (dtvValue.length() <= 0))
					return defReturn;

				switch(typeChar)
				{
					case 'D'	:
						try
						{
							return DateUtil.parseStringToDate(dtvValue, true);
						}
						catch(Exception e)
						{
							System.err.println("Bad format: " + e.getMessage());
						}
						break;
						
					case '\0'	:
					case 'T'	:
						try
						{
							return DateUtil.parseStringToDatetime(dtvValue, true);
						}
						catch(Exception e)
						{
							System.err.println("Bad format: " + e.getMessage());
						}
						break;

					default		:
				}
			}
		}
	}

	/*----------------------------------------------------------------------*/

	public static final <V> V inputListChoice (
			final PrintStream out, final BufferedReader in, final String title,
			final List<? extends V> valsList, final V defValue /* null=none */)
	{
		final int	numVals=(null == valsList) ? 0 : valsList.size();
		if (numVals <= 0)
			return defValue;

		final String	prompt="selected index "
			+ ((defValue != null) ? "[ENTER=" + defValue + "]" : "")
			+ "/(Q)uit"
			;
		for ( ; ; )
		{
			out.println(title);
			for (int	vIndex=0; vIndex < numVals; vIndex++)
				out.println("\t" + vIndex + ": " + valsList.get(vIndex));

			final String	ans=getval(out, in, prompt);
			if ((null == ans) || (ans.length() <= 0))
			{
				if (defValue != null)
					return defValue;
				continue;
			}

			if (isQuit(ans)) return null;

			try
			{
				final int	vIndex=Integer.parseInt(ans);
				if ((vIndex >= 0) && (vIndex < numVals))
					return valsList.get(vIndex);
			}
			catch(NumberFormatException e)
			{
				// ignored
			}
		}
	}

	/*----------------------------------------------------------------------*/

	public static final <V> List<? extends V> inputMultiListChoice (
			final PrintStream out, final BufferedReader in, final String title, final List<? extends V> valsList)
	{
		final int	numVals=(null == valsList) ? 0 : valsList.size();
		if (numVals <= 1)
			return valsList;

		for ( ; ; )
		{
			out.println(title);
			for (int	vIndex=0; vIndex < numVals; vIndex++)
				out.println("\t" + vIndex + ": " + valsList.get(vIndex));

			final String	ans=getval(out, in, "selected index(es) [*=ALL]/(Q)uit");
			if ((null == ans) || (ans.length() <= 0))
				continue;
			if ("*".equalsIgnoreCase(ans))
				return valsList;
			if (isQuit(ans))
				return null;

			final Collection<String>	il=StringUtil.splitString(ans, ',');
			final int					numIdx=(null == il) ? 0 : il.size();
			if (numIdx <= 0)
				continue;

			final List<V>				ret=new ArrayList<V>(numIdx);
			final Collection<Integer>	idxSet=new HashSet<Integer>(numIdx);
			try
			{
				for (final String s : il)
				{
					if ((null == s) || (s.length() <= 0))
						continue;

					final Integer	iv=Integer.valueOf(s);
					if (idxSet.contains(iv))
						continue;

					final int	idxVal=iv.intValue();
					if ((idxVal < 0) || (idxVal >= numVals))
						throw new NumberFormatException("bad index: " + s);

					final V	v=valsList.get(idxVal);
					if (null == v)
						continue;

					ret.add(v);
					idxSet.add(iv);
				}

				if (ret.size() > 0)
					return ret;
			}
			catch(NumberFormatException e)
			{
				System.err.println("Bad format: " + e.getMessage());
			}
		}
	}

	/*----------------------------------------------------------------------*/

	// returns NULL if Quit
	public static final Integer inputIntValue (
			final PrintStream out, final BufferedReader in, final String prompt,
			final int minVal /* inclusive */, final int maxVal /* inclusive */,
			final Integer defVal /* null means no default */)
	{
		final String	msg=prompt
			+ ((defVal != null) ? " [ENTER=" + defVal + "]" : "")
			+ " or Quit";
		for ( ; ; )
		{
			final String	ans=getval(out, in, msg);
			if ((null == ans) || (ans.length() <= 0))
			{
				if (defVal != null)
					return defVal;
				continue;
			}
			if (isQuit(ans)) return null;

			try
			{
				final int	v=Integer.parseInt(ans);
				if ((v >= minVal) && (v <= maxVal))
					return Integer.valueOf(v);
			}
			catch(NumberFormatException e)
			{
				continue;	// ignored
			}
		}
	}
	/**
	 * <P>Checks if a given string contains any of the given prefixes, and if so returns the
	 * <U>longest</U> match. E.g., for <code>--verbose</code> and prefixes=[ '-', '--' ]
	 * it will return '--'</P>
	 * @param s The {@link String} to check - if <code>null</code>/empty then no match is done
	 * @param strictPrefix Whether the prefixes should be strict - i.e., some characters must
	 * follow the prefix value
	 * @param caseSensitive If prefix is checked case sensitive or not
	 * @param prefixes The prefixes to check - if <code>null</code>/empty then no match is done.
	 * <B>Note:</B> <code>null</code>/empty prefix values are <U>ignored</U>
	 * @return The longest match or <code>null</code> if no match was found
	 * @see StringUtil#startsWith(String, String, boolean, boolean)
	 */
	public static final String getLongestPrefixMatch (
			final String s, final boolean strictPrefix, final boolean caseSensitive, final Collection<String> prefixes)
	{
		if ((s == null) || (s.length() <= 0))
			return null;
		if ((prefixes == null) || (prefixes.size() <= 0))
			return null;

		String			argPrefix=null;
		for (final String prfx : prefixes)
		{
			if ((prfx == null) || (prfx.length() <= 0))
				continue;

			if (!StringUtil.startsWith(s, prfx, strictPrefix, caseSensitive))
				continue;

			// if already have a prefix then prefer the longest match
			if (argPrefix != null)
			{
				final int	curLen=argPrefix.length(), newLen=prfx.length();
				if (curLen > newLen)
					continue;
			}

			argPrefix = prfx;
		}

		return argPrefix;
	}
	/**
	 * <P>Checks if a given string contains any of the given prefixes, and if so returns the
	 * <U>longest</U> match. E.g., for <code>--verbose</code> and prefixes=[ '-', '--' ]
	 * it will return '--'</P>
	 * @param s The {@link String} to check - if <code>null</code>/empty then no match is done
	 * @param strictPrefix Whether the prefixes should be strict - i.e., some characters must
	 * follow the prefix value
	 * @param caseSensitive If prefix is checked case sensitive or not
	 * @param prefixes The prefixes to check - if <code>null</code>/empty then no match is done.
	 * <B>Note:</B> <code>null</code>/empty prefix values are <U>ignored</U>
	 * @return The longest match or <code>null</code> if no match was found
	 */
	public static final String getLongestPrefixMatch (
			final String s, final boolean strictPrefix, final boolean caseSensitive, final String ... prefixes)
	{
		if ((prefixes == null) || (prefixes.length <= 0))
			return null;
		else
			return getLongestPrefixMatch(s, strictPrefix, caseSensitive, Arrays.asList(prefixes));
	}
	/**
	 * Separator used for a binary option value - e.g., '-debug=true' 
	 */
	public static final char	BINARY_OPTION_VALUE_SEP='=';
	/**
	 * <P>Parses command line values assumed to contain a sequence of options followed by
	 * the actual arguments. The options are assumed to be <U>prefixed</U> by one or more
	 * <U>strict</U> patterns (e.g., '-', '--', '/', etc.). The method goes over the original
	 * command line arguments and "consumes" the options until the <U>1st</U> non-option is
	 * encountered (i.e., a value not prefixed by one of the allowed prefixes). From there
	 * on the rest of the values (if any) are declared as "arguments". The options can be
	 * either unary (i.e., no arguments - e.g., '-v') or binary (i.e., one argument - e.g.,
	 * '--optimize on' or '--optimize=on'). If the <U>same options</U> is re-encountered
	 * then its value is added to the list</P>
	 * 
	 * <P>Example(s):</P></BR>
	 * <PRE>
	 * 		--user foo --password=bar -v localhost  - options/values are user/foo, password/bar, v/v
	 * 												and argument is 'localhost'
	 * 
	 *  	--file=x --file=y - options are file/[x,y] and no arguments
	 * </PRE>
	 * <P>
	 * @param prefixes Prefixes to be checked - if none specified then all the values are
	 * considered arguments (i.e., no options)
	 * @param caseSensitive <code>TRUE</code> if prefix checking/options mapping is case sensitive
	 * @param args {@link String} values {@link List}
	 * @return A "pair" represented as a {@link java.util.Map.Entry} whose 'key'=a {@link Map} of options,
	 * 'value'=a {@link List} of the arguments following the options. <B>Note:</B> either the
	 * value or each of the components may be <code>null</code>
	 */
	public static final Map.Entry<Map<String,List<String>>, List<String>> parseCommandLineArguments (
			final Collection<String> prefixes, final boolean caseSensitive, final List<String> args)
	{
		final int	numArgs=(args == null) ? 0 : args.size();
		if (numArgs <= 0)
			return null;

		// if no prefixes, then all of them are assumed to be arguments
		if ((prefixes == null) || (prefixes.size() <= 0))
			return new MapEntryImpl<Map<String,List<String>>, List<String>>(null, args);

		Map<String,List<String>>	optsMap=null;
		for (int	aIndex=0; aIndex < numArgs; aIndex++)
		{
			String			argVal=args.get(aIndex);
			final String	argPrefix=getLongestPrefixMatch(argVal, true, caseSensitive, prefixes);
			final int		prfxLen=(argPrefix == null) ? 0 : argPrefix.length();
			if (prfxLen  <= 0)	// reached end of options - the rest are arguments
			{
				final List<String>	argsList=(aIndex <= 0) ? args : args.subList(aIndex, numArgs);
				return new MapEntryImpl<Map<String,List<String>>, List<String>>(optsMap, argsList);
			}

			argVal = argVal.substring(prfxLen);	// strip the prefix

			if (optsMap == null)
				optsMap = caseSensitive ? new TreeMap<String,List<String>>() : new TreeMap<String,List<String>>(String.CASE_INSENSITIVE_ORDER);

			// check if this is a binary 'inline' option - e.g., --auth=y
			final int	avLen=argVal.length(), sepPos=argVal.indexOf(BINARY_OPTION_VALUE_SEP);
			String		optValue=null;
			if ((sepPos > 0) && (sepPos < (avLen-1)))
			{
				optValue = argVal.substring(sepPos + 1);
				argVal = argVal.substring(0, sepPos);
			}

			List<String>	optVals=optsMap.get(argVal);
			if (optVals == null)
			{
				optVals = new ArrayList<String>(5);
				optsMap.put(argVal, optVals);
			}

			// check if already have an inlined value
			if (optValue == null)
			{
				optValue = argVal;	// use as default in case a unary option

				// check if this is a no-args (unary) or one-arg (binary) option by checking if the NEXT argument has a prefix
				if (aIndex < (numArgs - 1))
				{
					optValue = args.get(aIndex+1);

					final String	nextPrefix=getLongestPrefixMatch(optValue, true, false, prefixes);
					if ((nextPrefix == null) || (nextPrefix.length() <= 0))
						aIndex++;	// skip the added option value
					else	// restore the default
						optValue = argVal;
				}
			}

			if (!optVals.add(optValue))
				continue;	// debug breakpoint
		}

		// this means that there were no arguments - only options
		return new MapEntryImpl<Map<String,List<String>>, List<String>>(optsMap, null);
	}

	public static final Map.Entry<Map<String,List<String>>, List<String>> parseCommandLineArguments (
			final Collection<String> prefixes, final boolean caseSensitive, final String ... args)
	{
		if ((args == null) || (args.length <= 0))
			return null;
		else
			return parseCommandLineArguments(prefixes, caseSensitive, Arrays.asList(args));
	}
}
