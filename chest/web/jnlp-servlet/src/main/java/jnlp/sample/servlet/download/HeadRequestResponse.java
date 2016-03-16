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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

public class HeadRequestResponse extends DownloadResponse {
    private String _mimeType;
    public String getMimeType ()
    {
        return _mimeType;
    }

    public void setMimeType (String mimeType)
    {
        _mimeType = mimeType;
    }

    private String _versionId;
    public String getVersionId ()
    {
        return _versionId;
    }

    public void setVersionId (String versionId)
    {
        _versionId = versionId;
    }

    private long _lastModified;
    public long getLastModified ()
    {
        return _lastModified;
    }

    public void setLastModified (long lastModified)
    {
        _lastModified = lastModified;
    }

    public void setLastModified (Date d)
    {
        setLastModified((null == d) ? 0L : d.getTime());
    }

    public void setLastModified (Calendar c)
    {
        setLastModified((null == c) ? 0L : c.getTimeInMillis());
    }

    private int _contentLength;
    public int getContentLength ()
    {
        return _contentLength;
    }

    public void setContentLength (int contentLength)
    {
        _contentLength = contentLength;
    }

    public HeadRequestResponse (String mimeType, String versionId, long lastModified, int contentLength)
    {
        _mimeType = mimeType;
        _versionId = versionId;
        _lastModified = lastModified;
        _contentLength = contentLength;
    }

    public HeadRequestResponse ()
    {
        this(null, null, 0L, 0);
    }
    /*
     * @see jnlp.sample.servlet.DownloadResponse#sendRespond(javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void sendRespond (HttpServletResponse response) throws IOException
    {
        // Set header information
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(getMimeType());
        response.setContentLength(getContentLength());

        final String    vid=getVersionId();
        if ((vid != null) && (vid.length() > 0))
            response.setHeader(HEADER_JNLP_VERSION, vid);

        final long    lastMod=getLastModified();
        if (lastMod != 0L)
            response.setDateHeader(HEADER_LASTMOD, lastMod);
    }

    /*
     * @see jnlp.sample.servlet.download.DownloadResponse#clone()
     */
    @Override
    public HeadRequestResponse /* co-variant return */ clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
