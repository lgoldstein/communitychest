package net.community.chest.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.community.chest.Triplet;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.NodeTypeEnum;
import net.community.chest.io.FileUtil;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 7, 2007 11:26:35 AM
 */
public class DOMUtilsTester extends TestBase {
    protected DOMUtilsTester ()
    {
        // no instance
    }

    /* ------------------------------------------------------------------- */

    public static final void printNode (final PrintStream out, final String indent, final Node n)
    {
        if (null == n)
            return;

        final NodeTypeEnum    t=NodeTypeEnum.fromNode(n);
        out.append(indent)
               .append('[').append((null == t) ? "???" : t.name()).append(']')
               .append(' ').append(n.getNodeName())
               .append('=').append(n.getNodeValue())
           .println()
           ;
    }

    //////////////////////////////////////////////////////////////////////////

    public static final int testDocumentTransformer (final PrintStream out, final BufferedReader in,
                                                     final String inPath, final Document doc,
                                                     final Transformer xf, final String outPath)
    {
        for ( ; ; )
        {
            out.println("Transforming " + inPath + " => " + outPath);

            try
            {
                OutputStream        os=new FileOutputStream(outPath);
                final StreamResult    res=new StreamResult(os);
                final DOMSource        src=new DOMSource(doc);
                try
                {
                    xf.transform(src, res);
                }
                finally
                {
                    FileUtil.closeAll(os);
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }

            final String    ans=getval(out, in, "again (y)/[n]");
            if ((null == ans) || (ans.length() <= 0) || (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }

        return 0;
    }

    /* ------------------------------------------------------------------- */

    public static final int testDocumentTransformer (final PrintStream out, final BufferedReader in,
             final String inPath, final Transformer xf, final String outPath)
        throws ParserConfigurationException, SAXException, IOException
    {
        final DocumentBuilderFactory    docFactory=DOMUtils.getDefaultDocumentsFactory();
        final DocumentBuilder            docBuilder=docFactory.newDocumentBuilder();
        final Document                    doc=docBuilder.parse(new File(inPath));
        return testDocumentTransformer(out, in, inPath, doc, xf, outPath);
    }

    //////////////////////////////////////////////////////////////////////////

    public static final int testElementDataParsing (
            final PrintStream out, final BufferedReader in, final Document owner, final String elemData)
    {
        for (final Element    root=(null == owner) ? null : owner.getDocumentElement(); ; )
        {
            out.println(elemData);
            try
            {
                final Triplet<? extends Element,? extends CharacterData,Boolean>    pe=
                    DOMUtils.parseElementString(owner, elemData);
                final Element                                                        elem=
                    (null == pe) ? null : pe.getV1();
                out.append("\tName: ")
                   .append((null == elem) ? null : elem.getTagName())
                   .println()
                   ;

                final CharacterData    cd=(null == pe) ? null : pe.getV2();
                if (cd != null)
                    out.append("\tText: ")
                       .append(cd.getData())
                       .println()
                       ;

                final Boolean    end=(null == pe) ? null : pe.getV3();
                out.append("\tTerminated:")
                   .append(String.valueOf(end))
                   .println()
                   ;

                final NamedNodeMap    attrs=elem.getAttributes();
                final int            numAttrs=(null == attrs) ? 0 : attrs.getLength();
                for (int    aIndex=0; aIndex < numAttrs; aIndex++)
                    printNode(out, "\t\t", attrs.item(aIndex));

                final NodeList    chl=elem.getChildNodes();
                final int        numNodes=(null == chl) ? 0 : chl.getLength();
                for (int    nIndex=0; nIndex < numNodes; nIndex++)
                    printNode(out, "\t\t", chl.item(nIndex));

                final String    elemText=DOMUtils.toString(elem);
                out.append("\t=> ").println(elemText);

                if (root != null)
                    root.appendChild(elem);
            }
            catch(Exception e)
            {
                System.err.println("testElementDataParsing(" + elemData + ") " + e.getClass().getName() + ": " + e.getMessage());
            }

            final String    ans=getval(out, in, "again (y)/[n]");
            if ((null == ans) || (ans.length() <= 0) || (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }

        return 0;
    }

    // each argument is an XML element string to be parsed
    public static final int testElementParsing (
            final PrintStream out, final BufferedReader in, final Document owner, final String ... args)
    {
        if (owner != null)
        {
            final Element    root=owner.createElement("results");
            owner.appendChild(root);
        }

        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    elemData=
                (aIndex < numArgs) ? args[aIndex] : getval(out, in, "XML element data (or Quit)");
            if ((null == elemData) || (elemData.length() <= 0))
                continue;
            if (isQuit(elemData)) break;

            // just in case we had to use single quotes...
            testElementDataParsing(out, in, owner, elemData.replace('\'', '"'));
        }

        return 0;
    }

    public static final int testElementParsing (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final String    ans=getval(out, in, "use owner document y/[n]/q");
        if (isQuit(ans))
            return 0;

        Document    owner=null;
        if ((ans != null) && (ans.length() > 0) && ('y' == Character.toLowerCase(ans.charAt(0))))
        {
            try
            {
                owner = DOMUtils.createDefaultDocument();
            }
            catch(ParserConfigurationException e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        final int    nErr=testElementParsing(out, in, owner, args);
        if (owner != null)
        {
            // TODO show resulting document
        }

        return nErr;
    }

    //////////////////////////////////////////////////////////////////////////

    private static final int testDOMTransformer (final PrintStream out, final BufferedReader in, final String inPath, final Document doc)
    {
        for ( ; ; )
        {
            final String    ans=getval(out, in, "output file path (ENTER=" + inPath + "-bak/Quit)");
            if (isQuit(ans)) break;

            final String    outPath;
            if ((null == ans) || (ans.length() <= 0))
            {
                final int        sPos=inPath.lastIndexOf('.');
                final String    sfx=inPath.substring(sPos),
                                pfx=inPath.substring(0, sPos);
                outPath = pfx + "-bak" + sfx;
            }
            else
                outPath = ans;

            try
            {
                final Transformer    t=DOMUtils.getDefaultXmlTransformer();
                final DOMSource        s=new DOMSource(doc);
                final StreamResult    r=new StreamResult(new File(outPath));
                final long            tStart=System.currentTimeMillis();
                t.transform(s, r);
                final long            tEnd=System.currentTimeMillis(), tDuration=tEnd - tStart;
                out.println("\t" + inPath + " transformed in " + tDuration + " msec. to " + outPath);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while transform file=" + inPath + ": " + e.getMessage());
            }
        }

        return 0;
    }
    // each argument is an XML file string to be parsed and re-saved with transformer
    public static final int testDOMTransformer (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "XML input path (or Quit)");
            if ((null == inPath) || (inPath.length() <= 0))
                continue;
            if (isQuit(inPath))
                break;

            final Document    doc;
            try
            {
                final DocumentBuilderFactory    docFactory=DOMUtils.getDefaultDocumentsFactory();
                final DocumentBuilder            docBuilder=docFactory.newDocumentBuilder();
                doc = docBuilder.parse(new File(inPath));
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while parse file=" + inPath + ": " + e.getMessage());
                continue;
            }

            testDOMTransformer(out, in, inPath, doc);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static final void testDOMStructure (final PrintStream out, final Node n, final String indent)
    {
        final String        subIndent=indent + "\t";
        final NodeTypeEnum    nType=NodeTypeEnum.fromNode(n);
        out.println(indent + n.getNodeName() + "[" + nType + "](" + n.getNodeValue() + ")");
        if (NodeTypeEnum.ELEMENT.equals(nType))
        {
            final Element        elem=(Element) n;
            final NamedNodeMap    aMap=elem.getAttributes();
            final int            numAttrs=(null == aMap) ? 0 : aMap.getLength();
            for (int    aIndex=0; aIndex < numAttrs; aIndex++)
            {
                final Node    a=aMap.item(aIndex);
                testDOMStructure(out, a, subIndent);
            }
        }

        final NodeList    nodes=n.getChildNodes();
        final int        numNodes=(null == nodes) ? 0 : nodes.getLength();
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
            testDOMStructure(out, nodes.item(nIndex), subIndent);
    }

    private static final int testDOMStructure (final PrintStream out, final BufferedReader in, final String inPath, final Document doc)
    {
        for ( ; ; )
        {
            out.println("Structure of " + inPath);

            testDOMStructure(out, doc.getDocumentElement(), "\t");

            final String    ans=getval(out, in, "again [y]/n");
            if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }

        return 0;
    }

    // each argument is an XML file string to be parsed and whose structure is to be displayed
    public static final int testDOMStructure (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "XML input path (or Quit)");
            if ((null == inPath) || (inPath.length() <= 0))
                continue;
            if (isQuit(inPath))
                break;

            final Document    doc;
            try
            {
                final DocumentBuilderFactory    docFactory=DOMUtils.getDefaultDocumentsFactory();
                final DocumentBuilder            docBuilder=docFactory.newDocumentBuilder();
                doc = docBuilder.parse(new File(inPath));
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while parse file=" + inPath + ": " + e.getMessage());
                continue;
            }

            testDOMStructure(out, in, inPath, doc);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    // each argument is an XML expression string
    public static final int testDOMStringParser (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inString=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "XML input string (or Quit)");
            if ((null == inString) || (inString.length() <= 0))
                continue;
            if (isQuit(inString))
                break;

            final Document    doc;
            try
            {
                final DocumentBuilderFactory    docFactory=DOMUtils.getDefaultDocumentsFactory();
                final DocumentBuilder            docBuilder=docFactory.newDocumentBuilder();
                Reader                            r=null;
                try
                {
                    r = new StringReader(inString);
                    doc = docBuilder.parse(new InputSource(r));
                }
                finally
                {
                    FileUtil.closeAll(r);
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while parse string=" + inString + ": " + e.getMessage());
                continue;
            }

            testDOMStructure(out, in, inString, doc);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    // each argument is an XML file path
    public static final int testJAXBMarshalling (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inString=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "XML output file path (or Quit)");
            if ((null == inString) || (inString.length() <= 0))
                continue;
            if (isQuit(inString))
                break;

            try
            {
                JAXB.marshal(inString, new File(inString));
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while output to file=" + inString + ": " + e.getMessage());
            }
        }

        return 0;
    }

    // each argument is an XML file path
    public static final int testJAXBUnmarshalling (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inString=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "XML input file path (or Quit)");
            if ((null == inString) || (inString.length() <= 0))
                continue;
            if (isQuit(inString))
                break;

            String    inClass=null;
            do {
                inClass = getval(out, in, "class of " + inString + " (or Quit)");
            } while ((null == inClass) || (inClass.length() <= 0));
            if (isQuit(inClass))
                break;

            try
            {
                final Object    o=JAXB.unmarshal(new File(inString), ClassUtil.loadClassByName(inClass));
                out.println("\trecovered [" + o.getClass().getName() + "] " + o);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while input from file=" + inString + ": " + e.getMessage());
            }
        }

        return 0;
    }
    // each argument is (m)arshal/(u)nmarshal request
    public static final int testJAXBInterface (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    ans=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "(m)arshal/(u)nmarshal/(q)uit");
            if (isQuit(ans)) break;

            final char    c=((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toLowerCase(ans.charAt(0));
            switch(c)
            {
                case 'm'    :
                    testJAXBMarshalling(out, in);
                    break;
                case 'u'    :
                    testJAXBUnmarshalling(out, in);
                    break;

                default        :    // ignore
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testElementParsing(System.out, in, args);
//        final int                nErr=testDOMTransformer(System.out, in, args);
//        final int                nErr=testDOMStructure(System.out, in, args);
//        final int                nErr=testDOMStringParser(System.out, in, args);
//        final int                nErr=testJAXBInterface(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}
