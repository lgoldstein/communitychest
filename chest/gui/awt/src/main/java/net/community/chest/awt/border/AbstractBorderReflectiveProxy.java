/*
 * 
 */
package net.community.chest.awt.border;

import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.border.AbstractBorder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link AbstractBorder}
 * @author Lyor G.
 * @since Dec 10, 2008 9:04:52 AM
 */
public abstract class AbstractBorderReflectiveProxy<B extends AbstractBorder> extends BorderReflectiveProxy<B> {
	protected AbstractBorderReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected AbstractBorderReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	/*
	 * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#handleUnknownAttribute(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	protected B handleUnknownAttribute (B src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
	{
		/*
		 *  	We use this attribute to distinguish between various border
		 *  implementations (e.g., inside and outside border(s) for CompoundBorder)
		 */
		if (NAME_ATTR.equalsIgnoreCase(name))
			return src;

		return super.handleUnknownAttribute(src, name, value, accsMap);
	}
}
