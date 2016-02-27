/*
 * 
 */
package net.community.chest.jfree.jfreechart.data.general;

import java.lang.reflect.Method;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy;
import net.community.chest.reflect.ClassUtil;

import org.jfree.data.general.Series;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <S> The type of {@link Series} being reflected
 * @author Lyor G.
 * @since Feb 11, 2009 12:41:09 PM
 */
public class SeriesReflectiveProxy<S extends Series> extends ChartReflectiveAttributesProxy<S> {
	protected SeriesReflectiveProxy (Class<S> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	KEY_ATTR="Key";
	public Comparable<?> getKeyValue (String name, String value) throws Exception
	{
		final int	vLen=(null == value) ? 0 : value.length();
		if (vLen <= 0)
			return null;

		final int	sPos=value.indexOf('['),
					ePos=(sPos > 0) ? value.indexOf(']', sPos) : (-1);
		if ((sPos <= 0) || (ePos <= 0) || (ePos <= (sPos+1)))
			return value;	// if incomplete specification assume String

		final String						keyType=value.substring(0, sPos),
											keyValue=value.substring(sPos + 1, ePos);
		final Class<?>						type=ClassUtil.loadClassByName(keyType);
		final ValueStringInstantiator<?>	vsi=resolveAttributeInstantiator(name, type);
		final Object						o=vsi.newInstance(keyValue);
		return (Comparable<?>) o;
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected S updateObjectAttribute (S src, String name, String value, Method setter) throws Exception
	{
		if (KEY_ATTR.equalsIgnoreCase(name))
		{
			final Comparable<?>	key=getKeyValue(name, value);
			setter.invoke(src, key);
			return src;
		}

		return super.updateObjectAttribute(src, name, value, setter);
	}
}
