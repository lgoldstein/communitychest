/*
 * 
 */
package net.community.chest.math.compare;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import net.community.chest.math.NumberType;
import net.community.chest.math.NumbersFunction;
import net.community.chest.math.functions.MathFunctions;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 13, 2009 2:04:51 PM
 */
public enum RangeOperator implements RangeComparator, NumbersFunction {
	INCLUDES("[]") {
			/*
			 * @see net.community.chest.math.compare.RangeOperator#isOkStartValueResult(int)
			 */
			@Override
			public boolean isOkStartValueResult (final int cRes)
			{
				return (cRes >= 0);
			}
			/*
			 * @see net.community.chest.math.compare.RangeOperator#isOkEndValueResult(int)
			 */
			@Override
			public boolean isOkEndValueResult (final int cRes)
			{
				return (cRes <= 0);
			}
		},
	EXCLUDES("><") {
			/*
			 * @see net.community.chest.math.compare.RangeOperator#isOkStartValueResult(int)
			 */
			@Override
			public boolean isOkStartValueResult (final int cRes)
			{
				return (cRes < 0);
			}
			/*
			 * @see net.community.chest.math.compare.RangeOperator#isOkEndValueResult(int)
			 */
			@Override
			public boolean isOkEndValueResult (final int cRes)
			{
				return (cRes > 0);
			}
		};
	/**
	 * @param cRes The result of comparing the value with the start value (if any)
	 * @return <code>null</code> if result matches the range operator
	 */
	public abstract boolean isOkStartValueResult (final int cRes);
	/**
	 * @param cRes The result of comparing the value with the end value (if any)
	 * @return <code>null</code> if result matches the range operator
	 */
	public abstract boolean isOkEndValueResult (final int cRes);
	/*
	 * @see net.community.chest.RangeComparator#invoke(java.util.Comparator, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public <V> Boolean invoke (Comparator<? super V> c, V value, V startValue, V endValue)
	{
		if ((null == c) || (null == value))
			return null;

		if ((startValue != null) && (!isOkStartValueResult(c.compare(value, startValue))))
			return Boolean.FALSE;
		if ((endValue != null) && (!isOkEndValueResult(c.compare(value, endValue))))
			return Boolean.FALSE;

		return Boolean.TRUE;
	}
	/*
	 * @see net.community.chest.RangeComparator#invoke(java.lang.Comparable, java.lang.Comparable, java.lang.Comparable)
	 */
	@Override
	public <V extends Comparable<V>> Boolean invoke (V value, V startValue, V endValue)
	{
		if (null == value)
			return null;

		if ((startValue != null) && (!isOkStartValueResult(value.compareTo(startValue))))
			return Boolean.FALSE;
		if ((endValue != null) && (!isOkEndValueResult(value.compareTo(endValue))))
			return Boolean.FALSE;

		return Boolean.TRUE;
	}
	/*
	 * @see net.community.chest.math.NumbersFunction#invoke(java.util.List)
	 */
	@Override
	public Number invoke (final List<? extends Number> args) throws IllegalArgumentException, ClassCastException
	{
		final int	numArgs=(null == args) ? 0 : args.size(),
					maxArgs=getNumArguments();
		if (numArgs < maxArgs)
			return null;

		final List<? extends Number>	vals=
			NumberType.convertToBestPrecision(NumberType.MAX_PRECISION_TYPE, args.get(0), args.get(1), args.get(2));
		final int						numVals=
			(null == vals) ? 0 : vals.size();
		if (numVals < maxArgs)
			return null;

		final Number		value=vals.get(0), startValue=vals.get(1), endValue=vals.get(2);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Boolean		res=invoke((Comparable) value, (Comparable) startValue, (Comparable) endValue);
		if (null == res)
			return null;

		return res.booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0);
	}
	/*
	 * @see net.community.chest.math.NumbersFunction#invoke(java.lang.Number[])
	 */
	@Override
	public Number invoke (Number... args) throws IllegalArgumentException, ClassCastException
	{
		if ((null == args) || (args.length < getNumArguments()))
			return null;

		return invoke(Arrays.asList(args));
	}
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

	private final String	_symbol;
	/*
	 * @see net.community.chest.math.FunctionInterface#getSymbol()
	 */
	@Override
	public final String getSymbol ()
	{
		return _symbol;
	}
	/*
	 * @see net.community.chest.math.NumbersFunction#getFloatingPointExecutionState()
	 */
	@Override
	public final Boolean getFloatingPointExecutionState ()
	{
		return null;
	}

	RangeOperator (String sym)
	{
		_symbol = sym;
	}

	public static final List<RangeOperator>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final RangeOperator fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	
	public static final RangeOperator fromSymbol (final String sym)
	{
		return MathFunctions.fromSymbol(VALUES, sym, false);
	}

	public static final RangeOperator inverse (final RangeOperator op)
	{
		if (null == op)
			return null;

		switch(op)
		{
			case EXCLUDES	: return INCLUDES;
			case INCLUDES	: return EXCLUDES;
			default			:	// should not happen
				throw new NoSuchElementException("inverse(" + op + ") unknown operator");
		}
	}
	// negates whatever Boolean invocation result is received from the real comparator
	public static final RangeComparator negate (final RangeComparator c)
	{
		// some "shortcuts"
		if (c instanceof RangeOperator)
			return inverse((RangeOperator) c);

		return ComparatorNegator.negate(RangeComparator.class, c);
	}
}
