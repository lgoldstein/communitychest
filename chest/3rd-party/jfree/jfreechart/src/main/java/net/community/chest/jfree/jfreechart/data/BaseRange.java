/*
 * 
 */
package net.community.chest.jfree.jfreechart.data;

import net.community.chest.convert.DoubleValueStringConstructor;

import org.jfree.data.Range;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 27, 2009 2:24:55 PM
 */
public class BaseRange extends Range {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1983493529169345309L;
	public BaseRange (double lower, double upper)
	{
		super(lower, upper);
	}

	public static final String	LOWER_ATTR="lower", UPPER_ATTR="upper";
	public BaseRange (Element elem) throws RuntimeException
	{
		this(DoubleValueStringConstructor.DEFAULT.fromString(elem.getAttribute(LOWER_ATTR)),
			 DoubleValueStringConstructor.DEFAULT.fromString(elem.getAttribute(UPPER_ATTR)));
	}
}
