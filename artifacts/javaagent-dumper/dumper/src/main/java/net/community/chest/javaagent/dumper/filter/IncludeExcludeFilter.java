/*
 * 
 */
package net.community.chest.javaagent.dumper.filter;

import java.util.Collection;

import net.community.chest.dom.DOMUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright as per GPLv2</P>
 * Uses an inclusion and exclusion filter where exclusion is "stronger" than inclusion
 * @author Lyor G.
 * @since Aug 11, 2011 3:33:14 PM
 */
public class IncludeExcludeFilter implements XmlConvertibleClassFilter {
	public IncludeExcludeFilter ()
	{
		this(null, null);
	}

	public IncludeExcludeFilter (ClassFilter inclFilter, ClassFilter exclFilter)
	{
		_inclFilter = inclFilter;
		_exclFilter = exclFilter;
	}
	
	public IncludeExcludeFilter (Element elem) throws Exception
	{
		final Object	filter=fromXml(elem);
		if (filter != this)
			throw new IllegalStateException("Mismatched reconstructed instances");
	}
	/*
	 * @see net.community.chest.javaagent.dumper.filter.ClassFilter#accept(java.lang.String)
	 */
	@Override
	public boolean accept (String className)
	{
		if (isExcluded(className))
			return false;
		if (!isIncluded(className))
			return false;	// debug breakpoint

		return true;
	}

	public boolean isIncluded (String className)
	{
		final ClassFilter	filter=getInclusionFilter();
		if (filter == null)
			return true;
		else
			return filter.accept(className);
	}

	public boolean isExcluded (String className)
	{
		final ClassFilter	filter=getExclusionFilter();
		if (filter == null)
			return false;
		else
			return filter.accept(className);
	}

	private ClassFilter	_inclFilter, _exclFilter;
	public ClassFilter getInclusionFilter ()
	{
		return _inclFilter;
	}

	public void setInclusionFilter (ClassFilter inclFilter)
	{
		_inclFilter = inclFilter;
	}

	public ClassFilter getExclusionFilter ()
	{
		return _exclFilter;
	}

	public void setExclusionFilter (ClassFilter exclFilter)
	{
		_exclFilter = exclFilter;
	}

	public static final String	FILTERS_ELEMENT="filters", INCLUDE_ELEMENT="include", EXCLUDE_ELEMENT="exclude";
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final Element	root=doc.createElement(FILTERS_ELEMENT);
		appendFilter(doc, root, INCLUDE_ELEMENT, getInclusionFilter());
		appendFilter(doc, root, EXCLUDE_ELEMENT, getExclusionFilter());
		return root;
	}

	protected Element appendFilter (Document doc, Element root, String tagName, ClassFilter filter) throws Exception
	{
		final Element	filterElem=(filter instanceof XmlConvertibleClassFilter)
				? ((XmlConvertibleClassFilter) filter).toXml(doc) : null;
		if (filterElem == null)
			return null;

		final Element	subRoot=doc.createElement(tagName);
		subRoot.appendChild(filterElem);
		root.appendChild(subRoot);
		return subRoot;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public IncludeExcludeFilter fromXml (Element root) throws Exception
	{
		final Collection<? extends Element>	elems=DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
		if ((elems == null) || elems.isEmpty())
			return this;

		for (final Element elem : elems)
		{
			final String	tagName=elem.getTagName();
			if (INCLUDE_ELEMENT.equals(tagName))
				setInclusionFilter(elem);
			else if (EXCLUDE_ELEMENT.endsWith(tagName))
				setExclusionFilter(elem);
			else
				throw new DOMException(DOMException.NAMESPACE_ERR, "Unknown element: " + DOMUtils.toString(elem));
		}

		return this;
	}
	
	protected XmlConvertibleClassFilter setInclusionFilter (Element elem) throws Exception
	{
		final XmlConvertibleClassFilter	filter=new MultiFilter(elem);
		setInclusionFilter(filter);
		return filter;
	}

	protected XmlConvertibleClassFilter setExclusionFilter (Element elem) throws Exception
	{
		final XmlConvertibleClassFilter	filter=new MultiFilter(elem);
		setExclusionFilter(filter);
		return filter;
	}
}
