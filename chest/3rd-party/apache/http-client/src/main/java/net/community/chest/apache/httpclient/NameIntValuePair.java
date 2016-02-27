package net.community.chest.apache.httpclient;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 10, 2007 12:03:49 PM
 */
public class NameIntValuePair extends ExtendedNameValuePair {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3979693641761319587L;
	/**
	 * Empty constructor - no name/zero value
	 */
	public NameIntValuePair ()
	{
		super();
	}
	/**
	 * Currently set integer value
	 */
	private int	_value /* =0 */;
	public int getIntValue ()
	{
		return _value;
	}

	public void setIntValue (int value)
	{
		super.setValue(String.valueOf(value));
		_value = value;
	}
	/**
	 * Auto-initialized pair value
	 * @param name parameter name
	 * @param value value to be set
	 */
	public NameIntValuePair (String name, int value)
	{
		super(name, String.valueOf(value));

		_value = value;
	}
	/**
	 * Initializes value to zero
	 * @param name parameter name
	 */
	public NameIntValuePair (String name)
	{
		this(name, 0);
	}

	public NameIntValuePair (NameIntValuePair p)
	{
		this((null == p) ? null : p.getName(), (null == p) ? 0 : p.getIntValue());
	}
	/*
	 * @see org.apache.commons.httpclient.NameValuePair#getValue()
	 */
	@Override
	public String getValue ()
	{
		return String.valueOf(_value);
	}
	/* NOTE !!! throws NumberFormatException if value is NOT a number
	 * @see org.apache.commons.httpclient.NameValuePair#setValue(java.lang.String)
	 */
	@Override
	public void setValue (String value)
	{
		_value = Integer.parseInt(value);
		super.setValue(value);
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object o)
	{
		if ((null == o) || (!(o instanceof NameIntValuePair)))
			return false;
		if (this == o)
			return true;

		final NameIntValuePair	nip=(NameIntValuePair) o;
		// if values not same, then no need to check names
		return (getIntValue() == nip.getIntValue())
			&& (0 == StringUtil.compareDataStrings(getName(), nip.getName(), true))
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getName(), true) + getIntValue();
	}
	/*
	 * @see net.community.chest.apache.httpclient.ExtendedNameValuePair#clone()
	 */
	@Override
	public NameIntValuePair clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
}
