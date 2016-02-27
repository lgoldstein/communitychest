/*
 * 
 */
package net.community.chest.awt.layout.gridbag;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <C> The reflected {@link ExtendedGridBagConstraints} type
 * @author Lyor G.
 * @since Jan 8, 2009 8:37:58 AM
 */
public class ExtendedGridBagConstraintsReflectiveProxy<C extends ExtendedGridBagConstraints>
		extends UIReflectiveAttributesProxy<C> {
	protected ExtendedGridBagConstraintsReflectiveProxy (Class<C> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public ExtendedGridBagConstraintsReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#handleUnknownAttribute(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	protected C handleUnknownAttribute (C src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
	{
		if (ExtendedGridBagConstraints.ANCHOR_ATTR.equalsIgnoreCase(name))
		{
			final GridBagAnchorType	a=GridBagAnchorType.fromString(value);
			if (null == a)
				throw new NoSuchElementException("updateObjectAttribute(" + name + ") unknown value: " + value);

			src.setAnchorType(a);
			return src;
		}
		else if (ExtendedGridBagConstraints.FILL_ATTR.equalsIgnoreCase(name))
		{
			final GridBagFillType	f=GridBagFillType.fromString(value);
			if (null == f)
				throw new NoSuchElementException("updateObjectAttribute(" + name + ") unknown value: " + value);

			src.setFillType(f);
			return src;
		}

		return super.handleUnknownAttribute(src, name, value, accsMap);
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected C updateObjectAttribute (C src, String name, String value, Method setter) throws Exception
	{
		if (ExtendedGridBagConstraints.GRIDHEIGHT_ATTR.equalsIgnoreCase(name)
		 || ExtendedGridBagConstraints.GRIDWIDTH_ATTR.equalsIgnoreCase(name))
		{
			final Integer	v=ExtendedGridBagConstraints.getGridSizingValue(value);
			if (null == v)
				throw new NoSuchElementException("updateObjectAttribute(" + name + ") unknown value: " + value);

			setter.invoke(src, v);
			return src;
		}
		else if (ExtendedGridBagConstraints.GRIDX_ATTR.equalsIgnoreCase(name)
			  || ExtendedGridBagConstraints.GRIDY_ATTR.equalsIgnoreCase(name))
		{
			final Integer	v=GridBagXYValueStringInstantiator.getGridXYValue(value);
			if (null == v)
				throw new NoSuchElementException("updateObjectAttribute(" + name + ") unknown value: " + value);

			setter.invoke(src, v);
			return src;
		}
		else if (ExtendedGridBagConstraints.IPADX_ATTR.equalsIgnoreCase(name)
			  || ExtendedGridBagConstraints.IPADY_ATTR.endsWith(name))
		{
			final Integer	v=ExtendedGridBagConstraints.getPadXYValue(value);
			if (null == v)
				throw new NoSuchElementException("updateObjectAttribute(" + name + ") unknown value: " + value);

			setter.invoke(src, v);
			return src;
		}

		return super.updateObjectAttribute(src, name, value, setter);
	}

	public static final ExtendedGridBagConstraintsReflectiveProxy<ExtendedGridBagConstraints> EGBC=
		new ExtendedGridBagConstraintsReflectiveProxy<ExtendedGridBagConstraints>(ExtendedGridBagConstraints.class, true);
}
