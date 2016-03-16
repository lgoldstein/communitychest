package net.community.chest.apache.httpclient.jmx;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.util.Collection;

import net.community.chest.apache.httpclient.HttpClientUtils;
import net.community.chest.jmx.JMXErrorHandler;
import net.community.chest.jmx.JMXProtocol;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 7, 2008 12:39:40 PM
 */
public class JMXSession extends BaseHttpAccessor {
    public JMXSession ()
    {
        super();
    }

    private JMXErrorHandler    _eh;
    public JMXErrorHandler getJMXErrorHandler ()
    {
        return _eh;
    }

    public void setJMXErrorHandler (JMXErrorHandler eh)
    {
        _eh = eh;
    }
    /*
     * @see net.community.chest.net.proto.jmx.JMXAccessor#getValues(java.lang.String, java.util.Collection, boolean)
     */
    @Override
    public Collection<? extends MBeanEntryDescriptor> getValues (
            final String                                        domain,
            final Collection<? extends MBeanEntryDescriptor>     mbl,
            final boolean                                         includeNulls)
        throws IOException
    {
        final int                    numMBeans=(null == mbl) ? 0 : mbl.size();
        final HostConfiguration        host=getHostConfiguration();
        if (null == host)
            throw new ConnectException("getValues(" + numMBeans + "/" + domain + "/" + JMXProtocol.NULLS_PARAM + "=" + includeNulls + ") no host configuration specified");

        // NOTE !!! we run the query even if no MBean(s) requested - just as a live-check
        final Collection<NameValuePair>    npl=AccessorUtils.buildGetRequest(domain, includeNulls);
        final int                        numParams=(null == npl) ? 0 : npl.size();
        final NameValuePair[]            qp=(numParams <= 0) ? null : npl.toArray(new NameValuePair[numParams]);
        if ((null == qp) || (qp.length <= 0))
            throw new StreamCorruptedException("getValues(" + numMBeans + "/" + domain+ "/" + JMXProtocol.NULLS_PARAM + "=" + includeNulls + ") no query parameters");

        final PostMethod            gm=new PostMethod();
        gm.setFollowRedirects(false);
        gm.setPath(getAccessPath());
        gm.setQueryString(qp);

        final String                reqData=JMXProtocol.buildDescriptorsDocument(mbl);
        final StringRequestEntity    reqEntity=new StringRequestEntity(reqData, "text/xml", "UTF-8");
        gm.setRequestEntity(reqEntity);

        final long    qStart=System.currentTimeMillis();
        try
        {
            final HttpClient    hc=getClientInstance();
            final int            stCode=hc.executeMethod(host, gm);
            final long            qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
            if (!HttpClientUtils.isOKHttpRspCode(stCode))
            {
                final byte[]    rspBody=gm.getResponseBody();    // MUST call this to flush the response regardless of status code
                throw new StreamCorruptedException("getValues(" + numMBeans + "/" + domain + "/" + JMXProtocol.NULLS_PARAM + "=" + includeNulls + ") bad response code (" + stCode + ")[" + ((null == rspBody) ? 0 : rspBody.length) + " bytes] after " + qDuration + " msec.: " + HttpClientUtils.getMethodStatusLine(gm));
            }

            try
            {
                return AccessorUtils.convertJMXServletResponse(gm, getJMXErrorHandler());
            }
            catch(Exception e)
            {
                if (e instanceof IOException)
                    throw (IOException) e;

                throw new StreamCorruptedException("getValues(" + numMBeans + "/" + JMXProtocol.NULLS_PARAM + "=" + includeNulls + ") " + e.getClass().getName() + " while converting response: " + e.getMessage());
            }
        }
        finally
        {
            gm.releaseConnection();
        }
    }
    /*
     * @see net.community.chest.net.proto.jmx.JMXAccessor#list(java.lang.String, java.lang.String, boolean, boolean, boolean, boolean, boolean)
     */
    @Override
    public Collection<? extends MBeanEntryDescriptor> list (final String    name,
                                                            final String    domain,
                                                            final boolean    withAttributes,
                                                            final boolean    withValues,
                                                            final boolean    includeNulls,
                                                            final boolean    withOperations,
                                                            final boolean    withParameters)
        throws IOException
    {
        final HostConfiguration        host=getHostConfiguration();
        if (null == host)
            throw new ConnectException("list(" + name + "/" + domain + ") no host configuration specified");

        final Collection<NameValuePair>    npl=
            AccessorUtils.buildListRequest(name, domain, withAttributes, withValues, includeNulls, withOperations, withParameters);
        final int                        numParams=(null == npl) ? 0 : npl.size();
        final NameValuePair[]            qp=(numParams <= 0) ? null : npl.toArray(new NameValuePair[numParams]);
        if ((null == qp) || (qp.length <= 0))
            throw new StreamCorruptedException("list(" + name + "/" + domain + ") no query parameters");

        final GetMethod    gm=new GetMethod();
        gm.setFollowRedirects(true);
        gm.setPath(getAccessPath());
        gm.setQueryString(qp);

        final long    qStart=System.currentTimeMillis();
        try
        {
            final HttpClient    hc=getClientInstance();
            final int            stCode=hc.executeMethod(host, gm);
            final long            qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
            if (!HttpClientUtils.isOKHttpRspCode(stCode))
            {
                final byte[]    rspBody=gm.getResponseBody();    // MUST call this to flush the response regardless of status code
                throw new StreamCorruptedException("list(" + name + "/" + domain + ") bad response code (" + stCode + ")[" + ((null == rspBody) ? 0 : rspBody.length) + " bytes] after " + qDuration + " msec.: " + HttpClientUtils.getMethodStatusLine(gm));
            }

            try
            {
                return AccessorUtils.convertJMXServletResponse(gm, getJMXErrorHandler());
            }
            catch(Exception e)
            {
                if (e instanceof IOException)
                    throw (IOException) e;

                throw new StreamCorruptedException("list(" + name + "/" + JMXProtocol.ATTRIBUTES_PARAM + "=" + withAttributes + "/" + JMXProtocol.VALUES_PARAM + "=" + withValues + "/" + JMXProtocol.NULLS_PARAM + "=" + includeNulls + ") " + e.getClass().getName() + " while converting response: " + e.getMessage());
            }
        }
        finally
        {
            gm.releaseConnection();
        }
    }
}
