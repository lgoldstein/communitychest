package net.community.chest.awt.dom.proxy;

import java.awt.Window;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <W> The reflected {@link Window} type
 * @author Lyor G.
 * @since Mar 20, 2008 10:08:49 AM
 */
public class WindowReflectiveProxy<W extends Window> extends ContainerReflectiveProxy<W> {
	public WindowReflectiveProxy (Class<W> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected WindowReflectiveProxy (Class<W> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	// special attribute(s)
	public static final String	ICON_IMAGE_ATTR="IconImage",
								REL_LOCATION_ATTR="LocationRelativeTo";
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected W updateObjectAttribute (W src, String name, String value, Method setter) throws Exception
	{
		if (ICON_IMAGE_ATTR.equalsIgnoreCase(name))
			return updateObjectResourceAttribute(src, name, value, setter);
		else if (REL_LOCATION_ATTR.equalsIgnoreCase(name))
		{
			if ("parent".equalsIgnoreCase(value))
				src.setLocationRelativeTo(src.getParent());
			else if ("none".equalsIgnoreCase(value))
				src.setLocationRelativeTo(null);
			else
				throw new NoSuchElementException("updateObjectAttribute(" + name + ") unknown value: " + value);
			return src;
		}

		return super.updateObjectAttribute(src, name, value, setter);
	}
}
