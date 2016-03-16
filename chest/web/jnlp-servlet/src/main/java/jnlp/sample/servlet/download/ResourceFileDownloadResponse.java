/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */
package jnlp.sample.servlet.download;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import jnlp.sample.util.ObjectUtil;

public class ResourceFileDownloadResponse extends FileDownloadResponse {
    private URL _url;
    public URL getURL ()
    {
        return _url;
    }

    private URLConnection    _urlConn;
    public void setURL (URL u)
    {
        if (_url != u)
        {
            _url = u;

            if (_urlConn != null)
                _urlConn = null;
        }
    }

    public ResourceFileDownloadResponse (URL url, String mimeType, String versionId, long lastModified)
    {
        super(mimeType, versionId, lastModified, (null == url) ? null : url.toString());
        _url = url;
    }

    public ResourceFileDownloadResponse ()
    {
        this(null, null, null, 0L);
    }

    protected synchronized URLConnection getURLConnection () throws IOException
    {
        if (null == _urlConn)
        {
            final URL    url=getURL();
            _urlConn = (null == url) ? null : url.openConnection();
        }

        return _urlConn;
    }
    /*
     * @see jnlp.sample.servlet.DownloadResponse.FileDownloadResponse#getContentLength()
     */
    @Override
    public int getContentLength () throws IOException
    {
        final URLConnection    conn=getURLConnection();
        if (null == conn)
            throw new FileNotFoundException("getContentLength(" + getURL() + ") no " + URLConnection.class.getSimpleName());

        return conn.getContentLength();
    }
    /*
     * @see jnlp.sample.servlet.DownloadResponse.FileDownloadResponse#getContent()
     */
    @Override
    public InputStream getContent () throws IOException
    {
        return ObjectUtil.openResource(getURL());
    }
    /*
     * @see jnlp.sample.servlet.DownloadResponse#toString()
     */
    @Override
    public String toString () { return super.toString() + "[ " + getArgString() + "]"; }
    /*
     * @see jnlp.sample.servlet.download.FileDownloadResponse#clone()
     */
    @Override
    public ResourceFileDownloadResponse /* co-variant return */ clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
