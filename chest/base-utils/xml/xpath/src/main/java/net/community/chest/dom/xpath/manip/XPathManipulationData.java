/*
 * 
 */
package net.community.chest.dom.xpath.manip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.NodeTypeEnum;
import net.community.chest.dom.xpath.XPathInsertLocation;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 7, 2009 8:10:03 AM
 */
public class XPathManipulationData implements PubliclyCloneable<XPathManipulationData> {
	public XPathManipulationData ()
	{
		super();
	}

	private XPathOperation	_op;
	public XPathOperation getOp ()
	{
		return _op;
	}

	public void setOp (XPathOperation op)
	{
		_op = op;
	}
	// used only for INSERT(ion)
	private XPathInsertLocation	_position;
	public XPathInsertLocation getPosition ()
	{
		return _position;
	}

	public void setPosition (XPathInsertLocation position)
	{
		_position = position;
	}
	/**
	 * The {@link Element} under which the manipulation data resides
	 */
	private Element	_dataElement;
	public Element getDataElement ()
	{
		return _dataElement;
	}

	public void setDataElement (Element dataElement)
	{
		_dataElement = dataElement;
	}
	/**
	 * The XPath expression used to select the nodes which this operation manipulates 
	 */
	private String	_path;
	public String getPath ()
	{
		return _path;
	}

	public void setPath (String path)
	{
		_path = path;
	}
	/**
	 * TRUE (default) fail if no matches found for the {@link #getPath()} expression
	 */
	private boolean	_failOnNoMatch=true;
	public boolean isFailOnNoMatch ()
	{
		return _failOnNoMatch;
	}

	public void setFailOnNoMatch (boolean failOnNoMatch)
	{
		_failOnNoMatch = failOnNoMatch;
	}
	/**
	 * TRUE fail if more than one matches found for the {@link #getPath()} expression
	 * (default=FALSE)
	 */
	private boolean	_failOnMultiMatch	/* =false */;
	public boolean isFailOnMultiMatch ()
	{
		return _failOnMultiMatch;
	}

	public void setFailOnMultiMatch (boolean failOnMultiMatch)
	{
		_failOnMultiMatch = failOnMultiMatch;
	}
	/**
	 * Type of node being manipulated
	 */
	private NodeTypeEnum	_nodeType;
	public NodeTypeEnum getNodeType ()
	{
		return _nodeType;
	}

	public void setNodeType (NodeTypeEnum nodeType)
	{
		_nodeType = nodeType;
	}

	private Collection<XPathManipulationListener>	_listeners;
	public Collection<XPathManipulationListener> getListeners ()
	{
		return _listeners;
	}

	public void setListeners (Collection<XPathManipulationListener> ll)
	{
		_listeners = ll;
	}

	public boolean addManipulationListener (XPathManipulationListener l)
	{
		if (null == l)
			return false;

		if (null == _listeners)
		{
			_listeners = SetsUtils.uniqueSetOf(l);
			return true;
		}

		return _listeners.add(l);
	}

	public boolean removeManipulationListener (XPathManipulationListener l)
	{
		final Collection<? extends XPathManipulationListener>	ll=getListeners(); 
		if ((null == l) || (null == ll) || (ll.size() <= 0))
			return false;

		return ll.remove(l);
	}

	public int fireManipulationEvent (final Document doc, final Element elem, final Object result)
	{
		final Collection<? extends XPathManipulationListener>	ll=getListeners();
		final int												numListeners=
			(null == ll) ? 0 : ll.size();
		if (numListeners <= 0)
			return 0;

		for (final XPathManipulationListener l : ll)
			l.handleManipulationExecutionResult(this, doc, elem, result);
		return numListeners;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public XPathManipulationData clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return DOMUtils.toString(getDataElement());
	}

	public Document execute (final Document doc, final Element elem) throws XPathExpressionException
	{
		final XPathOperation	op=getOp();
		if (null == op)
			throw new XPathExpressionException("execute(" + this + ")[" + DOMUtils.toString(elem) + "] no operation");

		return op.execute(this, doc, elem);
	}

	public Document execute (final XPath p, final Document org) throws XPathExpressionException
	{
		final String	xpExpr=getPath();
		if ((null == xpExpr) || (xpExpr.length() <= 0))
			throw new XPathExpressionException("execute(" + this + ") no path expression");
		
		final Object	res=p.evaluate(xpExpr, org, XPathConstants.NODESET);
		if (!(res instanceof NodeList))
			throw new XPathExpressionException("execute(" + this + ")[" + xpExpr + "] unexpected result type: " + ((null == res) ? null : res.getClass().getName()));

		final NodeList	nl=(NodeList) res;
		final int		numNodes=nl.getLength();
		if (numNodes <= 0)
		{
			if (isFailOnNoMatch())
				throw new XPathExpressionException("execute(" + this + ")[" + xpExpr + "] no matches found");
			return org;
		}
		else if (numNodes > 1)
		{
			if (isFailOnMultiMatch())
				throw new XPathExpressionException("execute(" + this + ")[" + xpExpr + "] multiple matches (" + numNodes + ") found");
		}

		Document	doc=org;
		for (int nIndex=0; nIndex < numNodes; nIndex++)
		{
			final Node	n=nl.item(nIndex);
			// we expect only element(s)
			if ((null == n) || (n.getNodeType() != Node.ELEMENT_NODE))
			{
				final NodeTypeEnum	t=NodeTypeEnum.fromNode(n);
				final String		ts;
				if (null == t)
					ts = (null == n) ? null : String.valueOf(n.getNodeType());
				else
					ts = t.toString();
				throw new XPathExpressionException("execute(" + this + ")[" + xpExpr + "] unexpected node type: " + ts);
			}

			doc = execute(doc, (Element) n);
		}

		return doc;
	}

	public Document execute (final XPathFactory	fac, final Document doc) throws XPathExpressionException
	{
		return execute((null == fac) ? null : fac.newXPath(), doc);
	}

	public Document execute (final Document doc) throws XPathExpressionException
	{
		return execute(XPathFactory.newInstance(), doc);
	}

	public static final Document execute (
			final Document org, final XPath p, final Collection<? extends XPathManipulationData> ml) throws XPathExpressionException
	{
		if ((null == org) || (null == ml) || (ml.size() <= 0))
			return org;

		Document	doc=org;
		for (final XPathManipulationData m : ml)
			doc = (null == m) ? doc : m.execute(p, org);
		return doc;
	}

	public static final Document execute (
			final Document org, final XPath p, final XPathManipulationData ... ml) throws XPathExpressionException
	{
		if ((null == org) || (null == ml) || (ml.length <= 0))
			return org;

		return execute(org, p, Arrays.asList(ml));
	}

	public static final Document execute (
			final Document org, final XPathFactory	fac, final Collection<? extends XPathManipulationData> ml) throws XPathExpressionException
	{
		if ((null == org) || (null == ml) || (ml.size() <= 0))
			return org;

		return execute(org, (null == fac) ? null : fac.newXPath(), ml);
	}

	public static final Document execute (
			final Document org, final XPathFactory	fac, final XPathManipulationData ... ml) throws XPathExpressionException
	{
		if ((null == org) || (null == ml) || (ml.length <= 0))
			return org;

		return execute(org, fac, Arrays.asList(ml));
	}

	public static final Document execute (
			final Document org, final Collection<? extends XPathManipulationData> ml) throws XPathExpressionException
	{
		if ((null == org) || (null == ml) || (ml.size() <= 0))
			return org;

		return execute(org, XPathFactory.newInstance(), ml);
	}

	public static final Document execute (
			final Document org, final XPathManipulationData ... ml) throws XPathExpressionException
	{
		if ((null == org) || (null == ml) || (ml.length <= 0))
			return org;

		return execute(org, Arrays.asList(ml));
	}

	// listener may be null
	public static final List<XPathManipulationData> loadOperations (
			final Collection<? extends Element> el, final XPathManipulationListener l) throws Exception
	{
		final int	numOps=(null == el) ? 0 : el.size();
		if (numOps <= 0)
			return null;

		final List<XPathManipulationData>	ol=new ArrayList<XPathManipulationData>(numOps);
		for (final Element elem : el)
		{
			final XPathManipulationData	d=
				(null == elem) ? null : XPathManipulationDataReflectiveProxy.DEFAULT.fromXml(elem);
			if (null == d)
				continue;

			d.addManipulationListener(l);
			ol.add(d);
		}

		return ol;
	}
	// listener may be null
	public static final List<XPathManipulationData> loadOperations (
			final Document doc, final XPathManipulationListener l) throws Exception
	{
		final Element						elem=(null == doc) ? null : doc.getDocumentElement();
		final Collection<? extends Element> el=DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE);
		return loadOperations(el, l);
	}
}
