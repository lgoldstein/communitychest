/*
 * 
 */
package net.community.chest.math.functions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.math.NumbersFunction;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 10:21:14 AM
 */
public enum TrigonometryFunctions implements NumbersFunction, SingleArgumentCalculator {
	SIN {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.sin(v);
			}
		},
	ASIN {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.asin(v);
			}
		},
	SINH {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.sinh(v);
			}
		},
	COS {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.cos(v);
			}
		},
	ACOS {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.acos(v);
			}
		},
	COSH {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.cosh(v);
			}
		},
	TAN {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.tan(v);
			}
		},
	ATAN {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.atan(v);
			}
		},
	TANH {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.tanh(v);
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
	 * @see net.community.chest.math.FunctionInterface#getSymbol()
	 */
	@Override
	public final String getSymbol ()
	{
		return getName();
	}
	/*
	 * @see net.community.chest.math.FunctionInterface#getNumArguments()
	 */
	@Override
	public final int getNumArguments ()
	{
		return 1;
	}
	/*
	 * @see net.community.chest.math.NumbersFunction#getFloatingPointExecutionState()
	 */
	@Override
	public final Boolean getFloatingPointExecutionState ()
	{
		return Boolean.TRUE;
	}
	/*
	 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(long)
	 */
	@Override
	public long execute (long v)
	{
		return (long) execute((double) v);
	}
 	/*
	 * @see net.community.chest.math.NumbersFunction#invoke(java.lang.Number[])
	 */
	@Override
	public Number invoke (Number... args) throws IllegalArgumentException, ClassCastException
	{
		if ((null == args) || (args.length <= 0))
			return null;

		final Number	n=args[0];
		if (null == n)
			return null;

		return Double.valueOf(execute(n.doubleValue()));
	}
	/*
	 * @see net.community.chest.math.NumbersFunction#invoke(java.util.List)
	 */
	@Override
	public Number invoke (List<? extends Number> args)
			throws IllegalArgumentException, ClassCastException
	{
		if ((null == args) || (args.size() <= 0))
			return null;
		return invoke(args.get(0));
	}

	public static final List<TrigonometryFunctions>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final TrigonometryFunctions fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final TrigonometryFunctions fromSymbol (final String sym)
	{
		return MathFunctions.fromSymbol(VALUES, sym, false);
	}
	// NOTE !!! not all functions have an inverse
	public static final TrigonometryFunctions inverse (final TrigonometryFunctions op)
	{
		if (null == op)
			return null;

		switch(op)
		{
			case SIN	: return ASIN;
			case ASIN	: return SIN;
			case COS	: return ACOS;
			case ACOS	: return COS;
			case TAN	: return ATAN;
			case ATAN	: return TAN;
			default		:	// OK if no inverse
				return null;
		}
	}
}
