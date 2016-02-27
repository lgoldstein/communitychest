/*
 * 
 */
package net.community.chest.io.filter;

import java.util.Collection;
import java.util.TreeSet;

import net.community.chest.util.compare.InstancesComparator;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @param <V> Type of value being filtered 
 * @author Lyor G.
 * @since Jan 28, 2010 5:33:16 PM
 */
public class ObjectFilterChain<V> extends TreeSet<ObjectFilter<V>> implements ObjectFilter<V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8864680149616236194L;
	private boolean	_conjFilter;
	public boolean isConjunctiveFilter ()
	{
		return _conjFilter;
	}

	public void setConjunctiveFilter (boolean f)
	{
		_conjFilter = f;
	}

	private final Class<V>	_valsClass;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final Class<V> getValuesClass ()
	{
		return _valsClass;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ObjectFilterChain (Class<V> valsClass, boolean conjunctive)
	{
		super(new InstancesComparator(ObjectFilter.class));
		if (null == (_valsClass=valsClass))
			throw new IllegalArgumentException("No values class specified");
		_conjFilter = conjunctive;
	}
	
	public static final <T> boolean accept (
			final T value, final boolean conjFilter, final Collection<? extends ObjectFilter<T>> fs)
	{
		if (null == value)
			return false;

		if ((null == fs) || (fs.size() <= 0))	// OK if no filters set
			return true;

		for (final ObjectFilter<T> ff : fs)
		{
			if (null == ff)
				continue;

			final boolean	fa=ff.accept(value);
			if (conjFilter)
			{
				if (!fa)	// for AND 1st failure is enough
					return false;
			}
			else
			{
				if (fa)	// for OR 1st success is enough
					return true;
			}
		}

		return conjFilter;
	}
	@SafeVarargs
    public static final <T> boolean accept (
			final T value, final boolean conjFilter, final ObjectFilter<T> ... fs)
	{
		return accept(value, conjFilter, ((null == value) || (null == fs) || (fs.length <= 0)) ? null : SetsUtils.uniqueSetOf(fs));
	}
	/*
	 * @see net.community.chest.io.filter.ObjectFilter#accept(java.lang.Object)
	 */
	@Override
	public boolean accept (V value)
	{
		return accept(value, isConjunctiveFilter(), this);
	}
}
