package net.community.chest.apache.maven.helpers;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import net.community.chest.CoVariantReturn;
import net.community.chest.Triplet;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.VersionComparator;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 8, 2007 3:56:41 PM
 */
public class BaseTargetDetails extends Triplet<String,String,String>
								implements  XmlConvertible<BaseTargetDetails>,
										  	PubliclyCloneable<BaseTargetDetails>,
										  	Comparable<BaseTargetDetails>,
										  	Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3432006014659663078L;
	/**
	 * @return the &lt;groupId&gt; value
	 */
	public String getGroupId ()
	{
		return getV1();
	}

	public void setGroupId (String groupId)
	{
		setV1(groupId);
	}
	/**
	 * @return the &lt;artifactId&gt; value
	 */
	public String getArtifactId ()
	{
		return getV2();
	}

	public void setArtifactId (String artifactId)
	{
		setV2(artifactId);
	}
	/**
	 * @return the &lt;version&gt; value
	 */
	public String getVersion ()
	{
		return getV3();
	}

	public void setVersion (String version)
	{
		setV3(version);
	}

	public String getBaseTargetName ()
	{
		return getArtifactId() + "-" + getVersion();
	}

	public BaseTargetDetails (String groupId, String artifactId, String version)
	{
		super(groupId, artifactId, version);
	}

	public BaseTargetDetails ()
	{
		this(null, null, null);
	}
	/*
	 * @see net.community.chest.Triplet#clone()
	 */
	@Override
	@CoVariantReturn
	public BaseTargetDetails clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (BaseTargetDetails td)
	{
		if (null == td)
			return (-1);

		int	nRes=StringUtil.compareDataStrings(getGroupId(), td.getGroupId(), true);
		if (nRes != 0)
			return nRes;
		if ((nRes=StringUtil.compareDataStrings(getArtifactId(), td.getArtifactId(), true)) != 0)
			return nRes;
		if ((nRes=VersionComparator.compareVersions(getVersion(), td.getVersion())) != 0)
			return nRes;
		return 0;
	}
	/*
	 * @see net.community.chest.Triplet#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (final Object obj)
	{
		if (!(obj instanceof BaseTargetDetails))
			return false;
		if (this == obj)
			return true;

		return (0 == compareTo((BaseTargetDetails) obj));
	}
	/*
	 * @see net.community.chest.Triplet#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getGroupId(), true)
		 	 + StringUtil.getDataStringHashCode(getArtifactId(), true)
		 	 + StringUtil.getDataStringHashCode(getVersion(), true)
		 	 ;
	}
	/*
	 * @see net.community.chest.Triplet#toString()
	 */
	@Override
	public String toString ()
	{
		return getArtifactId() + "-" + getVersion();
	}

	public URL toURL (final URL baseURL, final String ext, final boolean isSource) throws MalformedURLException
	{
		if (null == baseURL)
			throw new MalformedURLException("No base URL specified");

		final String	pth=baseURL.toString(),
						grp=getGroupId(),
						art=getArtifactId(),
						ver=getVersion();
		final int		pLen=(null == pth) ? 0 : pth.length(),
						gLen=(null == grp) ? 0 : grp.length(),
						aLen=(null == art) ? 0 : art.length(),
						vLen=(null == ver) ? 0 : ver.length(),
						eLen=(null == ext) ? 0 : ext.length();
		if ((pLen <= 0) || (eLen <= 0) || (gLen <= 0) || (aLen <= 0) || (vLen <= 0))
			throw new MalformedURLException("Incomplete dependency specification");

		final StringBuilder	sb=new StringBuilder(pLen + gLen + 2 * (aLen + vLen + 2) + (isSource ? BuildTargetFile.SRC_FILE_TYPE.length() : 0) + eLen + 8)
										.append(pth);
		if (pth.charAt(pLen-1) != '/')
			sb.append('/');
		sb.append(grp.replace('.', '/'))
		  .append('/')
		  .append(art)
		  .append('/')
		  .append(ver)
		  .append('/')
		  .append(art)
		  .append('-')
		  .append(ver)
		  ;
		if (isSource)
			sb.append('-')
			  .append(BuildTargetFile.SRC_FILE_TYPE)
			  ;
		sb.append('.').append(ext);
		return new URL(sb.toString());
	}

	public static final String	GROUPID_ELEM_NAME="groupId";
	public String setGroupId (final Element elem) throws Exception
	{
		final String	val=DOMUtils.getElementStringValue(elem);
		if ((val != null) && (val.length() > 0))
			setGroupId(val);

		return val;
	}

	public static final String	ARTIFACTID_ELEM_NAME="artifactId";
	public String setArtifactId (final Element elem) throws Exception
	{
		final String	val=DOMUtils.getElementStringValue(elem);
		if ((val != null) && (val.length() > 0))
			setArtifactId(val);

		return val;
	}

	public static final String	VERSION_ELEM_NAME="version";
	public String setVersion (final Element elem) throws Exception
	{
		final String	val=DOMUtils.getElementStringValue(elem);
		if ((val != null) && (val.length() > 0))
			setVersion(val);

		return val;
	}

	public void handleUnknownElement (final Element elem, final String tagName) throws Exception
	{
		if ((null == elem) || (null == tagName) || (tagName.length() <= 0))	// just so compiler does not complain about unreferenced parameter
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getArgumentsExceptionLocation(getClass(), "handleUnknownElement", tagName) + " incomplete parameters");
	}
	/*
	 * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseTargetDetails fromXml (final Element root) throws Exception
	{
		final Collection<? extends Element>	el=DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
		if ((el != null) && (el.size() > 0))
		{
			for (final Element	elem : el)
			{
				if (null == elem)
					continue;

				final String	tagName=elem.getTagName();
				if (GROUPID_ELEM_NAME.equalsIgnoreCase(tagName))
					setGroupId(elem);
				else if (ARTIFACTID_ELEM_NAME.equalsIgnoreCase(tagName))
					setArtifactId(elem);
				else if  (VERSION_ELEM_NAME.equalsIgnoreCase(tagName))
					setVersion(elem);
				else
					handleUnknownElement(elem, tagName);
			}
		}

		return this;
	}

	public BaseTargetDetails (final Element elem) throws Exception
	{
		final BaseTargetDetails	inst=fromXml(elem);
		if (inst != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched instances");
	}

	public static final String	DEPENDENCY_ELEM_NAME="dependency";
	public static final Element toXml (
			final BaseTargetDetails dtl, final Document doc, final boolean asAttributes) throws Exception
	{
		if (null == dtl)
			return null;

		final Element	root=doc.createElement(DEPENDENCY_ELEM_NAME);
		final String[]	vals={
				GROUPID_ELEM_NAME, 		dtl.getGroupId(),
				ARTIFACTID_ELEM_NAME,	dtl.getArtifactId(),
				VERSION_ELEM_NAME,		dtl.getVersion()
			};
		for (int	vIndex=0; vIndex < vals.length; vIndex += 2)
		{
			if (asAttributes)
			{
				DOMUtils.addNonEmptyAttribute(root, vals[vIndex], vals[vIndex+1]);
			}
			else
			{
				final Element	elem=DOMUtils.createElementValue(doc, vals[vIndex], vals[vIndex+1]);
				if (null == elem)
					continue;	// should not happen
				root.appendChild(elem);
			}
		}

		return root;
	}
	/*
	 * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		return toXml(this, doc, false);
	}
}
