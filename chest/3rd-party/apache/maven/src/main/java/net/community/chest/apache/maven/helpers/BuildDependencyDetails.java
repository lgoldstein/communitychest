package net.community.chest.apache.maven.helpers;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents a dependency entry</P>
 * 
 * @author Lyor G.
 * @since Aug 9, 2007 7:37:00 AM
 */
public class BuildDependencyDetails extends BaseTargetDetails {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4682697351081487364L;

	public BuildDependencyDetails ()
	{
		super();
	}

	private String	_scope	/* =null */;

	/**
	 * @return the &lt;scope&gt; value (if any)
	 */
	public String getScope ()
	{
		return _scope;
	}

	public void setScope (String scope)
	{
		_scope = scope;
	}

	private String	_systemPath	/* =null */;
	/**
	 * @return the &lt;systemPath&gt; value (if any)
	 */
	public String getSystemPath ()
	{
		return _systemPath;
	}

	public void setSystemPath (String systemPath)
	{
		_systemPath = systemPath;
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#clone()
	 */
	@Override
	@CoVariantReturn
	public BuildDependencyDetails clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}

	public int compareTo (BuildDependencyDetails dep)
	{
		if (null == dep)
			return (-1);

		int	nRes=super.compareTo(dep);
		if (nRes != 0)
			return nRes;
		if ((nRes=StringUtil.compareDataStrings(getScope(), dep.getScope(), false)) != 0)
			return nRes;
		if ((nRes=StringUtil.compareDataStrings(getSystemPath(), dep.getSystemPath(), true)) != 0)
			return nRes;

		return 0;
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#compareTo(net.community.chest.apache.maven.helpers.BaseTargetDetails)
	 */
	@Override
	public int compareTo (BaseTargetDetails td)
	{
		if (!(td instanceof BuildDependencyDetails))
			return (-1);
		else
			return compareTo((BuildDependencyDetails) td);
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (final Object obj)
	{
		if (!super.equals(obj))
			return false;
		if ((null == obj) || (!(obj instanceof BuildDependencyDetails)))
			return false;

		return (0 == compareTo((BuildDependencyDetails) obj));
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return super.hashCode()
			 + StringUtil.getDataStringHashCode(getScope(), false)
			 + StringUtil.getDataStringHashCode(getSystemPath(), true)
			 ;
	}

	public static final String	SCOPE_ELEM_NAME="scope";
	public String setScope (final Element elem) throws Exception
	{
		final String	val=DOMUtils.getElementStringValue(elem);
		if ((val != null) && (val.length() > 0))
			setScope(val);

		return val;
	}

	public static final String	SYSTEMPATH_ELEM_NAME="systemPath";
	public String setSystemPath (final Element elem) throws Exception
	{
		final String	val=DOMUtils.getElementStringValue(elem);
		if ((val != null) && (val.length() > 0))
			setSystemPath(val);

		return val;
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#handleUnknownElement(org.w3c.dom.Element, java.lang.String)
	 */
	@Override
	public void handleUnknownElement (final Element elem, final String tagName) throws Exception
	{
		if (SCOPE_ELEM_NAME.equalsIgnoreCase(tagName))
			setScope(elem);
		else if (SYSTEMPATH_ELEM_NAME.equalsIgnoreCase(tagName))
			setSystemPath(elem);
		else
			super.handleUnknownElement(elem, tagName);
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public BuildDependencyDetails fromXml (Element root) throws Exception
	{
		return getClass().cast(super.fromXml(root));
	}

	public BuildDependencyDetails (final Element elem) throws Exception
	{
		final BuildDependencyDetails	inst=fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched instances");
	}
	/*
	 * @see net.community.chest.apache.maven.helpers.BaseTargetDetails#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement toXml
		throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
	}
}
