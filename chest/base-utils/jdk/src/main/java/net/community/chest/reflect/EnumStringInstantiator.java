package net.community.chest.reflect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import net.community.chest.convert.ValueNumberInstantiator;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.EnumUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * Copyright 2007 as per GPLv2
 * 
 * A more efficient implementation of {@link net.community.chest.convert.ValueStringInstantiator} for
 * {@link Enum}-s. By default uses {@link CollectionsUtils#fromString(java.util.Collection, String, boolean)}
 *  
 * @param <E> The generic {@link Enum} class
 * @author Lyor G.
 * @since Jul 10, 2007 1:24:43 PM
 */
public class EnumStringInstantiator<E extends Enum<E>>
				extends AbstractXmlValueStringInstantiator<E>
				implements ValueNumberInstantiator<Integer,E> {
	private final boolean	_caseSensitive;
	public final boolean isCaseSensitive ()
	{
		return _caseSensitive;
	}

	public EnumStringInstantiator (Class<E> objClass, boolean caseSensitive) throws IllegalArgumentException
	{
		super(objClass);
		_caseSensitive = caseSensitive;
	}
	// 
	/**
	 * Cached {@link Enum}-s {@link List} of <U>all</U> values in order to make
	 * {@link #newInstance(String)} call more efficient by avoiding using a
	 * new array every time (which happens if {@link Class#getEnumConstants()}
	 * is called). Lazy initialized by first call to {@link #getValues()}
	 */
	private List<E>	_values	/* =null */;
	public synchronized List<E> getValues ()
	{
		if (null == _values)
			_values = Collections.unmodifiableList(Arrays.asList(getValuesClass().getEnumConstants()));
		return _values;
	}
	/**
	 * @param values Cached {@link Enum}-s {@link List} of <U>all</U> values in order
	 * to make {@link #newInstance(String)} call more efficient. <B>Caveat
	 * emptor:</B> undefined behavior may occur if this array does not "cover"
	 * <U>all</U> available {@link Enum}-s 
	 */
	public synchronized void setValues (List<E> values)
	{
		_values = values;
	}
	/* @throws NoSuchElementException if non-null/empty string and no match found
	 * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public E newInstance (final String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final E	value=CollectionsUtils.fromString(getValues(), s, isCaseSensitive());
		if (null == value)
			throw new NoSuchElementException(getExceptionLocation("newInstance") + "[" + s + "] no match found");

		return value;
	}
	/*
	 * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (final E inst) throws Exception
	{
		return (null == inst) ? null : inst.toString();
	}
	/*
	 * @see net.community.chest.reflect.ValueNumberInstantiator#getInstanceNumber(java.lang.Object)
	 */
	@Override
	public Integer getInstanceNumber (final E inst) throws Exception
	{
		return (null == inst) ? null : Integer.valueOf(inst.ordinal());
	}
	/*
	 * @see net.community.chest.reflect.ValueNumberInstantiator#newInstance(java.lang.Number)
	 */
	@Override
	public E newInstance (final Integer num) throws Exception
	{
		if (null == num)
			return null;

		final E	value=EnumUtil.fromOrdinal(getValues(), num.intValue());
		if (null == value)
			throw new NoSuchElementException(getExceptionLocation("newInstance") + "[" + num + "] no ordinal found");

		return value;
	}
}
