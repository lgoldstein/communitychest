/*
 * 
 */
package net.community.chest.dom.xpath.manip;

import java.util.Collection;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.AbstractTransformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 7, 2009 2:27:58 PM
 */
public class XPathManipulationTransformer extends AbstractTransformer {
	/**
	 * Default file suffix used to hold XPATH manipulation instructions
	 */
	public static final String	XPT_MAINP_FILE_SUFFIX=".xpt";

	private Collection<? extends XPathManipulationData>	_ml;
	public Collection<? extends XPathManipulationData> getManipulators ()
	{
		return _ml;
	}

	public void setManipulators (Collection<? extends XPathManipulationData> ml)
	{
		_ml = ml;
	}

	public XPathManipulationTransformer (Collection<? extends XPathManipulationData> ml)
	{
		_ml = ml;
	}

	public XPathManipulationTransformer (final Document doc, final XPathManipulationListener l) throws Exception
	{
		this(XPathManipulationData.loadOperations(doc, l));
	}

	public XPathManipulationTransformer ()
	{
		this(null);
	}

	private XPathFactory	_fac;
	protected XPathFactory getFactory (final boolean createIfNotExist)
	{
		if ((null == _fac) && createIfNotExist)
			_fac = XPathFactory.newInstance();
		return _fac;
	}

	public XPathFactory getFactory ()
	{
		return getFactory(false);
	}

	public void setFactory (XPathFactory fac)
	{
		_fac = fac;
	}

	private XPath	_xp;
	protected XPath getXPath (final boolean createIfNotExist)
	{
		if ((null == _xp) && createIfNotExist)
		{
			final XPathFactory	fac=getFactory(createIfNotExist);
			_xp = fac.newXPath();
		}

		return _xp;
	}

	public XPath getXPath ()
	{
		return getXPath(false);
	}

	public void setXPath (XPath xp)
	{
		_xp = xp;
	}

	public void transform (Document org, Document out) throws TransformerException
	{
		if ((null == org) || (null == out))
			throw new TransformerException("Missing input/output document(s)");

		// create copies and adopt the top-level children of the original document so as not to change it
		final NodeList	nl=org.getChildNodes();
		final int		numNodes=(null == nl) ? 0 : nl.getLength();
		for (int	nIndex=0; nIndex < numNodes; nIndex++)
		{
			final Node	n=nl.item(nIndex),
						cn=(null == n) ? null : n.cloneNode(true),
						an=(null == cn) ? null : out.adoptNode(cn);
			if (null == an)
				throw new TransformerException("Failed to adopt node=" + ((n instanceof Element) ? DOMUtils.toString((Element) n) : String.valueOf(n)));
			out.appendChild(an);
		}

		final Collection<? extends XPathManipulationData>	ml=getManipulators();
		try
		{
			final Document	ret=
				XPathManipulationData.execute(out, getXPath(true), ml);
			if (ret != out)
				throw new TransformerException("Mismatched input/output document");
		}
		catch(Exception e)
		{
			if (e instanceof TransformerException)
				throw (TransformerException) e;
			throw new TransformerException(e);
		}
	}
	/*
	 * @see javax.xml.transform.Transformer#transform(javax.xml.transform.Source, javax.xml.transform.Result)
	 */
	@Override
	public void transform (Source src, Result res) throws TransformerException
	{
		transform(getDocument(src), getDocument(res));
	}
}
