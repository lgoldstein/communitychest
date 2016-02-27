package net.community.chest.apache.ant.helpers;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Useful base class for ANT tasks that support sub-elements with a name/value
 * pair attributes
 * 
 * @author Lyor G.
 * @since Jun 20, 2007 4:10:29 PM
 */
public class AttributeValuePair implements Serializable, PubliclyCloneable<AttributeValuePair> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2651391502330083493L;
	protected AttributeValuePair ()
	{
		// do nothing
	}

	private String	_name	/* =null */;
	public String getName ()
	{
		return _name;
	}
	public void setName (String name)
	{
		_name = name;
	}

	private String	_value	/* =null */;
	public String getValue ()
	{
		return _value;
	}

	public void setValue (String value)
	{
		_value = value;
	}

	protected AttributeValuePair (String name, String value)
	{
		setName(name);
		setValue(value);
	}
	/**
	 * Resets contents to null/empty value(s) 
	 */
	public void clear ()
	{
		setName(null);
		setValue(null);
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public AttributeValuePair clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getName() + "=" + getValue();
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if ((null == obj) || (!(obj instanceof AttributeValuePair)))
			return false;
		if (this == obj)
			return true;

		final AttributeValuePair	a=(AttributeValuePair) obj;
		{
			final String	tn=getName(), on=a.getName();
			if ((null == tn) || (tn.length() <= 0))
			{
				if ((on != null) && (on.length() > 0))
					return false;
			}
			else
			{
				if (!tn.equals(on))
					return false;
			}
		}

		final String	tv=getValue(), ov=a.getValue();
		if ((null == tv) || (tv.length() <= 0))
			return ((null == ov) || (ov.length() <= 0));
		else
			return tv.equals(ov);
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final String	n=getName(), v=getValue();
		return ((null == n) ? 0 : n.hashCode())
			 + ((null == v) ? 0 : v.hashCode())
			 ;
	}
}
