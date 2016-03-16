package net.community.chest.rrd4j.common.test;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import net.community.chest.dom.DOMUtils;
import net.community.chest.rrd4j.common.core.ArcDefExt;
import net.community.chest.rrd4j.common.core.DsDefExt;
import net.community.chest.rrd4j.common.core.RrdDbExt;
import net.community.chest.rrd4j.common.core.RrdDefExt;
import net.community.chest.rrd4j.common.graph.RrdGraphDefExt;
import net.community.chest.test.TestBase;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 27, 2007 3:42:29 PM
 */
public class RrdDbAccessTest extends TestBase {

    /*----------------------------------------------------------------------*/

    private static final int testRrdExample (final PrintStream out, final BufferedReader in,
                                             final RrdDb rrdDb, final long crTime, final long updIncr)
    {
        try
        {
            out.println(rrdDb.getCanonicalPath());
        }
        catch(IOException e)
        {
            // ignored;
        }

        final Random    rnd=new Random(crTime);
        for (long curTimestamp=crTime + updIncr, lastVal=0L; ; )
        {
            final String    ans=getval(out, in, "# samples (or Quit)");
            if ((null == ans) || (ans.length() <= 0))
                continue;
            if (isQuit(ans)) break;

            final int    numSamples;
            try
            {
                if ((numSamples=Integer.parseInt(ans)) <= 0)
                    continue;
            }
            catch(NumberFormatException e)
            {
                continue;
            }

            try
            {
                final Sample sample=rrdDb.createSample();
                for (int    sIndex=0; sIndex < numSamples; curTimestamp += updIncr, sIndex++)
                {
                    final int        vIncr=rnd.nextInt(23);
                    final String    sVal=curTimestamp + ":" + lastVal;
                    sample.setAndUpdate(sVal);
                    out.println("\t" + sVal);
                    lastVal += vIncr;
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while creating " + numSamples + " samples: " + e.getMessage());
            }
        }

        return 0;
    }

    /*----------------------------------------------------------------------*/

    // args[i] is a path to a '.rrd' file
    public static final int testRrdExample (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    rrdPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "RRD file path (or Quit)");
            if ((null == rrdPath) || (rrdPath.length() <= 0))
                continue;
            if (isQuit(rrdPath)) break;

            RrdDb rrdDb=null;
            try
            {
                final RrdDefExt    rrdDef=new RrdDefExt(rrdPath);
                final long        crTime=System.currentTimeMillis();
                rrdDef.setStartTime(crTime);
                rrdDef.addDatasource(new DsDefExt("speed", DsType.COUNTER, 600L, Double.NaN, Double.NaN));
                rrdDef.addArchive(new ArcDefExt(ConsolFun.AVERAGE, 0.5, 1, 24));
                rrdDef.addArchive(new ArcDefExt(ConsolFun.AVERAGE, 0.5, 6, 10));

                rrdDb = new RrdDbExt(rrdDef);
                testRrdExample(out, in, rrdDb, crTime, 300L);
//                rrdDb.dumpXml(out);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            finally
            {
                if (rrdDb != null)
                {
                    try
                    {
                        rrdDb.close();
                    }
                    catch(IOException ce)
                    {
                        System.err.println(ce.getClass().getName() + " on close " + rrdPath + ": " + ce.getMessage());
                    }
                }
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    // args[i] is a path to an '.xml' file with graphing instructions
    private static final int testRrdGraph (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    xmlPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "graph XML file path (or Quit)");
            if ((null == xmlPath) || (xmlPath.length() <= 0))
                continue;
            if (isQuit(xmlPath)) break;

            try
            {
                final Document            doc=DOMUtils.loadDocument(xmlPath);
                final Element            elem=doc.getDocumentElement();
                final RrdGraphDefExt    rrdGraphDef=new RrdGraphDefExt(elem);
                final RrdGraph            rrdGraph=new RrdGraph(rrdGraphDef);
                final BufferedImage     bi=new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
                rrdGraph.render(bi.getGraphics());
                out.println("Processed " + xmlPath);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + "[" + xmlPath + "]: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
//        final int                nErr=testRrdExample(System.out, in, args);
        final int                nErr=testRrdGraph(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}
