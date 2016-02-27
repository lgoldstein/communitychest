package net.community.chest.reflect;

import net.community.chest.convert.NumberValueStringInstantiator;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Default implementation for {@link NumberValueStringInstantiator}
 * 
 * @param <N> Type of constructed {@link Number}
 * @author Lyor G.
 * @since Jul 11, 2007 9:56:36 AM
 */
public class NumberValueStringConstructor<N extends Number>
			extends ValueStringConstructor<N>
			implements NumberValueStringInstantiator<N> {
	private final Class<N>	_prmClass;
	/*
	 * @see net.community.chest.reflect.NumberValueStringInstantiator#getPrimitiveValuesClass()
	 */
	@Override
	public final /* no cheating */ Class<N> getPrimitiveValuesClass ()
	{
		return _prmClass;
	}
	/**
	 * @param prmClass the TYPE primitive {@link Class} - may NOT be null
	 * @param clsClass the {@link Class} of the equivalent type object
	 * @throws IllegalArgumentException if cannot initialize the object
	 */
	public NumberValueStringConstructor (Class<N> prmClass, Class<N> clsClass) throws IllegalArgumentException
	{
		super(clsClass);

		if (null == (_prmClass=prmClass))
			throw new IllegalArgumentException(getConstructorExceptionLocation() + " no primitive class instance");
	}
	/*
	 * @see net.community.chest.reflect.ValueNumberInstantiator#getInstanceNumber(java.lang.Object)
	 */
	@Override
	public N getInstanceNumber (N inst) throws Exception
	{
		return inst;
	}
	/*
	 * @see net.community.chest.reflect.ValueNumberInstantiator#newInstance(java.lang.Number)
	 */
	@Override
	public N newInstance (N num) throws Exception
	{
		return num;
	}

	public static final NumberValueStringConstructor<Integer>	INTEGER=
			new NumberValueStringConstructor<Integer>(Integer.TYPE, Integer.class);
	public static final NumberValueStringConstructor<Long>	LONG=
		new NumberValueStringConstructor<Long>(Long.TYPE, Long.class);
}
