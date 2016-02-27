/**
 * 
 */
package net.community.chest.apache.maven.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2008 8:53:00 AM
 */
public class DependenciesList extends ArrayList<BuildDependencyDetails> implements XmlConvertible<DependenciesList> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5979647408840704544L;
	public DependenciesList ()
	{
		super();
	}

	public DependenciesList (int initialCapacity)
	{
		super(initialCapacity);
	}

	public DependenciesList (Collection<? extends BuildDependencyDetails> c)
	{
		super(c);
	}
	/**
	 * Looks for 1st entry whose group/artifact/version matches the specified one
	 * (case <U>insensitive</U>).
	 * @param l A {@link List} of {@link BaseTargetDetails} to be searched
	 * @param groupId Group ID
	 * @param artifactId Artifact name
	 * @param version Version name - if null/empty then version is ignored
	 * @return Index of 1st match - negative if not found
	 */
	public static final int indexOf (final List<? extends BaseTargetDetails> l, final String groupId, final String artifactId, final String version /* may be null/empty */)
	{
		final int	numItems=(null == l) ? 0 : l.size();
		if ((null == groupId) || (groupId.length() <= 0)
		 ||	(null == artifactId) || (artifactId.length() <= 0)
		 || (numItems <= 0))
			return (-1);

		final boolean	useVersion=(version != null) && (version.length() > 0);
		for (int	index=0; index < numItems; index++)
		{
			final BaseTargetDetails	d=l.get(index);
			final String			dg=(null == d) ? null : d.getGroupId(),
									da=(null == d) ? null : d.getArtifactId();
			if ((StringUtil.compareDataStrings(groupId, dg, false) != 0)
			 || (StringUtil.compareDataStrings(artifactId, da, false) != 0))
				continue;

			if (useVersion)
			{
				final String	dv=(null == d) ? null : d.getVersion();
				if (StringUtil.compareDataStrings(version, dv, false) != 0)
					continue;
			}

			return index;
		}

		return (-1);	// reached if no match found
	}
	/**
	 * Looks for 1st entry whose group/artifact/version matches the specified one
	 * (case <U>insensitive</U>).
	 * @param groupId Group ID
	 * @param artifactId Artifact name
	 * @param version Version name - if null/empty then version is ignored
	 * @return Index of 1st match - negative if not found
	 * @see #indexOf(List, String, String, String) for static helper method
	 */
	public int indexOf (final String groupId, final String artifactId, final String version /* may be null/empty */)
	{
		return indexOf(this, groupId, artifactId, version);
	}
	/**
	 * Looks for 1st entry whose group/artifact matches the specified one
	 * (case <U>insensitive</U>).
	 * @param l A {@link List} of {@link BaseTargetDetails} to be searched
	 * @param groupId Group ID
	 * @param artifactId Artifact name
	 * @return Index of 1st match - negative if not found
	 * @see #indexOf(List, String, String, String)  for specifying a version as well
	 */
	public static final int indexOf (final List<? extends BaseTargetDetails> l, final String groupId, final String artifactId)
	{
		return indexOf(l, groupId, artifactId, null);
	}
	/**
	 * Looks for 1st entry whose group/artifact matches the specified one
	 * (case <U>insensitive</U>).
	 * @param groupId Group ID
	 * @param artifactId Artifact name
	 * @return Index of 1st match - negative if not found
	 * @see #indexOf(String, String, String) for specifying a version as well
	 */
	public int indexOf (final String groupId, final String artifactId)
	{
		return indexOf(this, groupId, artifactId, null);
	}

	public BuildDependencyDetails addDependency (final Element elem) throws Exception
	{
		final BuildDependencyDetails	tgt=new BuildDependencyDetails(elem);
		add(tgt);
		return tgt;
	}

	public void handleUnknownDependencyElement (final Element elem, final String tagName) throws Exception
	{
		if ((null == elem) || (null == tagName) || (tagName.length() <= 0))	// just so compiler does not complain about unreferenced parameter
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getArgumentsExceptionLocation(getClass(), "handleUnknownDependencyElement", tagName) + " incomplete parameters");
	}

	public DependenciesList fromElementsList (final Collection<? extends Element> el) throws Exception
	{
		if ((el != null) && (el.size() > 0))
		{
			for (final Element	elem : el)
			{
				if (null == elem)	// should not happen
					continue;

				final String	tagName=elem.getTagName();
				if (BaseTargetDetails.DEPENDENCY_ELEM_NAME.equalsIgnoreCase(tagName))
					addDependency(elem);
				else
					handleUnknownDependencyElement(elem, tagName);
			}
		}

		return this;
	}

	public DependenciesList fromElementsList (final Element ...elements) throws Exception
	{
		if ((null == elements) || (elements.length <= 0))
			return this;
		else
			return fromElementsList(Arrays.asList(elements));
	}

	public DependenciesList (final Element ...elements) throws Exception
	{
		this(Math.max((null == elements) ? 0 : elements.length,5));
		
		if (fromElementsList(elements) != this)
			throw new IllegalStateException("Mismatched re-constructed instance");
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public DependenciesList fromXml (final Element root) throws Exception
	{
		return fromElementsList(DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE));
	}

	private static final Element[] extractRootNodes (final Element root)
	{
		final Collection<? extends Element>	el=
			DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
		final int							numElems=(null == el) ? 0 : el.size();
		return (numElems <= 0) ? null : el.toArray(new Element[numElems]);
	}

	public DependenciesList (final Element root) throws Exception
	{
		this(extractRootNodes(root));
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement toXml
		throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
	}
}
