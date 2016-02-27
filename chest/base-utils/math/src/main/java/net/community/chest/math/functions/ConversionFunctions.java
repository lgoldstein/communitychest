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
 * @since Jun 7, 2009 3:06:06 PM
 */
public enum ConversionFunctions implements NumbersFunction, SingleArgumentCalculator {
	DEGS {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.toDegrees(v);
			}
		},
	RADS {
			/*
			 * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
			 */
			@Override
			public double execute (double v)
			{
				return Math.toRadians(v);
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

	public static final List<ConversionFunctions>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final ConversionFunctions fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final ConversionFunctions fromSymbol (final String sym)
	{
		return MathFunctions.fromSymbol(VALUES, sym, false);
	}
}
