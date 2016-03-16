package net.community.chest.rrd4j.client.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import net.community.chest.apache.log4j.test.Log4jTester;
import net.community.chest.dom.DOMUtils;
import net.community.chest.rrd4j.client.jmx.AbstractMBeanRrdPoller;
import net.community.chest.rrd4j.client.jmx.http.HttpRrdPollerInstantiator;
import net.community.chest.rrd4j.common.core.RrdDefExt;
import net.community.chest.rrd4j.common.jmx.MBeanRrdDef;
import net.community.chest.test.TestBase;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 10, 2008 2:29:44 PM
 */
public class MBeanPollerTest extends TestBase {
    private MBeanPollerTest ()
    {
        super();
    }
    // args[0]=servlet URL, args[1]=configuration file
    private static final int testJMXAccessor (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final String[]    tstArgs=resolveTestParameters(out, in, args,
                new String[] { "servlet URL", "configuration file path", "log4j XML path" });
        final String    urlArg=((null == tstArgs) || (tstArgs.length <= 0)) ? null : tstArgs[0],
                        xmlPath=((null == tstArgs) || (tstArgs.length <= 1)) ? null : tstArgs[1],
                        log4jPath=((null == tstArgs) || (tstArgs.length <= 2)) ? null : tstArgs[2];
        if ((null == urlArg) || (urlArg.length() <= 0)
         || (null == xmlPath) || (xmlPath.length() <= 0))
            return (-1);
        if (isQuit(urlArg) || isQuit(xmlPath) || isQuit(log4jPath))
            return 0;

        if ((log4jPath != null) && (log4jPath.length() > 0))
            Log4jTester.log4jInit(out, log4jPath);

        try
        {
            final Document                            doc=DOMUtils.loadDocument(xmlPath);
            final Collection<? extends MBeanRrdDef> defs=MBeanRrdDef.readDefinitions(doc);
            final Map<String,? extends Collection<? extends MBeanRrdDef>>    cm=RrdDefExt.checkDuplicatePaths(defs);
            if ((cm != null) && (cm.size() > 0))
                throw new IllegalStateException("Duplicate paths found");

            final HttpRrdPollerInstantiator    inst=new HttpRrdPollerInstantiator(new URI(urlArg));
            final LoggerWrapper                log=WrapperFactoryManager.getLogger(MBeanPollerTest.class);

            final Collection<? extends Map.Entry<String,? extends AbstractMBeanRrdPoller>>    threads=
                inst.start(defs, null, log);
            final int                                                                        numThreads=
                (null == threads) ? 0 : threads.size();
            if (numThreads <= 0)
                throw new IllegalStateException("No threads created");
            log.info("started " + numThreads + " threads");
            for ( ; ; )
            {
                final String    ans=getval(out, in, "(Q)uit");
                if (isQuit(ans)) break;
            }

            for (final Map.Entry<String,? extends AbstractMBeanRrdPoller> te : threads)
            {
                final AbstractMBeanRrdPoller t=(null == te) ? null : te.getValue();
                if (t != null)
                {
                    try
                    {
                        t.close();
                        log.info("stopped thread=" + te.getKey());
                    }
                    catch(Exception e)
                    {
                        System.err.println(e.getClass().getName() + " while stopping thread=" + te.getKey() + ": " + e.getMessage());
                    }
                }
            }

            return 0;
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
        final BufferedReader    in=getStdin();
//        final int                nErr=testResourceDownloader(System.out, in, args);
//        final int                nErr=testJMXServlet(System.out, in, args);
        final int                nErr=testJMXAccessor(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}
