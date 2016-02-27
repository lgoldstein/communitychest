/*
 * 
 */
package net.community.chest.apache.httpclient;

import net.community.chest.CoVariantReturn;

import org.apache.commons.httpclient.NameValuePair;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 12, 2009 2:57:33 PM
 */
public class ExtendedNameValuePair extends NameValuePair implements Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2300012704404191458L;
	public ExtendedNameValuePair ()
	{
		this(null, null);
	}

	public ExtendedNameValuePair (String name, String value)
	{
		super(name, value);
	}

	public ExtendedNameValuePair (NameValuePair p)
	{
		this((null == p) ? null : p.getName(), (null == p) ? null : p.getValue());
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public NameValuePair clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
}
