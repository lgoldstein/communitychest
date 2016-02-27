/*
 * 
 */
package net.community.chest.dom.xpath.manip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.NodeTypeEnum;
import net.community.chest.dom.xpath.XPathUtils;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Represents the current available XPATH based XML manipulation engine operations</P>
 * @author Lyor G.
 * @since May 7, 2009 7:55:15 AM
 */
public enum XPathOperation {
	/**
	 * Inserts a <U>new</U> node - if the node already exists it may cause
	 * an error in some cases (e.g. for attributes). The error may be thrown
	 * by the engine - depending on other settings)
	 */
	INSERT {
			/*
			 * @see net.community.chest.dom.xpath.manip.XPathOperation#execute(net.community.chest.dom.xpath.manip.XPathManipulationData, org.w3c.dom.Document, org.w3c.dom.Element)
			 */
			@Override
			public Document execute (final XPathManipulationData manip, final Document doc, final Element elem) throws XPathExpressionException
			{
				final NodeTypeEnum	t=(null == manip) ? null : manip.getNodeType();
				if (null == t)
					throw new XPathExpressionException("execute(" + manip + "][" + DOMUtils.toString(elem) + "] no node type specified");
				
				switch(t)
				{
					case ELEMENT	:
						{
							final Object res=XPathUtils.insertData(doc, elem, manip.getDataElement(), manip.getPosition());
							if (res != null)
								manip.fireManipulationEvent(doc, elem, res);
						}
						break;

					case ATTRIBUTE	:
						{
							final Collection<? extends Map.Entry<String,String>>	al=
								getAttributesList(manip);
							final Collection<? extends Attr>						vl=
								XPathUtils.insertAttributes(doc, elem, al);
							final int												numAttrs=
								(null == vl) ? 0 : vl.size();
							if (numAttrs > 0)
								manip.fireManipulationEvent(doc, elem, vl);
						}
						break;

					default			:
						throw new XPathExpressionException("execute(" + manip + "][" + DOMUtils.toString(elem) + "] unsupported node type: " + t);
				}

				return doc;
			}
		},
	/**
	 * Removes an <U>existing</U> node - if the node does not exist then it
	 * is an error (which may be thrown - depending on other settings)
	 */
	REMOVE {
		/*
		 * @see net.community.chest.dom.xpath.manip.XPathOperation#execute(net.community.chest.dom.xpath.manip.XPathManipulationData, org.w3c.dom.Document, org.w3c.dom.Element)
		 */
		@Override
		public Document execute (final XPathManipulationData manip, final Document doc, final Element elem) throws XPathExpressionException
		{
			final NodeTypeEnum	t=(null == manip) ? null : manip.getNodeType();
			if (null == t)
				throw new XPathExpressionException("execute(" + manip + "][" + DOMUtils.toString(elem) + "] no node type specified");

			switch(t)
			{
				case ELEMENT	:
					{
						final Node	res=XPathUtils.removeElement(doc, elem);
						if (res != null)
							manip.fireManipulationEvent(doc, elem, res);
					}
					break;

				case ATTRIBUTE	:
					{
						final Collection<? extends Map.Entry<String,?>>	al=
							getAttributesList(manip);
						final int										numAttrs=
							(null == al) ? 0 : al.size();
						if (numAttrs <= 0)
							return doc;

						final Collection<String>	nl=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
						for (final Map.Entry<String,?> ae : al)
						{
							final String	n=(null == ae) ? null : ae.getKey();
							if ((null == n) || (n.length() <= 0))
								continue;
							nl.add(n);
						}

						final Collection<? extends Attr>	res=
							XPathUtils.removeAttributes(elem, null, nl);
						if ((res != null) && (res.size() > 0))
							manip.fireManipulationEvent(doc, elem, res);
					}
					break;

				default			:
					throw new XPathExpressionException("execute(" + manip + "][" + DOMUtils.toString(elem) + "] unsupported node type: " + t);
			}

			return doc;
		}
	},
	/**
	 * Updates an <U>existing</U> node - if the node does not exist then it
	 * is an error (which may be thrown - depending on other settings)
	 */
	UPDATE {
			/*
			 * @see net.community.chest.dom.xpath.manip.XPathOperation#execute(net.community.chest.dom.xpath.manip.XPathManipulationData, org.w3c.dom.Document, org.w3c.dom.Element)
			 */
			@Override
			public Document execute (final XPathManipulationData manip, final Document doc, final Element elem) throws XPathExpressionException
			{
				final NodeTypeEnum	t=(null == manip) ? null : manip.getNodeType();
				if (null == t)
					throw new XPathExpressionException("execute(" + manip + "][" + DOMUtils.toString(elem) + "] no node type specified");
				
				switch(t)
				{
					case ELEMENT	:
						{
//TODO						final Object res=XPathUtils.updateData(doc, elem, manip.getDataElement(), manip.getInsertLocation());
//TODO						manip.fireManipulationEvent(doc, elem, res);
						}
						break;
	
					case ATTRIBUTE	:
						{
							final Collection<? extends Map.Entry<String,String>>	al=
								getAttributesList(manip);
							final int												numAttrs=
								(null == al) ? 0 : al.size();
							if (numAttrs <= 0)
								return doc;
							final Collection<? extends Attr>	vl=
								DOMUtils.getNodeAttributesList(elem);
							if ((null == vl) || (vl.size() <= 0))
								return doc;

							for (final Attr a : vl)
							{
								final String				aName=
									(null == a) ? null : a.getName();
								final Map.Entry<?,String>	mp=
									findMatchByName(aName, al);
								if (null == mp)
									continue;

								a.setValue(mp.getValue());
								manip.fireManipulationEvent(doc, elem, a);
							}
						}
						break;
	
					default			:
						throw new XPathExpressionException("execute(" + manip + "][" + DOMUtils.toString(elem) + "] unsupported node type: " + t);
				}
	
				return doc;
			}
		},
	/**
	 * Change the node name (tag(s) for element(s) and name(s) for attributes)
	 * - if the node does not exist then it is an error (which may be thrown
	 * - depending on other settings)
	 */
	RENAME {
			/*
			 * @see net.community.chest.dom.xpath.manip.XPathOperation#execute(net.community.chest.dom.xpath.manip.XPathManipulationData, org.w3c.dom.Document, org.w3c.dom.Element)
			 */
			@Override
			public Document execute (final XPathManipulationData manip, final Document doc, final Element elem) throws XPathExpressionException
			{
				final NodeTypeEnum	t=(null == manip) ? null : manip.getNodeType();
				if (null == t)
					throw new XPathExpressionException("execute(" + manip + "][" + DOMUtils.toString(elem) + "] no node type specified");

				// each pair is an "old/new" where old can be a regex(p)
				final Collection<? extends Map.Entry<String,String>>	al=
					getAttributesList(manip);
				final int										numAttrs=
					(null == al) ? 0 : al.size();
				if (numAttrs <= 0)
					return doc;

				switch(t)
				{
					case ELEMENT	:
						{
							final Map.Entry<?,String>	mp=findMatchByName(elem.getTagName(), al);
							if (mp != null)
							{
								final Node	n=doc.renameNode(elem, null, mp.getValue());
								if (n != null)
									manip.fireManipulationEvent(doc, elem, n);
							}
						}
						break;

					case ATTRIBUTE	:
						{
							final Collection<? extends Attr>	vl=
								DOMUtils.getNodeAttributesList(elem);
							if ((vl != null) && (vl.size() > 0))
							{
								for (final Attr a : vl)
								{
									final String					aName=
										(null == a) ? null : a.getName();
									final Map.Entry<?,String>	mp=
										findMatchByName(aName, al);
									if (null == mp)
										continue;

									final Node n=doc.renameNode(a, null, mp.getValue());
									if (n != null)
										manip.fireManipulationEvent(doc, elem, n);
								}
							}
						}
						break;

					default			:
						throw new XPathExpressionException("execute(" + manip + "][" + DOMUtils.toString(elem) + "] unsupported node type: " + t);
				}

				return doc;
			}
		};

	public Document execute (final XPathManipulationData manip, final Document doc, final Element elem) throws XPathExpressionException
	{
		if (doc != null)
			throw new XPathExpressionException("execute(" + manip + "][" + DOMUtils.toString(elem) + "] N/A");
		return doc;
	}

	public static final List<XPathOperation>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final XPathOperation fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final String	PROP_NAME_ATTR="name", PROP_VALUE_ATTR="value";
	public static final List<Map.Entry<String,String>> getAttributesList (final Collection<? extends Element> el)
	{
		final int	numAttrs=(null == el) ? 0 : el.size();
		if (numAttrs <= 0)
			return null;

		List<Map.Entry<String,String>>	al=null;
		for (final Element elem : el)
		{
			final String	n=(null == elem) ? null : elem.getAttribute(PROP_NAME_ATTR),
							v=(null == elem) ? null : elem.getAttribute(PROP_VALUE_ATTR);
			if ((null == n) || (n.length() <= 0))
				continue;

			if (null == al)
				al = new ArrayList<Map.Entry<String,String>>(numAttrs);
			al.add(new MapEntryImpl<String,String>(n,v));
		}

		return al;
	}

	public static final List<Map.Entry<String,String>> getAttributesList (final XPathManipulationData manip)
	{
		final Element						elem=(null == manip) ? null : manip.getDataElement();
		final Collection<? extends Element> el=DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE);
		return getAttributesList(el);
	}
	
	public static final <V> Map.Entry<String,V> findMatchByName (final String nn, final Collection<? extends Map.Entry<String,V>>	al)
	{
		if ((null == nn) || (nn.length() <= 0)
		 || (null == al) || (al.size() <= 0))
			return null;

		for (final Map.Entry<String,V> ae : al)
		{
			final String	pv=(null == ae) ? null : ae.getKey();
			if ((null == pv) || (pv.length() <= 0))
				continue;
			// we check equalsIgnoreCase 1st since it is most likely
			if (nn.equalsIgnoreCase(pv)
			 || "*".equals(pv)
			 || nn.matches(pv))
				return ae;
		}

		return null;
	}
}
