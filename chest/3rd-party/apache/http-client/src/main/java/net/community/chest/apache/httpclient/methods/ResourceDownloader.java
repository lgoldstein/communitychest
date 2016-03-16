package net.community.chest.apache.httpclient.methods;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import net.community.chest.apache.httpclient.HttpClientUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.io.encode.base64.Base64DecodeOutputStream;
import net.community.chest.io.encode.qp.QPDecodeOutputStream;
import net.community.chest.mail.MimeEncodingTypeEnum;
import net.community.chest.mail.headers.RFCHeaderDefinitions;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 10, 2007 12:22:43 PM
 */
public class ResourceDownloader {
    private int    _bufSize=IOCopier.DEFAULT_COPY_SIZE;
    public int getIOBufSize ()
    {
        return _bufSize;
    }

    public void setIOBufSize (int bufSize)
    {
        _bufSize = bufSize;
    }
    /**
     * @param path Path to be accessed to download the resource
     * @return The {@link HttpMethod} to be used to access the resource
     * (default={@link GetMethod} with some default initializations).
     */
    public HttpMethod getAccessMethod (final String path)
    {
        final GetMethod    gm=new GetMethod();
        gm.setFollowRedirects(true);
        gm.setPath(path);
        gm.setRequestHeader(HttpHeaders.stdAcceptRangesHdr, HttpHeaders.bytesModifier);

        return gm;
    }

    public HttpMethod getAccessMethod (final String path, final long startPos, final long len)
    {
        final HttpMethod    gm=getAccessMethod(path);
        // check if partial access required
        if ((startPos > 0L) || (len > 0L))
        {
            final long        firstPos=Math.max(startPos, 0L);
            final String    endPos=(len > 0L) ? String.valueOf(firstPos + len - 1L) : "" ;

            gm.setRequestHeader(HttpHeaders.stdRangeHdr, HttpHeaders.bytesModifier + "=" + String.valueOf(firstPos) + "-" + endPos);
        }

        return gm;
    }

    protected OutputStream getEffectiveOutputStream (final HttpMethod gm, final OutputStream out) throws IOException
    {
        final Header                xferHdr=gm.getResponseHeader(RFCHeaderDefinitions.stdContentXferEncoding);
        final String                xferVal=(null == xferHdr) ? null : xferHdr.getValue();
        final MimeEncodingTypeEnum    xferType=MimeEncodingTypeEnum.fromXferEncoding(xferVal);
        if (MimeEncodingTypeEnum.BASE64.equals(xferType))
        {
            if (null == out)
                throw new IOException("getEffectiveOutputStream(" + xferVal + ") no original " + OutputStream.class.getSimpleName());

            return new Base64DecodeOutputStream(out, false);
        }
        else if (MimeEncodingTypeEnum.QUOTEDPRINTABLE.equals(xferType))
        {
            if (null == out)
                throw new IOException("getEffectiveOutputStream(" + xferVal + ") no original " + OutputStream.class.getSimpleName());

            return new QPDecodeOutputStream(out, false);
        }
        else    // if no/unknown xfer type then assume 8-bit
            return out;
    }

    protected InputStream getEffectiveInputStream (final HttpMethod gm) throws IOException
    {
        return (null == gm) ? null : gm.getResponseBodyAsStream();
    }

    public int downloadResource (final String                 path,
                                 final HttpClient             hc,
                                 final HostConfiguration    host,
                                 final OutputStream            out,
                                 final long                    startPos,
                                 final long                    len) throws IOException
    {
        final HttpMethod    gm=getAccessMethod(path, startPos, len);
        try
        {
            final int    stCode=(null == host)
                            ? hc.executeMethod(gm)
                            : hc.executeMethod(host, gm)
                            ;
            if (HttpClientUtils.isOKHttpRspCode(stCode))
            {
                OutputStream    effOut=null;
                InputStream        in=null;
                try
                {
                    effOut = getEffectiveOutputStream(gm, out);
                    in = getEffectiveInputStream(gm);

                    final long            cpySize=IOCopier.copyStreams(in, effOut, getIOBufSize());
                    if (cpySize < 0L)
                        throw new IOException("downloadResource(" + path + ")[" + host + "] bad I/O copy error: " + cpySize);
                }
                finally
                {
                    FileUtil.closeAll(in, effOut);
                }
            }

            return stCode;
        }
        finally
        {
            gm.releaseConnection();
        }
    }

    public int downloadResource (final String                path,
                                 final HttpClient            hc,
                                 final HostConfiguration    host,
                                 final OutputStream            out) throws IOException
    {
        return downloadResource(path, hc, host, out, 0L, -1L);
    }

    protected HttpClient getHttpConnection (final HttpConnectionManager    mgr,
                                            final HostConfiguration        host,
                                            final String                path) throws IOException
    {
        if (null == mgr)
            throw new IOException("getHttpConnection(" + host + ")[" + path + "] no " + HttpConnectionManager.class.getSimpleName() + " instance");

        return new HttpClient(mgr);
    }

    public int downloadResource (final String                     path,
                                 final HttpConnectionManager    mgr,
                                 final HostConfiguration        host,
                                 final OutputStream                out,
                                 final long                        startPos,
                                 final long                        len) throws IOException
    {
        return downloadResource(path, getHttpConnection(mgr, host, path), host, out, startPos, len);
    }

    public int downloadResource (final String                     path,
                                  final HttpConnectionManager    mgr,
                                  final HostConfiguration        host,
                                  final OutputStream                out) throws IOException
    {
        return downloadResource(path, mgr, host, out, 0L, (-1L));
    }

    public int downloadResource (final HttpConnectionManager    mgr,
                                 final URI                        url,
                                 final OutputStream                out,
                                 final long                        startPos,
                                 final long                        len) throws IOException
    {
        final HostConfiguration    hc=new HostConfiguration();
        hc.setHost(url.getHost(), url.getPort());

        return downloadResource(url.getPath(), mgr, hc, out, startPos, len);
    }

    public int downloadResource (final HttpConnectionManager    mgr,
                                  final URI                        url,
                                  final OutputStream                out) throws IOException
    {
        return downloadResource(mgr, url, out, 0L, (-1L));
    }

    private HttpConnectionManager    _mgr    /* =null */;
    public HttpConnectionManager getConnectionsManager ()
    {
        return _mgr;
    }

    public void setConnectionManager (HttpConnectionManager mgr)
    {
        _mgr = mgr;
    }

    public int downloadResource (final URI url, final OutputStream out, final long startPos, final long len) throws IOException
    {
        return downloadResource(getConnectionsManager(), url, out, startPos, len);
    }

    public int downloadResource (final URI url, final OutputStream out) throws IOException
    {
        return downloadResource(url, out, 0L, (-1L));
    }

    public int downloadResource (final URI url, final String outPath, final long startPos, final long len) throws IOException
    {
        OutputStream    fout=null;
        try
        {
            fout = new FileOutputStream(outPath);

            return downloadResource(url, fout, startPos, len);
        }
        finally
        {
            FileUtil.closeAll(fout);
        }
    }

    public int downloadResource (final URI url, final String outPath) throws IOException
    {
        return downloadResource(url, outPath, 0L, (-1L));
    }
}
