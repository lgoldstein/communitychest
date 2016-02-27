/*
 * 
 */
package net.community.chest.text;

import java.lang.reflect.Method;
import java.text.Format;
import java.util.Map;

import net.community.chest.dom.proxy.ReflectiveAttributesProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> Type of {@link Format} being reflected
 * @author Lyor G.
 * @since Jan 12, 2009 3:14:25 PM
 */
public abstract class FormatReflectiveProxy<F extends Format> extends ReflectiveAttributesProxy<F> {
	protected FormatReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	// virtual attribute used to initialize instances with a pre-defined format
	public static final String	FORMAT_VIRTATTR="format";
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#handleUnknownAttribute(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	protected F handleUnknownAttribute (F src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
	{
		if (FORMAT_VIRTATTR.equalsIgnoreCase(name))
			return src;

		return super.handleUnknownAttribute(src, name, value, accsMap);
	}
}
