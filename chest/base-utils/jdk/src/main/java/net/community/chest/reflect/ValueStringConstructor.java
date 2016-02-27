package net.community.chest.reflect;

import java.lang.reflect.Constructor;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Useful class that provides access to a {@link Constructor} that accepts
 * a single {@link String} argument - e.g., all primitive type...
 * 
 * @param <V> Type of constructed object(s)
 * @author Lyor G.
 * @since Jul 10, 2007 12:50:15 PM
 */
public class ValueStringConstructor<V> extends AbstractXmlValueStringInstantiator<V> {
	private final Constructor<V>	_ctor;
	public final Constructor<V> getConstructor ()
	{
		return _ctor;
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public V newInstance (final String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		return getConstructor().newInstance(s);
	}
	/**
	 * @param ctor {@link Constructor} to be used. <B>Note:</B> does not care
	 * about the <U>visibility</U> of the constructor
	 * @throws Exception if bad/illegal {@link Constructor} - e.g., null, not
	 * expected number or type of parameter(s) (i.e., exactly <U>one</U>
	 * parameter of {@link String} type)
	 */
	public ValueStringConstructor (final Constructor<V> ctor) throws Exception
	{
		super(ctor.getDeclaringClass());

		// make sure exactly one argument of type string
		final Class<?>[]	params=ctor.getParameterTypes();
		final Class<?>		p=((null == params) || (params.length != 1)) ? null : params[0];
		if ((null == p) || (!p.isAssignableFrom(String.class)))
			throw new NoSuchMethodException(getConstructorExceptionLocation() + " bad/Illegal " + Constructor.class.getName() + " parameters");

		_ctor = ctor;
	}

	protected Constructor<V> getConstructor (final Class<V> valsClass) throws Exception
	{
		return valsClass.getConstructor(String.class);
	}
	/**
	 * @param valsClass {@link Class} of objects created by this constructor
	 * @throws IllegalArgumentException if unable to retrieve the single {@link String}
	 * argument {@link Constructor}. <B>Note:</B> does not care about the
	 * <U>visibility</U> of the constructor
	 * @see Class#getConstructor(Class[])
	 */
	public ValueStringConstructor (final Class<V> valsClass) throws IllegalArgumentException
	{
		super(valsClass);

		try
		{
			if (null == (_ctor=getConstructor(valsClass)))	// should not happen
				throw new NoSuchMethodException("No string constructor");
		}
		catch(Exception e)
		{
			if (e instanceof IllegalArgumentException)
				throw (IllegalArgumentException) e;
			else
				throw new IllegalArgumentException(e.getClass().getName() +  " while get string constructor of class=" + valsClass.getName() + ": " + e.getMessage());
		}
	}
	/*
	 * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (final V inst) throws Exception
	{
		return (null == inst) ? null : inst.toString();
	}

	public static final ValueStringConstructor<String>	STRING=
					new ValueStringConstructor<String>(String.class);
	public static final ValueStringConstructor<Boolean>	BOOLEAN=
					new ValueStringConstructor<Boolean>(Boolean.class);
}