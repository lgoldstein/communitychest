/*
 *
 */
package net.community.chest.eclipse.classpath.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.eclipse.launch.AttributeDescriptor;
import net.community.chest.eclipse.launch.LaunchUtils;
import net.community.chest.io.dom.PrettyPrintTransformer;
import net.community.chest.test.TestBase;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 13, 2009 12:58:33 PM
 */
public class LaunchingTester extends TestBase {
    public static final int testLaunchFileParser (
            final PrintStream out, final BufferedReader in, final File inFile)
        throws ParserConfigurationException, SAXException, IOException
    {
        final Document    doc=DOMUtils.loadDocument(inFile);
        for ( ; ; )
        {
            out.append("Parsing ").append(inFile.getAbsolutePath()).println();
            try
            {
                final Collection<? extends Map.Entry<? extends AttributeDescriptor,?>>    al=
                    LaunchUtils.parseLaunchAttributes(doc);
                final int    numAttrs=(null == al) ? 0 : al.size();
                if (numAttrs > 0)
                {
                    for (final Map.Entry<? extends AttributeDescriptor,?> ae : al)
                    {
                        final AttributeDescriptor    ad=(null == ae) ? null : ae.getKey();
                        final Object                av=(null == ae) ? null : ae.getValue();
                        if ((null == ad) || (null == av))
                            continue;

                        out.append("\t[")
                           .append(ad.getAttributeType().toString())
                           .append("] ")
                           .append(ad.getAttributeKey())
                           .append('=')
                           ;
                        if (av instanceof Collection<?>)
                        {
                            final Collection<?>    vl=(Collection<?>) av;
                            out.append('[');
                            if ((vl != null) && (vl.size() > 0))
                            {
                                for (final Object vv : vl)
                                    out.append(' ').print(vv);
                            }
                            out.println(']');
                        }
                        else if (av instanceof Document)
                        {
                            final Document    vd=(Document) av;
                            PrettyPrintTransformer.DEFAULT.transform(vd, out);
                        }
                        else
                            out.println(av);
                    }
                }
                else
                    out.println("\tNo attributes extracted");
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }

            final String    ans=getval(out, in, "again [y]/n");
            if ((ans != null) && (ans.length() > 0)
             && ('y' != Character.toLowerCase(ans.charAt(0))))
                break;
        }

        return 0;
    }

    // each argument is an XML file representing an Eclipse launch structured file
    public static final int testLaunchFileParser (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inPath=
                (aIndex < numArgs) ? args[aIndex] : getval(out, in, "launch input file (or Quit)");
            if ((null == inPath) || (inPath.length() <= 0))
                continue;
            if (isQuit(inPath))
                break;

            try
            {
                testLaunchFileParser(out, in, new File(inPath));
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testLaunchFileParser(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}
