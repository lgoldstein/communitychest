/*
 * 
 */
package net.community.chest.apache.log4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;
import net.community.chest.util.map.MapEntryImpl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Represents a log4j {@link org.apache.log4j.Category} configuration entry</P>
 * 
 * @author Lyor G.
 * @since Mar 31, 2009 8:30:38 AM
 */
public class CategoryConfig extends MapEntryImpl<String,Level>
		implements Serializable, XmlConvertible<CategoryConfig>, PubliclyCloneable<CategoryConfig> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3262079933132516998L;
	public CategoryConfig (String name, Level value)
	{
		super(name, value);
	}

	public CategoryConfig ()
	{
		this(null, null);
	}

	public String getName ()
	{
		return getKey();
	}

	public void setName (String n)
	{
		setKey(n);
	}
	// returns same element as the input
	public Element addName (Element elem)
	{
		return DOMUtils.addNonEmptyAttribute(elem, NAME_ATTR, getName());
	}

	public static final String	NAME_ATTR="name";
	// returns name value (if any)
	public String setName (Element elem)
	{
		final String	n=(null == elem) ? null : elem.getAttribute(NAME_ATTR);
		if ((n != null) && (n.length() > 0))
			setName(n);
		return n;
	}

	public Level getLevel ()
	{
		return getValue();
	}

	public void setLevel (Level l)
	{
		setValue(l);
	}

	public static final String	VALUE_ATTR="value";
	// returns Level value (if any)
	public Level setLevel (Element elem)
	{
		final String	v=(null == elem) ? null : elem.getAttribute(VALUE_ATTR);
		final Level		l=((null == v) || (v.length() <= 0)) ? null : ExtendedLevel.toExtendedLevel(v, null);
		if (l != null)
			setLevel(l);
		else if ((v != null) && (v.length() > 0))
			throw new NoSuchElementException("setLevel(" + DOMUtils.toString(elem) + ") unknown value: " + v);
		return l;
	}
	/**
	 * Default XML {@link Element} name of the <U>child</U> priority configuration
	 */
	public static final String	PRIORITY_ELEM_NAME="priority";
	public String getPriorityElementName ()
	{
		return PRIORITY_ELEM_NAME;
	}
	// returns priority element (if any)
	public Element addLevel (Document doc, Element root)
	{
		final Level		l=getLevel();
		final String	v=(null == l) ? null : l.toString();
		if ((null == v) || (v.length() <= 0))
			return null;

		final String	n=getPriorityElementName();
		final Element	elem=DOMUtils.addNonEmptyAttribute(doc.createElement(n), VALUE_ATTR, v);
		root.appendChild(elem);
		return elem;
	}

	protected void handleUnknownChildElement (final String tagName, final Element elem)
	{
		throw new UnsupportedOperationException("handleUnknownChildElement(" + tagName + ") " + DOMUtils.toString(elem));
	}

	public Level setLevel (final Collection<? extends Element>	el)
	{
		if ((null == el) || (el.size() <= 0))
			return null;

		final String	peName=getPriorityElementName();
		Level			l=null;
		for (final Element pe : el)
		{
			final String	tagName=(null == pe) ? null : pe.getTagName();
			if (0 == StringUtil.compareDataStrings(tagName, peName, false))
			{
				if (l != null)
					throw new IllegalStateException("setLevel(" + DOMUtils.toString(pe) + ") level re-specified: " + l);

				if (null == (l=setLevel(pe)))
					throw new IllegalStateException("setLevel(" + DOMUtils.toString(pe) + ") no level set");
			}
			else
				handleUnknownChildElement(tagName, pe);
		}

		return l;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public CategoryConfig fromXml (Element elem) throws Exception
	{
		setName(elem);
		setLevel(DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE));
		return this;
	}

	public CategoryConfig (Element elem) throws Exception
	{
		final Object	o=fromXml(elem);
		if (o != this)
			throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");
	}
	/**
	 * Default XML {@link Element} tag name for the {@link #toXml(Document)}
	 * implementation
	 */
	public static final String	CATEGORY_ELEM_NAME="category";
	public String getRootElementName ()
	{
		return CATEGORY_ELEM_NAME;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final String	rootName=getRootElementName();
		final Element	root=doc.createElement(rootName);
		addName(root);
		addLevel(doc, root);

		return root;
	}
	/*
	 * @see net.community.chest.util.map.MapEntryImpl#clone()
	 */
	@Override
	@CoVariantReturn
	public CategoryConfig clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}

	public static final List<CategoryConfig> readCategories (final Collection<? extends Element> el)
	{
		final int					numElems=(null == el) ? 0 : el.size();
		final List<CategoryConfig>	cl=(numElems <= 0) ? null : new ArrayList<CategoryConfig>(numElems);
		if (numElems > 0)
		{
			for (final Element elem : el)
			{
				if (null == elem)
					continue;

				try
				{
					cl.add(new CategoryConfig(elem));
				}
				catch(Exception e)
				{
					throw ExceptionUtil.toRuntimeException(e);
				}
	
			}
		}

		return cl;
	}

	public static final List<CategoryConfig> readCategories (final Element root)
	{
		return (null == root) ? null : readCategories(DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE));
	}

	public static final List<CategoryConfig> readCategories (final Document doc)
	{
		return (null == doc) ? null : readCategories(doc.getDocumentElement());
	}

	public static final CategoryConfig findBestMatch (final String loggerName, final Collection<? extends CategoryConfig> cl)
	{
		final int	nLen=(null == loggerName) ? 0 : loggerName.length();
		if ((nLen <= 0) || (null == cl) || (cl.size() <= 0))
			return null;

		CategoryConfig	ret=null;
		int				rLen=0;
		for (final CategoryConfig cc : cl)
		{
			final String	cn=(null == cc) ? null : cc.getName();
			final int		cLen=(null == cn) ? 0 : cn.length();
			if ((cLen <= 0) || (cLen > nLen))
				continue;	// the category name must be a prefix/exact match of logger name

			if (!StringUtil.startsWith(loggerName, cn, false, false))
				continue;

			if ((cLen < nLen) && (loggerName.charAt(cLen) != '.'))
				continue;	// make sure proper category prefix

			if (cLen < rLen)
				continue;	// if have a longer match then use it

			ret = cc;
			rLen = cLen;
		}

		return ret;
	}

	public static final CategoryConfig findBestMatch (final Logger l, final Collection<? extends CategoryConfig> cl)
	{
		return (null == l) ? null : findBestMatch(l.getName(), cl);
	}

	public static final Collection<Map.Entry<String,CategoryConfig>> findBestMatches (final Collection<? extends CategoryConfig> cl, final Collection<String> nl)
	{
		final int	numNames=(null == nl) ? 0 : nl.size(),
					numCategories=(null == cl) ? 0 : cl.size();
		if ((numNames <= 0) || (numCategories <= 0))
			return null;

		Collection<Map.Entry<String,CategoryConfig>>	ret=null;
		for (final String n : nl)
		{
			final CategoryConfig	cc=findBestMatch(n, cl);
			if (null == cc)
				continue;

			if (null == ret)
				ret = new LinkedList<Map.Entry<String,CategoryConfig>>();
			ret.add(new MapEntryImpl<String,CategoryConfig>(n, cc));
		}

		return ret;
	}

	public static final Collection<Map.Entry<String,CategoryConfig>> readBestMatches (final Collection<? extends Element> el, final Collection<String> nl)
	{
		return ((null == nl) || (nl.size() <= 0)) ? null : findBestMatches(readCategories(el), nl);
	}

	public static final Collection<Map.Entry<String,CategoryConfig>> readBestMatches (final Element root, final Collection<String> nl)
	{
		return ((null == root) || (null == nl) || (nl.size() <= 0)) ? null : readBestMatches(DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE), nl);
	}

	public static final Collection<Map.Entry<String,CategoryConfig>> readBestMatches (final Document doc, final Collection<String> nl)
	{
		return ((null == doc) || (null == nl) || (nl.size() <= 0)) ? null : readBestMatches(doc.getDocumentElement(), nl);
	}

	public static final <L extends Logger> Collection<Map.Entry<L,CategoryConfig>> findLoggersMatches (final Collection<? extends CategoryConfig> cl, final Collection<? extends L> ll)
	{
		final int	numLoggers=(null == ll) ? 0 : ll.size(),
					numCategories=(null == cl) ? 0 : cl.size();
		if ((numLoggers <= 0) || (numCategories <= 0))
			return null;

		Collection<Map.Entry<L,CategoryConfig>>	ret=null;
		for (final L l : ll)
		{
			final CategoryConfig	cc=findBestMatch(l, cl);
			if (null == cc)
				continue;

			if (null == ret)
				ret = new LinkedList<Map.Entry<L,CategoryConfig>>();
			ret.add(new MapEntryImpl<L,CategoryConfig>(l, cc));
		}

		return ret;
	}

	public static final <L extends Logger> Collection<Map.Entry<L,CategoryConfig>> readLoggersMatches (final Collection<? extends Element> el, final Collection<? extends L> ll)
	{
		return ((null == ll) || (ll.size() <= 0)) ? null : findLoggersMatches(readCategories(el), ll);
	}

	public static final <L extends Logger> Collection<Map.Entry<L,CategoryConfig>> readLoggersMatches (final Element root, final Collection<? extends L> ll)
	{
		return ((null == root) || (null == ll) || (ll.size() <= 0)) ? null : readLoggersMatches(DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE), ll);
	}

	public static final <L extends Logger> Collection<Map.Entry<L,CategoryConfig>> readLoggersMatches (final Document doc, final Collection<? extends L> ll)
	{
		return ((null == doc) || (null == ll) || (ll.size() <= 0)) ? null : readLoggersMatches(doc.getDocumentElement(), ll);
	}

	public static final void applySettings (final Collection<? extends Map.Entry<? extends Logger,? extends CategoryConfig>> pl)
	{
		if ((null == pl) || (pl.size() <= 0))
			return;

		for (final Map.Entry<? extends Logger,? extends CategoryConfig> pe : pl)
		{
			final Logger			l=(null == pe) ? null : pe.getKey();
			final CategoryConfig	cc=(null == pe) ? null : pe.getValue();
			final Level				ll=(null == cc) ? null : cc.getLevel();
			if ((null == l) || (null == ll))
				continue;

			l.setLevel(ll);
		}
	}
	// returns list of Logger(s) whose settings have been changed
	public static final List<Logger> applyCategorySettings (final Collection<? extends CategoryConfig> cl)
	{
		final int	numConfigs=(null == cl) ? 0 : cl.size();
		if (numConfigs <= 0)
			return null;

		List<Logger>	ll=null;
		for (final CategoryConfig cc : cl)
		{
			final String	n=(null == cc) ? null : cc.getName();
			final Logger	l=((null == n) || (n.length() <= 0)) ? null : Logger.getLogger(n);
			final Level		v=(null == cc) ? null : cc.getLevel();
			if ((null == l) || (null == v))
				continue;

			final Level	o=l.getEffectiveLevel();
			if (AbstractComparator.compareObjects(l, o))
				continue;	// ignore if same effective level

			l.setLevel(v);
			if (null == ll)
				ll = new ArrayList<Logger>(numConfigs);
			ll.add(l);
		}

		return ll;
	}

	public static final List<Logger> applyElementSettings (final Collection<? extends Element> el)
	{
		return ((null == el) || (el.size() <= 0)) ? null : applyCategorySettings(readCategories(el));
	}

	public static final List<Logger> applyElementSettings (final Element elem)
	{
		return (null == elem) ? null : applyElementSettings(DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE));
	}

	public static final List<Logger> applyDocumentSettings (final Document doc)
	{
		return (null == doc) ? null : applyElementSettings(doc.getDocumentElement());
	}
}