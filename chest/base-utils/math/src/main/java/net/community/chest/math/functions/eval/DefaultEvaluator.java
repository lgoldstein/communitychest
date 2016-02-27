/*
 * 
 */
package net.community.chest.math.functions.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.community.chest.math.NumbersFunction;
import net.community.chest.resources.PropertyAccessor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 3:26:14 PM
 */
public class DefaultEvaluator implements Evaluator {
	private String	_name;
	/*
	 * @see net.community.chest.math.FunctionInterface#getName()
	 */
	@Override
	public String getName ()
	{
		return _name;
	}

	public void setName (String name)
	{
		_name = name;
	}

	private String	_symbol;
	/*
	 * @see net.community.chest.math.FunctionInterface#getSymbol()
	 */
	@Override
	public String getSymbol ()
	{
		return _symbol;
	}

	public void setSymbol (String symbol)
	{
		_symbol = symbol;
	}

	private Boolean	_fp;
	/*
	 * @see net.community.chest.math.NumbersFunction#getFloatingPointExecutionState()
	 */
	@Override
	public Boolean getFloatingPointExecutionState ()
	{
		return _fp;
	}

	public void setFloatingPointExecutionState (Boolean fp)
	{
		_fp = fp;
	}
	/*
	 * @see net.community.chest.math.FunctionInterface#getNumArguments()
	 */
	@Override
	public int getNumArguments ()
	{
		return 0;
	}

	private NumbersFunction	_func;
	public NumbersFunction getNumbersFunction ()
	{
		return _func;
	}

	public void setNumbersFunction (NumbersFunction f)
	{
		_func = f;
	}

	private Collection<Evaluator>	_evArgs;
	public Collection<Evaluator> getArguments ()
	{
		return _evArgs;
	}

	public void setArguments (Collection<Evaluator> evArgs)
	{
		_evArgs = evArgs;
	}

	public Collection<Evaluator> addArgument (Evaluator e)
	{
		Collection<Evaluator>	eva=getArguments();
		if (null == e)
			return eva;

		if (null == eva)
		{
			final NumbersFunction	f=getNumbersFunction();
			final int				n=(null == f) ? 0 : f.getNumArguments();
			setArguments(new ArrayList<Evaluator>(Math.max(n, 10)));
			if (null == (eva=getArguments()))
				throw new IllegalStateException("No evaluators collection available though created");
		}

		eva.add(e);
		return eva;
	}

	public Collection<Evaluator> addArgument (Collection<? extends Evaluator> el)
	{
		Collection<Evaluator>	ret=getArguments();
		if ((el != null) && (el.size() > 0))
		{
			for (final Evaluator e : el)
				ret = addArgument(e);
		}

		return ret;
	}

	public Collection<Evaluator> addArgument (Evaluator ... el)
	{
		return addArgument(((null == el) || (el.length <= 0)) ? null : Arrays.asList(el));
	}
	/*
	 * @see net.community.chest.math.functions.eval.Evaluator#invoke(net.community.chest.resources.PropertyAccessor)
	 */
	@Override
	public Number invoke (PropertyAccessor<String,? extends Number> resolver)
			throws RuntimeException
	{
		final NumbersFunction	f=getNumbersFunction();
		if (null == f)
			return null;

		final int								numArgs=f.getNumArguments();
		final List<Number>						nl=
			(numArgs <= 0) ? null : new ArrayList<Number>(numArgs);
		final Collection<? extends Evaluator>	el=
			(numArgs <= 0) ? null : getArguments();
		final Iterator<? extends Evaluator>		ei=
			((null == el) || (el.size() <= 0)) ? null : el.iterator();
		for (int	aIndex=0; aIndex < numArgs; aIndex++)
		{
			final Evaluator	e=ei.hasNext() ? ei.next() : null;
			final Number	n=(null == e) ? null : e.invoke(resolver);
			if (null == n)
				return null;
			nl.add(n);
		}

		return f.invoke(nl);
	}
	/*
	 * @see net.community.chest.math.NumbersFunction#invoke(java.util.List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Number invoke (List<? extends Number> args)
			throws IllegalArgumentException, ClassCastException
	{
		if (args instanceof PropertyAccessor)
			return invoke((PropertyAccessor<String,? extends Number>) args);
		else
			return invoke((PropertyAccessor<String,? extends Number>) new PositionalArgumentPropertyAccessor(args));
	}
	/*
	 * @see net.community.chest.math.NumbersFunction#invoke(java.lang.Number[])
	 */
	@Override
	public Number invoke (Number... args) throws IllegalArgumentException, ClassCastException
	{
		return invoke(((null == args) || (args.length <= 0)) ? null : Arrays.asList(args));
	}
}
