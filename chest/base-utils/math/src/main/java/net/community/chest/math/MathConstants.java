/*
 * 
 */
package net.community.chest.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.math.functions.MathFunctions;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 11:42:29 AM
 */
public enum MathConstants implements NumbersFunction {
	PI(Double.valueOf(Math.PI)),
	E(Double.valueOf(Math.E));
	/*
	 * @see net.community.chest.math.NumbersFunction#getFloatingPointExecutionState()
	 */
	@Override
	public final Boolean getFloatingPointExecutionState ()
	{
		return Boolean.TRUE;
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
		return 0;
	}
	/*
	 * @see net.community.chest.math.FunctionInterface#getSymbol()
	 */
	@Override
	public final String getSymbol ()
	{
		return getName();
	}

	private final Number	_val;
	public final Number getValue ()
	{
		return _val;
	}
	/*
	 * @see net.community.chest.math.NumbersFunction#invoke(java.util.List)
	 */
	@Override
	public Number invoke (List<? extends Number> args)
			throws IllegalArgumentException, ClassCastException
	{
		return getValue();
	}
	/*
	 * @see net.community.chest.math.NumbersFunction#invoke(java.lang.Number[])
	 */
	@Override
	public Number invoke (Number... args) throws IllegalArgumentException, ClassCastException
	{
		return getValue();
	}

	MathConstants (Number val)
	{
		_val = val;
	}

	public static final List<MathConstants>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final MathConstants fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final MathConstants fromSymbol (final String sym)
	{
		return MathFunctions.fromSymbol(VALUES, sym, false);
	}

	public static final NumbersFunction fromConstant (final Number n)
	{
		return (null == n) ? null : new MathConstantEmbedder(n);
	}
}
