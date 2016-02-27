package net.community.chest.dom;

import net.community.chest.lang.ObjectIndicatorExceptionContainer;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Carries exception(s) that occurred due to parsing an XML {@link Element}</P>
 * 
 * @author Lyor G.
 * @since Jul 30, 2007 7:57:31 AM
 */
public class ElementIndicatorExceptionContainer extends ObjectIndicatorExceptionContainer<Element> {
	public ElementIndicatorExceptionContainer (Element value, Throwable t)
	{
		super(Element.class, value, t);
	}

	public ElementIndicatorExceptionContainer (Element value)
	{
		this(value, null);
	}

	public ElementIndicatorExceptionContainer (Throwable t)
	{
		this(null, t);
	}

	public ElementIndicatorExceptionContainer ()
	{
		this(null, null);
	}
}
