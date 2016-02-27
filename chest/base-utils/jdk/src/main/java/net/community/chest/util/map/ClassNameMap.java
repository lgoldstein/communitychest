package net.community.chest.util.map;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import net.community.chest.resources.PropertyAccessor;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 * <P>Holds {@link Class} names as keys using their <U>fully-qualified</U> name
 * as a <U>case sensitive</U> string as default</P>
 * 
 * @param <V> Type of mapped object
 * @author Lyor G.
 * @since Feb 28, 2008 9:14:16 AM
 */
public class ClassNameMap<V> extends TreeMap<String,V> implements PropertyAccessor<Class<?>, V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8645974705066418435L;

	public ClassNameMap ()
	{
		super();
	}

	public ClassNameMap (Comparator<? super String> c)
	{
		super(c);
	}

	public ClassNameMap (Map<? extends String, ? extends V> m)
	{
		super(m);
	}

	public V put (Class<?> c, V value)
	{
		if (null == c)
			throw new IllegalArgumentException("No " + Class.class.getSimpleName() + " instance provided");

		return super.put(c.getName(), value);
	}

	// special handling for some classes that have a common instantiator
	public V getDerivedClassValue (Class<?> c)
	{
		if (null == c)	// just so we don't get an "unreferenced" parameter warning
			return null;

		if (Date.class.isAssignableFrom(c) && (!c.isAssignableFrom(Date.class)))
			return get(Date.class);
		else if (File.class.isAssignableFrom(c) && (!c.isAssignableFrom(File.class)))
			return get(File.class);
		else if (Element.class.isAssignableFrom(c) && (!c.isAssignableFrom(Element.class)))
			return get(Element.class);
		else if (Calendar.class.isAssignableFrom(c) && (!c.isAssignableFrom(Calendar.class)))
			return get(Calendar.class);
		else if (TimeZone.class.isAssignableFrom(c) && (!c.isAssignableFrom(TimeZone.class)))
			return get(TimeZone.class);
		else if (DateFormat.class.isAssignableFrom(c) && (!c.isAssignableFrom(DateFormat.class)))
			return get(DateFormat.class);
		else if (NumberFormat.class.isAssignableFrom(c) && (!c.isAssignableFrom(NumberFormat.class)))
			return get(NumberFormat.class);

		return null;
	}

	public V get (Class<?> c)
	{
		if (null == c)
			return null;

		final V	ret=super.get(c.getName());
		if (null == ret)
			return getDerivedClassValue(c);

		return ret;
	}
	/*
	 * @see net.community.chest.resources.PropertyAccessor#getProperty(java.lang.Object)
	 */
	@Override
	public V getProperty (Class<?> key)
	{
		return get(key);
	}

	public V remove (final Class<?> c)
	{
		return (null == c) ? null : super.remove(c.getName());
	}

	public static final <V> V get (final Map<String,? extends V> cm, final Class<?> c)
	{
		if ((null == c) || (null == cm) || (cm.size() <= 0))
			return null;

		if (cm instanceof ClassNameMap<?>)
		{
			final ClassNameMap<? extends V>	cnm=(ClassNameMap<? extends V>) cm;
			return cnm.get(c);
		}

		return cm.get(c.getName());
	}
}
