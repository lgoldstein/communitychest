/*
 * 
 */
package net.community.chest.awt;

import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.community.chest.awt.attributes.Sizeable;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Type of <code>getXXXSize</code> available for a {@link Component}</P>
 * @author Lyor G.
 * @since Mar 23, 2009 12:37:43 PM
 */
public enum ComponentSizeType {
	SIZE {
			/*
			 * @see net.community.chest.awt.ComponentSizeType#getSize(java.awt.Component)
			 */
			@Override
			public Dimension getSize (Component c)
			{
				return (null == c) ? null : c.getSize();
			}
			/*
			 * @see net.community.chest.awt.ComponentSizeType#setSize(java.awt.Component, java.awt.Dimension)
			 */
			@Override
			public void setSize (Component c, Dimension d)
			{
				if (c != null)
					c.setSize(d);
			}
		},
	MAXIMUM {
			/*
			 * @see net.community.chest.awt.ComponentSizeType#getSize(java.awt.Component)
			 */
			@Override
			public Dimension getSize (Component c)
			{
				return (null == c) ? null : c.getMaximumSize();
			}
			/*
			 * @see net.community.chest.awt.ComponentSizeType#setSize(java.awt.Component, java.awt.Dimension)
			 */
			@Override
			public void setSize (Component c, Dimension d)
			{
				if (c != null)
					c.setMaximumSize(d);
			}
		},
	MINIMUM {
			/*
			 * @see net.community.chest.awt.ComponentSizeType#getSize(java.awt.Component)
			 */
			@Override
			public Dimension getSize (Component c)
			{
				return (null == c) ? null : c.getMinimumSize();
			}
			/*
			 * @see net.community.chest.awt.ComponentSizeType#setSize(java.awt.Component, java.awt.Dimension)
			 */
			@Override
			public void setSize (Component c, Dimension d)
			{
				if (c != null)
					c.setMinimumSize(d);
			}
		},
	PREFERRED {
			/*
			 * @see net.community.chest.awt.ComponentSizeType#getSize(java.awt.Component)
			 */
			@Override
			public Dimension getSize (Component c)
			{
				return (null == c) ? null : c.getPreferredSize();
			}
			/*
			 * @see net.community.chest.awt.ComponentSizeType#setSize(java.awt.Component, java.awt.Dimension)
			 */
			@Override
			public void setSize (Component c, Dimension d)
			{
				if (c != null)
					c.setPreferredSize(d);
			}
		};

	public abstract Dimension getSize (Component c);
	public abstract void setSize (Component c, Dimension d);

	public static final List<ComponentSizeType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final ComponentSizeType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final ComponentSizeType fromMethod (final Method m)
	{
		final AttributeMethodType	t=(null == m) ? null : AttributeMethodType.classifyAttributeMethod(m);
		final Class<?>				c=(null == t) ? null : t.getAttributeType(m);
		if ((null == c) || (!Dimension.class.isAssignableFrom(c)))
			return null;

		final String	n=t.getPureAttributeName(m);
		if (Sizeable.ATTR_NAME.equalsIgnoreCase(n))
			return SIZE;
		else if (StringUtil.endsWith(n, Sizeable.ATTR_NAME, true, false))
			return fromString(n.substring(0, n.length() - Sizeable.ATTR_NAME.length()));

		return null;
	}

	public static final Map<ComponentSizeType,Dimension> getSizes (final Component c)
	{
		if (null == c)
			return null;

		Map<ComponentSizeType,Dimension>	ret=null;
		for (final ComponentSizeType v : VALUES)
		{
			final Dimension	d=(null == v) ? null : v.getSize(c);
			if (null == d)
				continue;

			if (null == ret)
				ret = new EnumMap<ComponentSizeType,Dimension>(ComponentSizeType.class);
			ret.put(v, d);
		}

		return ret;
	}

	public static final void setSizes (final Component c, final Collection<? extends Map.Entry<ComponentSizeType,? extends Dimension>>	sl)
	{
		if ((null == c) || (null == sl) || (sl.size() <= 0))
			return;

		for (final Map.Entry<ComponentSizeType,? extends Dimension> se : sl)
		{
			final ComponentSizeType	szt=(null == se) ? null : se.getKey();
			final Dimension			d=(null == se) ? null : se.getValue();
			if ((null == szt) || (null == d))
				continue;
			szt.setSize(c, d);
		}
	}

	public static final void setSizes (final Component c, final Map<ComponentSizeType,? extends Dimension> sm)
	{
		final Collection<? extends Map.Entry<ComponentSizeType,? extends Dimension>>	sl=
			((null == c) || (null == sm) || (sm.size() <= 0)) ? null : sm.entrySet();
		setSizes(c, sl);
	}
}
