package net.community.chest.net.proto.text.imap4;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.EnumUtil;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Specifies a single namespace definition</P>
 * 
 * @author Lyor G.
 * @since Sep 20, 2007 12:03:49 PM
 */
public class IMAP4Namespace implements Serializable, PubliclyCloneable<IMAP4Namespace> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2973088048080559829L;
	public IMAP4Namespace ()
	{
		super();
	}
	/**
	 * Namespace type
	 */
	private IMAP4NamespaceType	_type	/* =null */;
	public IMAP4NamespaceType getType ()
	{
		return _type;
	}

	public void setType (IMAP4NamespaceType type)
	{
		_type = type;
	}
	/**
	 * Prefix of namespace
	 */
	private String	_prefix	/* =null */;
	public String getPrefix ()
	{
		return _prefix;
	}

	public void setPrefix (String prefix)
	{
		_prefix = prefix;
	}
	/**
	 * Delimiter for the namespace
	 */
	private char _delim='\0';
	public char getDelimiter ()
	{
		return _delim;
	}

	public void setDelimiter (char delim)
	{
		_delim = delim;
	}
	/**
	 * Initialize object to empty values
	 */
	public void reset ()
	{
		setPrefix(null);
		setDelimiter('\0');
	}

	public IMAP4Namespace (IMAP4NamespaceType type, String prefix, char delim)
	{
		_type = type;
		_prefix = prefix;
		_delim = delim;
	}

	public IMAP4Namespace (IMAP4NamespaceType type)
	{
		this(type, null, '\0');
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public IMAP4Namespace clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if ((null == obj) || (!(obj instanceof IMAP4Namespace)))
			return false;
		if (this == obj)
			return true;

		final IMAP4Namespace	ns=(IMAP4Namespace) obj;
		return (getDelimiter() == ns.getDelimiter())
			&& (0 == EnumUtil.compareValues(getType(), ns.getType()))
			&& (0 == StringUtil.compareDataStrings(getPrefix(), ns.getPrefix(), true))
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return getDelimiter()
			+ EnumUtil.getValueHashCode(getType())
			+ StringUtil.getDataStringHashCode(getPrefix(), true)
			;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final char	delim=getDelimiter();
		return getType()
			+ ":" + getPrefix()
			+ "[" + (('\0' == delim) ? "\\0" : String.valueOf(delim)) + "]"
			;
	}

}
