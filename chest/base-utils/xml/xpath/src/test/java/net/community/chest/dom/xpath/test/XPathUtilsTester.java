/*
 * 
 */
package net.community.chest.dom.xpath.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Collection;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.NodeTypeEnum;
import net.community.chest.dom.xpath.manip.XPathManipulationData;
import net.community.chest.dom.xpath.manip.XPathManipulationTransformer;
import net.community.chest.io.dom.PrettyPrintTransformer;
import net.community.chest.test.DOMUtilsTester;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 7, 2009 11:33:53 AM
 */
public class XPathUtilsTester extends DOMUtilsTester {
	protected XPathUtilsTester ()
	{
		super();
	}

	//////////////////////////////////////////////////////////////////////////

	public static final int testXPathAccess (
			final PrintStream out, final BufferedReader in, final XPathFactory	fac, final Document doc)
	{
		for (final XPath	p=fac.newXPath(); ; )
		{
			final String	xpExpr=getval(out, in, "XPATH expression (or Quit)");
			if ((null == xpExpr) || (xpExpr.length() <= 0))
				continue;
			if (isQuit(xpExpr))
				break;

			try
			{
				final Object	res=p.evaluate(xpExpr, doc, XPathConstants.NODESET);
				if (res != null)
				{
					if (res instanceof NodeList)
					{
						final NodeList	nl=(NodeList) res;
						final int		numNodes=nl.getLength();
						if (numNodes <= 0)
						{
							System.err.println("No matches found");
							continue;
						}

						for (int nIndex=0; nIndex < numNodes; nIndex++)
						{
							final Node			n=nl.item(nIndex);
							final NodeTypeEnum	t=NodeTypeEnum.fromNode(n);
							if (null == t)
							{
								System.err.println("Unknown node type: " + ((null == n) ? null : n.getClass().getName()));
								continue;
							}

							out.print("\t" + t + ": ");
							switch(t)
							{
								case ELEMENT	:
									out.println(DOMUtils.toString((Element) n));
									break;
								case ATTRIBUTE	:
									out.println(DOMUtils.toString((Attr) n));
									break;
								default			:
									out.println("Unexpected node type");
							}
						}
					}
					else
						out.println("Got a " + res.getClass().getName());
				}
				else
					System.err.println("No result");
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}

		return 0;
	}

	/* ------------------------------------------------------------------- */
	// each argument is an XML file path
	public static final int testXPathAccess (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final XPathFactory	fac=XPathFactory.newInstance();

		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	inPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "XML input file (or Quit)");
			if ((null == inPath) || (inPath.length() <= 0))
				continue;
			if (isQuit(inPath))
				break;

			out.println("Processing " + inPath);
			try
			{
				if (testXPathAccess(out, in, fac, DOMUtils.loadDocument(inPath)) < 0)
					break;
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + " while handle file=" + inPath + ": " + e.getMessage());
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static final int testXPathManipulations (
			final PrintStream out, final BufferedReader in, final String inPath, final Collection<? extends XPathManipulationData>	ml)
	{
		for (XPathManipulationTransformer t=null; ; )
		{
			final String	xt=getval(out, in, inPath + " - use transformer [y]/n/q");
			if (isQuit(xt)) break;

			try
			{
				final Document	org=DOMUtils.loadDocument(inPath), doc;
				if ((null == xt) || (xt.length() <= 0) || (Character.toLowerCase(xt.charAt(0)) == 'y'))
				{
					doc = DOMUtils.createDefaultDocument();

					if (null == t)
						t = new XPathManipulationTransformer(ml);
					t.transform(new DOMSource(org), new DOMResult(doc));
				}
				else
					doc = XPathManipulationData.execute(org, ml);

				PrettyPrintTransformer.DEFAULT.transform(doc, out);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}

		return 0;
	}

	public static final int testXPathManipulations (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final String[]	prompts={ "input XML file", "XPath manipulation file" },
		             	vals=resolveTestParameters(out, in, args, prompts);
		if ((null == vals) || (vals.length < prompts.length))
			return 0;

		try
		{
			final Document										doc=
				DOMUtils.loadDocument(vals[1]);
			final Collection<? extends XPathManipulationData>	ml=
				XPathManipulationData.loadOperations(doc, new XPathManipTestListener(out));
			return testXPathManipulations(out, in, vals[0], ml);
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			return (-1);
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
//		final int				nErr=testXPathAccess(System.out, in, args);
		final int				nErr=testXPathManipulations(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}

}
