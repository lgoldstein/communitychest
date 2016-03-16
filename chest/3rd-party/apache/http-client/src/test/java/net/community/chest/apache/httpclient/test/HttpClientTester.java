package net.community.chest.apache.httpclient.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.community.chest.apache.httpclient.HttpClientUtils;
import net.community.chest.apache.httpclient.jmx.JMXSession;
import net.community.chest.apache.httpclient.methods.ResourceDownloader;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.jmx.JMXProtocol;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;
import net.community.chest.net.proto.jmx.JMXAccessor;
import net.community.chest.test.TestBase;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Document;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 10, 2007 12:59:24 PM
 */
public class HttpClientTester extends TestBase {
    private static final int testResourceDownloader (
            final PrintStream out, final BufferedReader in, final ResourceDownloader dldr, final URI url)
    {
        for ( ; ; )
        {
            final String    outPath=getval(out, in, url + " output file path (or Quit)");
            if ((null == outPath) || (outPath.length() <= 0))
                continue;
            if (isQuit(outPath))
                break;

            try
            {
                final long    dStart=System.currentTimeMillis();
                final int    stCode=dldr.downloadResource(url, outPath);
                final long    dEnd=System.currentTimeMillis(), dDuration=dEnd - dStart;
                if (HttpClientUtils.isOKHttpRspCode(stCode))
                {
                    final File    outFile=new File(outPath);
                    final long    outSize=outFile.length();
                    out.println("\tDownloaded " + outSize + " bytes in " + dDuration + " msec.");
                }
                else
                {
                    System.err.println("Failed (err=" + stCode + ") in " + dDuration + " msec.");
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " on test " + url + " download: " + e.getMessage());
            }
        }

        return 0;
    }

    /*----------------------------------------------------------------------*/

    // each argument is a resource URL to be downloaded
    public static final int testResourceDownloader (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int                    numArgs=(null == args) ? 0 : args.length;
        final ResourceDownloader    dldr=new ResourceDownloader();
        dldr.setConnectionManager(new MultiThreadedHttpConnectionManager());

        for (int    aIndex=0; ; aIndex++)
        {
            final String    inPath=
                (aIndex < numArgs) ? args[aIndex] : getval(out, in, "resource URL (or Quit)");
            if ((null == inPath) || (inPath.length() <= 0))
                continue;
            if (isQuit(inPath))
                break;

            try
            {
                testResourceDownloader(out, in, dldr, new URI(inPath));
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " on test " + inPath + " download: " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static Collection<NameValuePair> setFlagValue (final PrintStream out, final BufferedReader in, final String flagName, final Collection<NameValuePair> npl)
    {
        final String    ans=getval(out, in, flagName + " (T)rue/[F]alse/(Q)uit");
        if ((null == ans) || (ans.length() <= 0))
            return npl;
        if (isQuit(ans))
            return null;

        final boolean    flag=('T' == Character.toUpperCase(ans.charAt(0)));
        if (flag)
            npl.add(new NameValuePair(flagName, String.valueOf(flag)));
        return npl;
    }

    private static final void handleJMXServletResponse (final HttpMethod m, final OutputStream out) throws Exception
    {
        Document    doc=null;
        {
            InputStream    inResp=null;
            try
            {
                inResp = m.getResponseBodyAsStream();

                if (null == (doc=DOMUtils.loadDocument(inResp)))
                    throw new StreamCorruptedException("No XML response");
            }
            finally
            {
                FileUtil.closeAll(inResp);
            }
        }

        final Transformer    t=DOMUtils.getDefaultXmlTransformer();
        final Source        s=new DOMSource(doc);
        final Result        r=new StreamResult(out);
        t.transform(s, r);
    }

    /*----------------------------------------------------------------------*/

    private static final int testListJMXMBeans (final PrintStream out, final BufferedReader in,
                                                 final HttpConnectionManager mgr,
                                                 final HostConfiguration     host,
                                                 final String                path)
    {
        for ( ; ; )
        {
            final String    name=getval(out, in, "MBean name (ENTER=all/Quit)");
            if (isQuit(name)) break;

            Collection<NameValuePair>    npl=new LinkedList<NameValuePair>();
            npl.add(new NameValuePair("req", "list"));
            if ((name != null) && (name.length() > 0))
                npl.add(new NameValuePair("name", "'" + name + "'"));

            if (null == (npl=setFlagValue(out, in, "attributes", npl)))
                continue;
            if (null == (npl=setFlagValue(out, in, "values", npl)))
                continue;
            if (null == (npl=setFlagValue(out, in, "null", npl)))
                continue;

            final GetMethod    gm=new GetMethod();
            gm.setFollowRedirects(true);
            gm.setPath(path);

            final NameValuePair[]    qp=npl.toArray(new NameValuePair[npl.size()]);
            gm.setQueryString(qp);

            try
            {
                final HttpClient    hc=new HttpClient(mgr);
                final int            stCode=hc.executeMethod(host, gm);
                if (!HttpClientUtils.isOKHttpRspCode(stCode))
                    throw new StreamCorruptedException("Bad response code: " + stCode);

                handleJMXServletResponse(gm, out);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            finally
            {
                gm.releaseConnection();
            }
        }

        return 0;
    }

    /*----------------------------------------------------------------------*/

    private static final int testGetJMXMBeanAttributes (final PrintStream out, final BufferedReader in,
                                                         final HttpConnectionManager mgr,
                                                         final HostConfiguration     host,
                                                         final String                path)
    {
        for ( ; ; )
        {
            final String    name=getval(out, in, "request XML file name (or Quit)");
            if ((null == name) || (name.length() <= 0))
                continue;
            if (isQuit(name)) break;

            Collection<NameValuePair>    npl=new LinkedList<NameValuePair>();
            npl.add(new NameValuePair("req", "get"));

            try
            {
                if (null == (npl=setFlagValue(out, in, "null", npl)))
                    continue;

                final PostMethod    gm=new PostMethod();
                gm.setFollowRedirects(false);
                gm.setPath(path);

                final NameValuePair[]    qp=npl.toArray(new NameValuePair[npl.size()]);
                gm.setQueryString(qp);

                InputStream    inReq=null;
                try
                {
                    inReq = new FileInputStream(name);
                    gm.setRequestEntity(new InputStreamRequestEntity(inReq, "text/xml"));

                    final HttpClient    hc=new HttpClient(mgr);
                    final int            stCode=hc.executeMethod(host, gm);
                    if (!HttpClientUtils.isOKHttpRspCode(stCode))
                        throw new StreamCorruptedException("Bad response code: " + stCode);

                    handleJMXServletResponse(gm, out);
                }
                finally
                {
                    gm.releaseConnection();
                    FileUtil.closeAll(inReq);
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    /*----------------------------------------------------------------------*/

    private static final int testJMXServlet (final PrintStream out, final BufferedReader in,
                                             final HttpConnectionManager mgr,
                                             final HostConfiguration     host,
                                             final String                path)
    {
        for ( ; ; )
        {
            final String    ans=getval(out, in, "[L]ist/(G)et/(Q)uit");
            if (isQuit(ans)) break;

            final char    opChar=((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toUpperCase(ans.charAt(0));
            switch(opChar)
            {
                case '\0'    :
                case 'L'    :
                    testListJMXMBeans(out, in, mgr, host, path);
                    break;

                case 'G'    :
                    testGetJMXMBeanAttributes(out, in, mgr, host, path);
                    break;

                default        :    // do nothing
            }
        }

        return 0;
    }

    /*----------------------------------------------------------------------*/

    // each argument is a JMX servlet prefix (e.g., 'http://localhost:8080/somePath') to which the arguments will be added
    public static final int testJMXServlet (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final HttpConnectionManager    mgr=new MultiThreadedHttpConnectionManager();
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "resource URL (or Quit)");
            if ((null == inPath) || (inPath.length() <= 0))
                continue;
            if (isQuit(inPath))
                break;

            try
            {
                final URL                url=new URL(inPath);
                final HostConfiguration    host=new HostConfiguration();
                host.setHost(url.getHost(), url.getPort());

                testJMXServlet(out, in, mgr, host, url.getPath());
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " on test " + inPath + " access: " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static final boolean getFlagValue (final PrintStream out, final BufferedReader in, final String name)
    {
        final String    ans=getval(out, in, name + " (T)rue/[F]alse");
        return (ans != null) && (ans.length() > 0) && ('T' == Character.toUpperCase(ans.charAt(0)));
    }

    private static final void displayAccessResult (final PrintStream out, final Collection<? extends MBeanEntryDescriptor>    mbl)
    {
        if ((mbl != null) && (mbl.size() > 0))
        {
            for (final MBeanEntryDescriptor mbe : mbl)
            {
                if (mbe != null)
                    out.println(mbe);
            }
        }
    }

    /*----------------------------------------------------------------------*/

    private static final int testJMXMAccessorGetValues (final PrintStream out, final BufferedReader in, final JMXAccessor    acc)
    {
        for ( ; ; )
        {
            final String    name=getval(out, in, "request XML file name (or Quit)");
            if ((null == name) || (name.length() <= 0))
                continue;
            if (isQuit(name)) break;

            final boolean includeNulls=getFlagValue(out, in, JMXProtocol.NULLS_PARAM);

            final long    qStart=System.currentTimeMillis();
            try
            {
                final Collection<? extends MBeanEntryDescriptor>    org=
                        MBeanEntryDescriptor.readMBeans(name, null),
                                                                    mbl=
                        acc.getValues(null, org, includeNulls);
                final int    numMBeans=(null == mbl) ? 0 : mbl.size();
                final long    qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
                out.println("Fetched " + numMBeans + " items in " + qDuration + " msec.");
                if (numMBeans > 0)
                    displayAccessResult(out, mbl);
            }
            catch(Exception e)
            {
                final long    qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
                System.err.println(e.getClass().getName() + " after " + qDuration + " msec.: " + e.getMessage());
            }
        }

        return 0;
    }
    /*----------------------------------------------------------------------*/

    private static final int testJMXMAccessorList (final PrintStream out, final BufferedReader in, final JMXAccessor    acc)
    {
        for ( ; ; )
        {
            final String    name=getval(out, in, "MBean name (ENTER=all/Quit)");
            if (isQuit(name)) break;

            final boolean    withAttributes=getFlagValue(out, in, JMXProtocol.ATTRIBUTES_PARAM),
                            withValues=getFlagValue(out, in, JMXProtocol.VALUES_PARAM),
                            includeNulls=getFlagValue(out, in, JMXProtocol.NULLS_PARAM),
                            withOperations=getFlagValue(out, in, JMXProtocol.OPERATIONS_PARAM),
                            withParams=getFlagValue(out, in, JMXProtocol.PARAMS_PARAM);
            final long    qStart=System.currentTimeMillis();
            try
            {
                final Collection<? extends MBeanEntryDescriptor>    mbl=
                    acc.list(name, null, withAttributes, withValues, includeNulls, withOperations, withParams);
                final long    qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
                final int    numMBeans=(null == mbl) ? 0 : mbl.size();
                out.println("Fetched " + numMBeans + " items in " + qDuration + " msec.");
                if (numMBeans > 0)
                    displayAccessResult(out, mbl);
            }
            catch(Exception e)
            {
                final long    qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
                System.err.println(e.getClass().getName() + " after " + qDuration + " msec.: " + e.getMessage());
            }
        }

        return 0;
    }

    /*----------------------------------------------------------------------*/

    private static final int testJMXAccessor (final PrintStream out, final BufferedReader in, final JMXAccessor    acc)
    {
        for ( ; ; )
        {
            final String    ans=getval(out, in, "[L]ist/(G)et/(Q)uit");
            if (isQuit(ans)) break;

            final char    opChar=((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toUpperCase(ans.charAt(0));
            switch(opChar)
            {
                case '\0'    :
                case 'L'    :
                    testJMXMAccessorList(out, in, acc);
                    break;

                case 'G'    :
                    testJMXMAccessorGetValues(out, in, acc);
                    break;

                default        :    // do nothing
            }
        }

        return 0;
    }

    /*----------------------------------------------------------------------*/

    // each argument is a JMX servlet prefix (e.g., 'http://localhost:8080/somePath') to which the arguments will be added
    public static final int testJMXAccessor (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "resource URL (or Quit)");
            if ((null == inPath) || (inPath.length() <= 0))
                continue;
            if (isQuit(inPath))
                break;

            try
            {
                final JMXAccessor    acc=new JMXSession();
                acc.connect(new URI(inPath));

                testJMXAccessor(out, in, acc);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " on test " + inPath + " access: " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testResourceDownloader(System.out, in, args);
//        final int                nErr=testJMXServlet(System.out, in, args);
//        final int                nErr=testJMXAccessor(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}
