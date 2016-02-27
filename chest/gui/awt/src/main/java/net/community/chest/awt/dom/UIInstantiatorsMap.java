package net.community.chest.awt.dom;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.border.Border;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.reflect.StringInstantiatorsMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Adds some UI specific handling (e.g., {@link Border})
 * 
 * @author Lyor G.
 * @since Nov 29, 2007 3:35:55 PM
 */
public class UIInstantiatorsMap extends StringInstantiatorsMap {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7285764013847998072L;
	public UIInstantiatorsMap ()
	{
		super();
	}
	/*
	 * @see net.community.chest.reflect.StringInstantiatorsMap#getDerivedClassValue(java.lang.Class)
	 */
	@Override
	public ValueStringInstantiator<?> getDerivedClassValue (Class<?> c)
	{
		if (null == c)
			return null;

		if (Border.class.isAssignableFrom(c))
			return super.get(Border.class);
		else if (Paint.class.isAssignableFrom(c))
			return super.get(Color.class);
		else if (Point2D.class.isAssignableFrom(c))
			return super.get(Point2D.class);
		else if (Line2D.class.isAssignableFrom(c))
			return super.get(Line2D.class);

		return super.getDerivedClassValue(c);
	}
}
