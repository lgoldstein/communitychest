package net.community.chest.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;

import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides some useful "shortcut" implementations for the {@link DynamicMBean}
 * implementing classes</P>
 * 
 * @author Lyor G.
 * @since Aug 14, 2007 1:44:06 PM
 */
public abstract class AbstractDynamicMBean extends EmbeddedJMXErrorHandler implements DynamicMBean {
	protected AbstractDynamicMBean (JMXErrorHandler eh)
	{
		super(eh);
	}

	protected AbstractDynamicMBean ()
	{
		this(null);
	}

	protected void handleSetAttributeException (final Attribute a, final Throwable t)
	{
		if ((null == a) || (null == t))	// just so compiler does not complain about un-referenced objects
			throw errorThrowable(new IllegalArgumentException(ClassUtil.getExceptionLocation(getClass(), "handleSetAttributeException") + " incomplete arguments"));
	}
	/*
	 * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
	 */
	@Override
	public AttributeList setAttributes (final AttributeList attributes)
	{
		final int			numAttrs=(null == attributes) ? 0 : attributes.size();
		final AttributeList	retVals=(numAttrs <= 0) ? new AttributeList() : new AttributeList(numAttrs);
		for (int	aIndex=0; aIndex < numAttrs; aIndex++)
		{
			final Attribute	a=(Attribute) attributes.get(aIndex);
			try
			{
				setAttribute(a);
				retVals.add(a);
			}
			catch(Exception e)
			{
				handleSetAttributeException(a, e);
			}
		}

		return retVals;
	}

	protected void handleGetAttributeException (final String attrName, final Throwable t)
	{
		if ((null == attrName) || (attrName.length() <= 0) || (null == t))	// just so compiler does not complain about un-referenced objects
			throw errorThrowable(new IllegalArgumentException(ClassUtil.getExceptionLocation(getClass(), "handleGetAttributeException") + " incomplete arguments"));
	}
	/*
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	@Override
	public AttributeList getAttributes (final String[] attributes)
	{
		final int			numAttrs=(null == attributes) ? 0 : attributes.length;
		final AttributeList	retVals=(numAttrs <= 0) ? new AttributeList() : new AttributeList(numAttrs);
		for (int	aIndex=0; aIndex < numAttrs; aIndex++)
		{
			final String	attrName=attributes[aIndex];
			try
			{
				final Object	attrValue=getAttribute(attrName);
				final Attribute	a=new Attribute(attrName, attrValue);
				retVals.add(a);
			}
			catch(Exception e)
			{
				handleGetAttributeException(attrName, e);
			}
		}

		return retVals;
	}
}
