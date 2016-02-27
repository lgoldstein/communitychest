/*
 * @(#)DownloadResponse.java	1.8 07/03/15
 * 
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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import jnlp.sample.util.ObjectUtil;

/** 
 * A class used to encapsulate a file response, and
 * factory methods to create some common types.
 */
abstract public class DownloadResponse implements Cloneable {    
	public static final String HEADER_LASTMOD      = "Last-Modified";    
	public static final String HEADER_JNLP_VERSION = "x-java-jnlp-version-id";
	public static final String JNLP_ERROR_MIMETYPE = "application/x-java-jnlp-error";
    
    public static final int STS_00_OK           = 0;
    public static final int ERR_10_NO_RESOURCE  = 10;
    public static final int ERR_11_NO_VERSION   = 11;
    public static final int ERR_20_UNSUP_OS     = 20;
    public static final int ERR_21_UNSUP_ARCH   = 21;
    public static final int ERR_22_UNSUP_LOCALE = 22;
    public static final int ERR_23_UNSUP_JRE    = 23;
    public static final int ERR_99_UNKNOWN      = 99;

    // HTTP Compression RFC 2616 : Standard headers
    public static final String CONTENT_ENCODING         = "content-encoding";
    // HTTP Compression RFC 2616 : Standard header for HTTP/Pack200 Compression
    public static final String GZIP_ENCODING            = "gzip";
    public static final String PACK200_GZIP_ENCODING    = "pack200-gzip";

    protected DownloadResponse () { super(); }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString () { return getClass().getName(); }
    /*
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DownloadResponse /* co-variant return */ clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
    /**
     * @param response The {@link HttpServletResponse} to post response to
     * @throws IOException If failed to write the response
     */
    public abstract void sendRespond (HttpServletResponse response) throws IOException;

    /*---------- Factory methods for error responses --------------*/
    private static NotFoundResponse	_notFoundResp	/* =null */;
    public static synchronized DownloadResponse getNotFoundResponse ()
    { 
    	if (null == _notFoundResp)
    		_notFoundResp = new NotFoundResponse();
    	return _notFoundResp;
    }

    private static NoContentResponse	_noContentResp	/* =null */;
    public static final synchronized DownloadResponse getNoContentResponse ()
    { 
    	if (null == _noContentResp)
    		_noContentResp = new NoContentResponse();
    	return _noContentResp;
    }

    public static DownloadResponse getJnlpErrorResponse (int jnlpErrorCode)
    { 
    	return new JnlpErrorResponse(jnlpErrorCode);
    }

    private static NotModifiedResponse	_notModifiedResp	/* =null */;
    public static final synchronized DownloadResponse getNotModifiedResponse ()
    {
    	if (null == _notModifiedResp)
    		_notModifiedResp = new NotModifiedResponse();
    	return _notModifiedResp;
    }

    public static DownloadResponse getHeadRequestResponse (String mimeType, String versionId, long lastModified, int contentLength)
    {
        return new HeadRequestResponse(mimeType, versionId, lastModified, contentLength);
    }

    public static DownloadResponse getFileDownloadResponse (byte[] content, String mimeType, long timestamp, String versionId)
    {
    	return new ByteArrayFileDownloadResponse(content, mimeType, versionId, timestamp);
    }	
    

    public static DownloadResponse getFileDownloadResponse (File file, String mimeType, long timestamp, String versionId)
    {
    	return new DiskFileDownloadResponse(file, mimeType, versionId, timestamp);
    }	

    public static DownloadResponse getFileDownloadResponse (URL resource, String mimeType, long timestamp, String versionId)
    {
		if (ObjectUtil.isFileResource(resource))
			return getFileDownloadResponse(ObjectUtil.toFile(resource), mimeType, timestamp, versionId);
		else
			return new ResourceFileDownloadResponse(resource, mimeType, versionId, timestamp);
    }	
}



