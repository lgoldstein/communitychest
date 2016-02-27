package net.community.chest.util.datetime;

import java.lang.reflect.Constructor;
import java.util.Date;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @param <D> The {@link Date} generic type
 * @author Lyor G.
 * @since Dec 11, 2007 3:09:33 PM
 */
public class DateValueInstantiator<D extends Date> extends AbstractXmlValueStringInstantiator<D> {
	public DateValueInstantiator (Class<D> dClass)
	{
		super(dClass);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (D inst) throws Exception
	{
		return (null == inst) ? null : String.valueOf(inst.getTime());
	}

	private Constructor<D>	_ctor	/* =null */;
	public synchronized Constructor<D> getConstructor () throws Exception
	{
		if (null == _ctor)
		{
			final Class<D>	vClass=getValuesClass();
			if (null == (_ctor=vClass.getConstructor(Long.TYPE)))
				throw new NoSuchMethodException("No long value constructor");
		}

		return _ctor;
	}

	public synchronized void setConstructor (Constructor<D> ctor)
	{
		_ctor = ctor;
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public D newInstance (final String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final Long				tValue=Long.valueOf(s);
		final Constructor<D>	dCtor=getConstructor();
		return dCtor.newInstance(tValue);
	}

	public static final DateValueInstantiator<Date>	DEFAULT=new DateValueInstantiator<Date>(Date.class);
}
