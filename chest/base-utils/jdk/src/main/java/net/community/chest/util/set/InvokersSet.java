/*
 * 
 */
package net.community.chest.util.set;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import net.community.chest.util.compare.InstancesComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * Reflects any calls to the stored instances
 * 
 * @param <V> Type of value being stored
 * @author Lyor G.
 * @since Apr 22, 2009 1:49:15 PM
 */
public class InvokersSet<V> extends UniqueInstanceSet<V> implements InvocationHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2652774559358303202L;
	/**
	 * TRUE if use a copy of itself when invoking or use <code>this</code>
	 * instance (concurrent modifications avoidance)
	 */
	private boolean	_useCopy;
	public boolean isUseCopy ()
	{
		return _useCopy;
	}

	public void setUseCopy (boolean useCopy)
	{
		_useCopy = useCopy;
	}

	public InvokersSet (InstancesComparator<? super V> c, boolean useCopy)
	{
		super(c);
		_useCopy = useCopy;
	}

	public InvokersSet (InstancesComparator<? super V> c)
			throws IllegalArgumentException
	{
		this(c, false);
	}

	public InvokersSet (Class<V> vc, Collection<? extends V> c, boolean useCopy)
		throws IllegalArgumentException
	{
		super(vc, c);
		_useCopy = useCopy;
	}

	public InvokersSet (Class<V> vc, Collection<? extends V> c)
			throws IllegalArgumentException
	{
		this(vc, c, false);
	}

	public InvokersSet (Class<V> vc, boolean useCopy) throws IllegalArgumentException
	{
		super(vc);
		_useCopy = useCopy;
	}

	public InvokersSet (Class<V> vc) throws IllegalArgumentException
	{
		this(vc, false);
	}

	public Object invoke (final Collection<? extends V> vl, final Method m, Object ... args)
		throws Throwable
	{
		if ((null == vl) || (vl.size() <= 0))
			return null;

		Object	o=null;
		for (final V v : vl)
		{
			if (null == v)
				continue;

			final Object	r=m.invoke(v, args);
			if (r != null)
				o = r;
		}

		return o;
	}
	/*
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke (Object proxy, Method m, Object[] args)
			throws Throwable
	{
		if (size() <= 0)
			return null;

		final Collection<? extends V>	vl;
		synchronized(this)
		{
			if (isUseCopy())
				vl = new ArrayList<V>(this);
			else
				vl = this;
		}

		return invoke(vl, m, args);
	}
}
