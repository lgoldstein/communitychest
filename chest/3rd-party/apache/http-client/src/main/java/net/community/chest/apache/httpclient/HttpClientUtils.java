package net.community.chest.apache.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.io.output.AutoGrowArrayOutputStream;
import net.community.chest.net.proto.text.http.HttpUtils;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.w3c.dom.Document;
/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful static methods</P>
 * @author Lyor G.
 * @since Oct 10, 2007 11:47:57 AM
 */
public final class HttpClientUtils {
    private HttpClientUtils ()
    {
        // no instance
    }
    /**
     * @param rspCode response code to be checked
     * @return TRUE if this is an 2xx code
     */
    public static final boolean isOKHttpRspCode (final int rspCode)
    {
        return ((rspCode >= HttpStatus.SC_OK) && (rspCode <= 299));
    }
    /**
     * @param hm method whose response line is to be extracted
     * @return status line (e.g., "404 Not found") - <I>null</I> if
     * <I>null</I> method object
     */
    public static final String getMethodStatusLine (final HttpMethod hm)
    {
        return (null == hm) ? null : String.valueOf(hm.getStatusCode()) + " " + hm.getStatusText();
    }
    /**
     * @param dataBuf response body/payload
     * @param pos start position in payload
     * @param len number of bytes to convert to string
     * @param csName charset name to use
     * @return string for this payload converted according to supplied charset
     * (<I>null</I> if error buffer)
     * @throws UnsupportedEncodingException if unable to convert payload to specified charset
     */
    public static final String getPayload (final byte[] dataBuf, final int pos, int len, final String csName) throws UnsupportedEncodingException
    {
        if ((null == dataBuf) || (dataBuf.length <= 0) || (pos <= 0) || ((pos+len) > dataBuf.length))
            return null;
        else
            return new String(dataBuf, pos, len, csName);
    }
    /**
     * @param dataBuf response body/payload
     * @param csName charset name to use
     * @return string for this payload converted according to supplied charset
     * @throws UnsupportedEncodingException if unable to convert payload to specified charset
     */
    public static final String getPayload (final byte[] dataBuf, final String csName) throws UnsupportedEncodingException
    {
        return getPayload(dataBuf, 0, (null == dataBuf) ? 0 : dataBuf.length, csName);
    }

    public static final String DEFAULT_PAYLOAD_CHARSET="US-ASCII";
    /**
     * @param dataBuf response body/payload
     * @param pos start position in payload
     * @param len number of bytes to convert to string
     * @return string for this payload converted according as US-ASCII
     * (<I>null</I> if error buffer)
     * @throws UnsupportedEncodingException if unable to convert payload to specified charset
     */
    public static final String getPayload (final byte[] dataBuf, final int pos, int len) throws UnsupportedEncodingException
    {
        return getPayload(dataBuf, pos, len, DEFAULT_PAYLOAD_CHARSET);
    }
    /**
     * @param dataBuf response body/payload
     * @return <U>US-ASCII<U> string for this payload
     * @throws UnsupportedEncodingException if unable to convert payload to US-ASCII
     */
    public static final String getPayload (final byte[] dataBuf) throws UnsupportedEncodingException
    {
        return getPayload(dataBuf, 0, (null == dataBuf) ? 0 : dataBuf.length);
    }
    /**
     * @param hm method used for the request
     * @param csName charset name to use for conversion
     * @return converted string for this payload (<I>null</I> if no method/payload)
     * @throws IOException if unable to convert payload
     */
    public static final String getPayload (final HttpMethod hm, final String csName) throws IOException
    {
        return (null == hm) ? null : getPayload(hm.getResponseBody(), csName);
    }
    /**
     * @param hm method used for the request
     * @return US-ASCII string for this payload
     * @throws IOException if unable to convert payload
     */
    public static final String getPayload (final HttpMethod hm) throws IOException
    {
        return getPayload(hm, DEFAULT_PAYLOAD_CHARSET);
    }
    /**
     * @param inPayload dequeued payload body as stream
     * @param csName charset name to use
     * @return payload as string
     * @throws IOException if unable to convert payload
     */
    public static final String getPayloadAsString (final InputStream inPayload, final String csName) throws IOException
    {
        final InputStreamReader    inReader=(null == inPayload) ? null : new InputStreamReader(inPayload, csName);
        if (null == inReader)
            return null;

        final StringWriter    outWriter=new StringWriter(IOCopier.DEFAULT_COPY_SIZE);
        final long            cpyLen=IOCopier.copyReaderToWriter(inReader, outWriter);
        if (cpyLen < 0L)
            return null;

        return outWriter.toString();
    }
    /**
     * @param inPayload dequeued payload body as stream
     * @return payload as string in <U>US-ASCII</U>
     * @throws IOException if unable to convert payload
     */
    public static final String getPayloadAsString (final InputStream inPayload) throws IOException
    {
        return getPayloadAsString(inPayload, DEFAULT_PAYLOAD_CHARSET);
    }
    /**
     * @param inPayload dequeued payload body as stream
     * @return payload as de-facto bytes array
     * @throws IOException if cannot convert payload to bytes
     */
    public static final byte[] getPayloadAsBytes (final InputStream inPayload) throws IOException
    {
        final AutoGrowArrayOutputStream    ags=new AutoGrowArrayOutputStream();
        final long                        cpyLen=IOCopier.copyStreams(inPayload, ags);
        if (cpyLen < 0L)
            return null;
        else
            return ags.toByteArray();
    }

    public static final Document loadDocument (final HttpMethod m) throws Exception
    {
        InputStream    inResp=null;
        try
        {
            inResp = m.getResponseBodyAsStream();

            return DOMUtils.loadDocument(inResp);
        }
        finally
        {
            FileUtil.closeAll(inResp);
        }
    }
    /**
     * Checks if the response code is a redirection
     * @param rspCode response code to be checked
     * @return TRUE if this is a re-direction code
     */
    public static final boolean isRedirectHttpRspCode (final int rspCode)
    {
        return (HttpStatus.SC_MOVED_TEMPORARILY == rspCode) ||
               (HttpStatus.SC_MOVED_PERMANENTLY == rspCode) ||
               (HttpStatus.SC_SEE_OTHER == rspCode) ||
               (HttpStatus.SC_TEMPORARY_REDIRECT == rspCode);
    }

    public static final List<NameValuePair> getQueryStringParameters (final String qry)
    {
        final Collection<? extends Map.Entry<String,String>>    pl=
            HttpUtils.getQueryStringParameters(qry);
        final int                                                numPairs=
            (null == pl) ? 0 : pl.size();
        if (numPairs <= 0)
            return null;

        final List<NameValuePair>    nvl=new ArrayList<NameValuePair>(numPairs);
        for (final Map.Entry<String,String> pe : pl)
        {
            final String    n=(null == pe) ? null : pe.getKey(),
                            v=(null == pe) ? null : pe.getValue();
            if ((null == n) || (n.length() <= 0)
             || (null == v) || (v.length() <= 0))
                continue;

            nvl.add(new NameValuePair(n,v));
        }

        return nvl;
    }

    public static final List<NameValuePair> getQueryStringParameters (final URI u)
    {
        return (null == u) ? null : getQueryStringParameters(u.getQuery());
    }

    public static final Collection<NameValuePair> getQueryStringParameters (final URL u)
    {
        return (null == u) ? null : getQueryStringParameters(u.getQuery());
    }
}
