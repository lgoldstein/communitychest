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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import jnlp.sample.servlet.JnlpResource;
import jnlp.sample.util.log.Logger;
import jnlp.sample.util.log.LoggerFactory;

public abstract class FileDownloadResponse extends DownloadResponse {        
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

	private String _fileName;
	public String getFileName ()
	{ 
		return _fileName;
	}

	public void setFileName (String fileName)
	{
		_fileName = fileName;
	}

	private final transient Logger	_log;
	protected FileDownloadResponse (String mimeType, String versionId, long lastModified, String fileName)
	{
		_mimeType = mimeType;
		_versionId = versionId;
		_lastModified = lastModified;
		_fileName = fileName;
		_log = LoggerFactory.getLogger(FileDownloadResponse.class);
	}

	protected FileDownloadResponse (String mimeType, String versionId, long lastModified)
	{
		this(mimeType, versionId, lastModified, null);
	}

	public abstract int getContentLength () throws IOException;
	public abstract InputStream getContent () throws IOException;

	protected OutputStream getOutputStream (HttpServletResponse response) throws IOException
	{
		return response.getOutputStream();
	}

	public static final int	DATA_COPY_SIZE=4 * 1024;
	/*
	 * @see jnlp.sample.servlet.DownloadResponse#sendRespond(javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void sendRespond (HttpServletResponse response) throws IOException
	{		
	    // Set header information
		final int	totalLen=getContentLength();
		response.setStatus(HttpServletResponse.SC_OK);
	    response.setContentType(getMimeType());
	    response.setContentLength(totalLen);

	    final String	vid=getVersionId();
	    if ((vid != null) && (vid.length() > 0))
	    	response.setHeader(HEADER_JNLP_VERSION, getVersionId());

	    final long	lastMod=getLastModified();
	    if (lastMod != 0L)
	    	response.setDateHeader(HEADER_LASTMOD, lastMod);			    

	    final String	fileName=getFileName();
	    if ((fileName != null) && (fileName.length() > 0))
	    {
	    	if (fileName.endsWith(JnlpResource.PACK_GZ_SUFFIX))
	    		response.setHeader(CONTENT_ENCODING, PACK200_GZIP_ENCODING );
	    	else if (fileName.endsWith(JnlpResource.GZ_SUFFIX))
	    		response.setHeader(CONTENT_ENCODING, GZIP_ENCODING );
	    	else
	    		response.setHeader(CONTENT_ENCODING, null);
	    }

	    final String	argVal=_log.isDebugLevel() ? getArgString() : null;
		if (_log.isDebugLevel())
			_log.debug("sendRespond(" + fileName + ") start " + argVal);

		// Send contents	   	
    	int					totalRead=0;
    	final long			cpyStart=System.currentTimeMillis();
        final OutputStream  out=getOutputStream(response);
    	try
    	{
            InputStream    in=getContent();
            if (in == null)
    			throw new EOFException("No content " + InputStream.class.getSimpleName() + " created");

    		try {
        		final byte[]	bytes=new byte[DATA_COPY_SIZE];
        		for (int	read=in.read(bytes); (read != (-1)) && (totalRead < totalLen); read=in.read(bytes))
        		{
        			if (read > 0)
        			{
    	    			out.write(bytes, 0, read);
        				totalRead += read;
        			}
        			else if (read == (-1))
        				break;
    
        			if (_log.isTraceLevel())
        				_log.trace("sendRespond(" + fileName + ") copied " + totalRead + " out of " + totalLen);
        		}
      
        		final long	cpyEnd=System.currentTimeMillis(), cpyDuration=cpyEnd - cpyStart; 
        		if (_log.isDebugLevel())
        			_log.debug("sendRespond(" + fileName + ") end (" + cpyDuration + " msec.) " + argVal);
    		} finally {
    		    in.close();
    		}
    	}
    	catch(IOException ioe)
    	{
    		final long	cpyEnd=System.currentTimeMillis(), cpyDuration=cpyEnd - cpyStart; 
			_log.warn("sendRespond(" + fileName + ") " + ioe.getClass().getName() + " after copying " + totalRead + " bytes in " + cpyDuration + " msec.: " + ioe.getMessage(), ioe);
    		throw ioe;
    	}
    	finally
    	{
			out.close();	    
    	}	
	}

	protected String getArgString ()
	{ 
		int length=0;
		try
		{
			length = getContentLength();
		}
		catch(IOException ioe)
		{
			/* ignore */
		}

		return "Mimetype=" + getMimeType()
			+ " VersionId=" + getVersionId()
			+ " Timestamp=" + new Date(getLastModified())
			+ " Length=" + length
			; 
	}

	/*
	 * @see jnlp.sample.servlet.download.DownloadResponse#clone()
	 */
	@Override
	public FileDownloadResponse /* co-variant return */ clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
}