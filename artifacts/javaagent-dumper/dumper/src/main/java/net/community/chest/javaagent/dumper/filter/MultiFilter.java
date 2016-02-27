/*
 * 
 */
package net.community.chest.javaagent.dumper.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright as per GPLv2</P>
 * Returns TRUE if any {@link ClassFilter} returns TRUE. <B>Note:</B> if no
 * filters are set then {@link #accept(String)} result is FALSE.
 * @author Lyor G.
 * @since Aug 11, 2011 3:20:31 PM
 */
public class MultiFilter implements XmlConvertibleClassFilter {
	public MultiFilter ()
	{
		this((ClassFilter[]) null);
	}

	public MultiFilter (ClassFilter ... filters)
	{
		this(((filters == null) || (filters.length <= 0)) ? (Collection<? extends ClassFilter>) null : Arrays.asList(filters));
	}

	public MultiFilter (Collection<? extends ClassFilter> filters)
	{
		_filters = filters;
	}

	public MultiFilter (Element root) throws Exception
	{
		final MultiFilter	filter=fromXml(root);
		if (this != filter)
			throw new IllegalStateException("Mismatched re-constructed instances");
	}

	private Collection<? extends ClassFilter>	_filters;
	public Collection<? extends ClassFilter> getFilters ()
	{
		return _filters;
	}

	public void setFilters (Collection<? extends ClassFilter> filters)
	{
		_filters = filters;
	}
	/*
	 * @see net.community.chest.javaagent.dumper.filter.ClassFilter#accept(java.lang.String)
	 */
	@Override
	public boolean accept (String className)
	{
		final Collection<? extends ClassFilter>	filters=getFilters();
		if ((filters == null) || filters.isEmpty())
			return false;	// no sub-filters means no acceptance 

		for (final ClassFilter f : filters)
		{
			if (f.accept(className))
				return true;
		}

		return false;	// none accepted
	}

	public static final String	PATTERNS_ELEMENT="patterns";
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final Element							root=doc.createElement(PATTERNS_ELEMENT);
		final Collection<? extends ClassFilter>	filters=getFilters();
		if ((filters == null) || filters.isEmpty())
			return root;

		for (final ClassFilter f : filters)
		{
			if (f instanceof XmlConvertibleClassFilter)
				root.appendChild(((XmlConvertibleClassFilter) f).toXml(doc));
		}

		return root;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public MultiFilter fromXml (Element root) throws Exception
	{
		setFilters(root);
		return this;
	}

	protected List<ClassFilter> setFilters (Element root) throws Exception
	{
		return setFilters(root.getElementsByTagName(PatternClassFilter.PATTERN_ELEMENT));
	}

	protected List<ClassFilter> setFilters (NodeList list) throws Exception
	{
		final int	numNodes=(list == null) ? 0 : list.getLength();
		if (numNodes <= 0)
			return Collections.emptyList();

		final List<ClassFilter>	filters=new ArrayList<ClassFilter>(numNodes);
		for (int	nIndex=0; nIndex < numNodes; nIndex++)
			filters.add(new PatternClassFilter((Element) list.item(nIndex)));

		setFilters(filters);
		return filters;
	}
}
