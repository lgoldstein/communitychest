/*
 * 
 */
package net.community.chest.math.strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.StringUtil;
import net.community.chest.math.compare.ComparatorNegator;
import net.community.chest.math.functions.MathFunctions;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 2:01:49 PM
 */
public enum StringComparison implements StringComparisonExecutor {
	STARTS {
			/*
			 * @see net.community.chest.math.strings.StringComparisonExecutor#invoke(java.lang.String, java.lang.String, boolean)
			 */
			@Override
			public Boolean invoke (String s1, String s2, boolean caseSensitive)
			{
				return Boolean.valueOf(StringUtil.startsWith(s1, s2, false, caseSensitive));
			}
		},
	ENDS {
			/*
			 * @see net.community.chest.math.strings.StringComparisonExecutor#invoke(java.lang.String, java.lang.String, boolean)
			 */
			@Override
			public Boolean invoke (String s1, String s2, boolean caseSensitive)
			{
				return Boolean.valueOf(StringUtil.endsWith(s1, s2, false, caseSensitive));
			}
		},
	CONTAINS {
			/*
			 * @see net.community.chest.math.strings.StringComparisonExecutor#invoke(java.lang.String, java.lang.String, boolean)
			 */
			@Override
			public Boolean invoke (String s1, String s2, boolean caseSensitive)
			{
				return Boolean.valueOf(StringUtil.contains(s1, s2, false, caseSensitive));
			}
		},
	MATCHES {
			/*
			 * @see net.community.chest.math.strings.StringComparisonExecutor#invoke(java.lang.String, java.lang.String, boolean)
			 */
			@Override
			public Boolean invoke (String s1, String s2, boolean caseSensitive)
			{
				return Boolean.valueOf(StringUtil.compareDataStrings(s1, s2, caseSensitive) == 0);
			}
		},
	DIFFS {
			/*
			 * @see net.community.chest.math.strings.StringComparisonExecutor#invoke(java.lang.String, java.lang.String, boolean)
			 */
			@Override
			public Boolean invoke (String s1, String s2, boolean caseSensitive)
			{
				return Boolean.valueOf(StringUtil.compareDataStrings(s1, s2, caseSensitive) != 0);
			}
		};
	/*
	 * @see net.community.chest.math.FunctionInterface#getName()
	 */
	@Override
	public final String getName ()
	{
		return name();
	}
	/*
	 * @see net.community.chest.math.FunctionInterface#getNumArguments()
	 */
	@Override
	public final int getNumArguments ()
	{
		return 3;
	}
	/*
	 * @see net.community.chest.math.FunctionInterface#getSymbol()
	 */
	@Override
	public final String getSymbol ()
	{
		return getName();
	}

	public static final List<StringComparison>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final StringComparison fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final StringComparison fromSymbol (final String sym)
	{
		return MathFunctions.fromSymbol(VALUES, sym, false);
	}
	// negates whatever Boolean invocation result is received from the real comparator
	public static final StringComparisonExecutor negate (final StringComparisonExecutor c)
	{
		// some "shortcuts"
		if (MATCHES.equals(c))
			return DIFFS;
		else if (DIFFS.equals(c))
			return MATCHES;

		return ComparatorNegator.negate(StringComparisonExecutor.class, c);
	}
}
