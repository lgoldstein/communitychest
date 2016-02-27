/*
 * 
 */
package net.community.chest.awt.border;

import java.util.NoSuchElementException;

import javax.swing.border.Border;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.ExceptionUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link Border} type
 * @author Lyor G.
 * @since Dec 10, 2008 9:03:47 AM
 */
public abstract class BorderReflectiveProxy<B extends Border> extends UIReflectiveAttributesProxy<B> {
	protected BorderReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected BorderReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final XmlValueInstantiator<? extends Border> getBorderInstantiator (final Element elem)
	{
		final String	type=(null == elem) ? null : elem.getAttribute(CLASS_ATTR);
		if ((null == type) || (type.length() <= 0))
			return null;

		if ("titled".equalsIgnoreCase(type))
			return TitledBorderReflectiveProxy.TITLED;
		else if ("compound".equalsIgnoreCase(type))
			return CompoundBorderReflectiveProxy.COMPOUND;

		// assume covers all others
		try
		{
			return BorderValueInstantiator.getSpecificBorderInstantiator(type);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}

	public static final String	BORDER_ELEM_NAME="border";
	public boolean isBorderElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, BORDER_ELEM_NAME);
	}

	public XmlValueInstantiator<? extends Border> getBorderProxy (final Element elem) throws Exception
	{
		if (null == elem)
			return null;
		// uses the class attribute to determine type of instantiator to return
		final XmlValueInstantiator<? extends Border>	proxy=getBorderInstantiator(elem);
		if (null == proxy)
			throw new NoSuchElementException("getBorderProxy(" + DOMUtils.toString(elem) + ") no proxy available");

		return proxy;
	}
}
