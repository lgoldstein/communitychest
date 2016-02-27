/*
 * 
 */
package net.community.chest.javaagent.dumper.data;

import java.io.IOException;
import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 9:49:29 AM
 */
public class ParamInfo implements Serializable, Comparable<ParamInfo>, XmlConvertible<ParamInfo> {
	private static final long serialVersionUID = -6108509270183230287L;

	public ParamInfo ()
	{
		super();
	}

	public ParamInfo (Element root) throws Exception
	{
		final ParamInfo	info=fromXml(root);
		if (this != info)
			throw new IllegalStateException("Mismatched re-constructed instances");
	}

	private String	_type;
	public String getType ()
	{
		return _type;
	}

	public void setType (String type)
	{
		_type = type;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		return DOMUtils.addNonEmptyAttribute(doc.createElement(InfoUtils.PARAM_ELEMENT), InfoUtils.TYPE_ATTR, getType());
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public ParamInfo fromXml (Element root) throws Exception
	{
		final String	type=root.getAttribute(InfoUtils.TYPE_ATTR);
		if ((type != null) && (type.length() > 0))
			setType(type);
		return this;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getType(), true);
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof ParamInfo))
			return false;
		if (compareTo((ParamInfo) obj) != 0)
			return false;	// debug breakpoint

		return true;
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (ParamInfo o)
	{
		if (o == null)
			return (-1);
		if (this == o)
			return 0;

		return StringUtil.compareDataStrings(getType(), o.getType(), true);
	}

	public <A extends Appendable> A append (A sb) throws IOException
	{
		return InfoUtils.appendParamTypeAttribute(sb, getType());
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		try
		{
			return append(new StringBuilder()).toString();
		}
		catch(IOException e)	// unexpected
		{
			return e.getMessage();
		}
	}
}
