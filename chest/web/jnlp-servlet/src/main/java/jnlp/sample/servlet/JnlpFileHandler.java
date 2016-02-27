/*
 * @(#)JnlpFileHandler.java	1.12 05/11/17
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

package jnlp.sample.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jnlp.sample.servlet.download.DownloadRequest;
import jnlp.sample.servlet.download.DownloadResponse;
import jnlp.sample.util.ObjectUtil;
import jnlp.sample.util.log.Logger;
import jnlp.sample.util.log.LoggerFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* The JNLP file handler implements a class that keeps
 * track of JNLP files and their specializations
 */
public class JnlpFileHandler {
    public static final String JNLP_MIME_TYPE = "application/x-java-jnlp-file";
    public static final String HEADER_LASTMOD = "Last-Modified";
    
    private final ServletContext _servletContext;
    public final ServletContext getServletContext ()
    {
    	return _servletContext;
    }

    private final transient Logger _log;
    
    public static class JnlpFileEntry implements Cloneable {
        // Response
        private DownloadResponse _response;
        public DownloadResponse getResponse () { return _response; }
        public void setResponse (DownloadResponse r) { _response = r; }

        // Keeps track of cache is out of date
        private long   _lastModified;
        public long getLastModified () { return _lastModified; }
        public void setLastModified (long m) { _lastModified = m; }
        
        // Constructor
        public JnlpFileEntry (DownloadResponse response, long lastmodfied)
        {
            _response = response;
            _lastModified = lastmodfied;
        }

        public JnlpFileEntry ()
        {
        	this(null, 0L);
        }
		/*
		 * @see java.lang.Object#clone()
		 */
		@Override
		public JnlpFileEntry /* co-variant return */ clone () throws CloneNotSupportedException
		{
			return getClass().cast(super.clone());
		}
    }

    private Map<String,JnlpFileEntry> _jnlpFiles;
    protected Map<String,JnlpFileEntry> getEntriesMap ()
    {
    	return _jnlpFiles;
    }
    // CAVEAT EMPTOR
    protected void setEntriesMap (Map<String,JnlpFileEntry> m)
    {
    	_jnlpFiles = m;
    }

    protected JnlpFileEntry findEntry (final String reqUrl)
    {
    	if ((null == reqUrl) || (reqUrl.length() <= 0))
    		return null;

    	final Map<String,JnlpFileEntry>	em=getEntriesMap();
    	if (null == em)
    		return null;
       	/*
    	 * NOTE !!! we run the risk of same request being re-updated, but better
    	 * 		to lock the instance as shortly as possible
    	 */
    	synchronized(em)
    	{
    		return em.get(reqUrl);
    	}
    }

    protected JnlpFileEntry removeEntry (final String reqUrl)
    {
    	if ((null == reqUrl) || (reqUrl.length() <= 0))
    		return null;

    	final Map<String,JnlpFileEntry>	em=getEntriesMap();
    	if (null == em)
    		return null;

    	synchronized(em)
    	{
    		return em.remove(reqUrl);
    	}
    }

    protected JnlpFileEntry updateEntry (final String reqUrl, final JnlpFileEntry entry)
    {
    	if ((null == reqUrl) || (reqUrl.length() <= 0) || (null == entry))
    		return null;

    	final Map<String,JnlpFileEntry>	em=getEntriesMap();
    	if (null == em)
    		return null;

    	synchronized(em)
    	{
   			return em.put(reqUrl, entry);
    	}
    }
    /* Initialize JnlpFileHandler for the specific ServletContext */
    public JnlpFileHandler (ServletContext servletContext)
    {
        _servletContext = servletContext;
        _log = LoggerFactory.getLogger(JnlpFileHandler.class);
        _jnlpFiles = new TreeMap<String,JnlpFileEntry>(String.CASE_INSENSITIVE_ORDER);
    }
    /**
     * Result of reading in-memory a JNLP file data - also represented
     * as a {@link java.util.Map.Entry} "pair" where key=data {@link String} and
     * value=timestamp {@link Long}
     * 
     * @author Lyor G.
     * @since Feb 24, 2009 9:45:58 AM
     */
    public static class ReadResult implements Map.Entry<String,Long> {
    	private String	_jnlpFileData;
		public String getJnlpFileData ()
		{
			return _jnlpFileData;
		}

		public void setJnlpFileData (String jnlpFileData)
		{
			_jnlpFileData = jnlpFileData;
		}

		private Long	_timestamp;
		public Long getTimestamp ()
		{
			return _timestamp;
		}

		public void setTimestamp (Long timestamp)
		{
			_timestamp = timestamp;
		}

		public void setTimestamp (long ts)
		{
			setTimestamp(Long.valueOf(ts));
		}

		public ReadResult (String data, Long ts)
		{
			_jnlpFileData = data;
			_timestamp = ts;
		}

		public ReadResult (String data, long ts)
		{
			this(data, Long.valueOf(ts));
		}

		public ReadResult ()
		{
			this(null, null);
		}
		/*
		 * @see java.util.Map.Entry#getKey()
		 */
		@Override
		public String getKey ()
		{
			return getJnlpFileData();
		}
		/*
		 * @see java.util.Map.Entry#getValue()
		 */
		@Override
		public Long getValue ()
		{
			return getTimestamp();
		}
		/*
		 * @see java.util.Map.Entry#setValue(java.lang.Object)
		 */
		@Override
		public Long setValue (Long value)
		{
			final Long	prev=getTimestamp();
			setTimestamp(value);
			return prev;
		}
    }

    protected ReadResult readJnlpFile (final URL resource, final long lastModified) throws IOException
    {
    	if (null == resource)
    		throw new FileNotFoundException("readJnlpFile() no resource specified");

    	final String			path=resource.getPath();
    	final URLConnection		conn=resource.openConnection();
        final int				resLen=conn.getContentLength();
        final StringBuilder		jnlpFileTemplate=new StringBuilder(Math.max(resLen,Byte.MAX_VALUE));
        BufferedReader			br=null;
        InputStream				ins=null;
    	long 					timeStamp=lastModified;
    	try
    	{
    		ins = ObjectUtil.openResource(resource);
    		br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));

    		String line=br.readLine();
	    	if ((line != null) && line.startsWith("TS:"))
	    	{
	    		timeStamp = parseTimeStamp(line.substring(3));
	    		if (_log.isDebugLevel())
	    			_log.debug("readJnlpFile(" + path + ") Timestamp: " + timeStamp + " " + new Date(timeStamp));
	    		if (timeStamp == 0L)
	    		{		
	    			_log.warn("servlet.log.warning.notimestamp", path);
	    			timeStamp = lastModified;
	    		}	    

	    		line = br.readLine();   
	    	}
	
	    	while(line != null)
	    	{
	    		jnlpFileTemplate.append(line);
	    		line = br.readLine();
	    	}
    	}
    	finally
    	{
    		if (br != null)
    			br.close();

    		if (ins != null)
    		{
    			try
    			{
    				ins.close();
    			}
    			catch(IOException ioe)
    			{
    				// ignored
    			}
    		}
    	}

    	return new ReadResult(jnlpFileTemplate.toString(), timeStamp);
    }

    protected String xlateJnlpTemplate (DownloadRequest dreq, String path, String tmplContent) throws IOException
    {
    	final HttpServletRequest	req=(null == dreq) ? null : dreq.getHttpRequest();
    	if (null == req)
    		throw new StreamCorruptedException("xlateJnlpTemplate(" + path + ") no " + HttpServletRequest.class.getSimpleName());

    	return specializeJnlpTemplate(req, path, tmplContent);
    }

    public DownloadResponse getJnlpFile (JnlpResource jnlpres, DownloadRequest dreq) throws IOException
    {		
    	if ((null == jnlpres) || (null == dreq))
    		throw new StreamCorruptedException("getJnlpFile(" + jnlpres + ")[" + dreq + "] missing parameters");

    	final String	path=jnlpres.getPath();
    	final URL		resource=jnlpres.getResource();		
    	long 			lastModified=jnlpres.getLastModified();	
	
    	if (_log.isDebugLevel())
    		_log.debug("getJnlpFile(" + path + ") lastModified: " + lastModified + " " + new Date(lastModified));
    	if (lastModified == 0L)
    		_log.warn("servlet.log.warning.nolastmodified", path);
	
    	// fix for 4474854:  use the request URL as key to look up jnlp file
    	// in hash map
        @SuppressWarnings("deprecation")
        final String reqUrl=javax.servlet.http.HttpUtils.getRequestURL(dreq.getHttpRequest()).toString();

    	// Check if entry already exist in Map
    	JnlpFileEntry jnlpFile=findEntry(reqUrl);
	    // Entry found in cache, so return it
    	if ((jnlpFile != null) && (jnlpFile.getLastModified() == lastModified))
    		return jnlpFile.getResponse();   
 
    	// Read information from WAR file
    	final ServletContext	ctx=getServletContext();
    	String mimeType=(null == ctx) ? null : ctx.getMimeType(path);
    	if ((mimeType == null) || (mimeType.length() <= 0))
    		mimeType = JNLP_MIME_TYPE;

    	final ReadResult		rr=readJnlpFile(resource, lastModified);
    	final Long				tsv=(null == rr) ? null : rr.getTimestamp();
    	final long				timeStamp=(null == tsv) ? 0L : tsv.longValue();
    	final String			tmplContent=(null == rr) ? null : rr.getJnlpFileData(),
    							retVerId=jnlpres.getReturnVersionId(),
    							jnlpFileContent=xlateJnlpTemplate(dreq, path, tmplContent);
    	final DownloadResponse	resp;
    	if (jnlpFileContent != tmplContent)	// check if had to translate
    	{
    		// Convert to bytes as a UTF-8 encoding
    		final byte[] byteContent=jnlpFileContent.getBytes("UTF-8");
    		resp = DownloadResponse.getFileDownloadResponse(byteContent, mimeType, timeStamp, retVerId);
    	}
    	else	// no translation was necessary
    	{
   			resp = DownloadResponse.getFileDownloadResponse(resource, mimeType, timeStamp, retVerId);
    	}

    	jnlpFile = new JnlpFileEntry(resp, timeStamp);

    	final JnlpFileEntry	prev=updateEntry(reqUrl, jnlpFile);
    	if (prev != null)
    	{
    		if (_log.isInformationalLevel())
    			_log.info("getJnlpFile(" + path + ") re-mapped " + reqUrl);
    	}
    	else if (_log.isDebugLevel())
			_log.debug("getJnlpFile(" + path + ") mapped " + reqUrl);

    	return resp;
    }

    protected ReadResult readJnlpFileEx (final URL resource, final long lastModified) throws IOException
    {
    	if (null == resource)
    		throw new FileNotFoundException("readJnlpFileEx() no resource specified");

    	final String			path=resource.getPath();
        final URLConnection 	conn=resource.openConnection();
        final int				resLen=conn.getContentLength();
        final StringBuilder		jnlpFileTemplate=new StringBuilder(Math.max(resLen,Byte.MAX_VALUE));
        BufferedReader			br=null;
        InputStream				ins=null;
        long 					timeStamp=lastModified;
        try
        {
    		ins = ObjectUtil.openResource(resource);
        	br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));

        	String line=br.readLine();
        	if ((line != null) && line.startsWith("TS:"))
        	{
        		timeStamp = parseTimeStamp(line.substring(3));
        		if (_log.isDebugLevel())
        			_log.debug("readJnlpFileEx(" + path + ") Timestamp: " + timeStamp + " " + new Date(timeStamp));
        		if (timeStamp == 0L)
        		{
        			_log.warn("servlet.log.warning.notimestamp", path);
        			timeStamp = lastModified;
        		}
        		line = br.readLine();
        	}
        
        	while(line != null)
        	{
        		jnlpFileTemplate.append(line);
        		line = br.readLine();
        	}
        }
        finally
        {
        	if (br != null)
        		br.close();

    		if (ins != null)
    		{
    			try
    			{
    				ins.close();
    			}
    			catch(IOException ioe)
    			{
    				// ignored
    			}
    		}
        }

    	return new ReadResult(jnlpFileTemplate.toString(), timeStamp);
    }

    protected ReadResult transformJnlpFileEx (final String path, final String query, final String testJRE, final ReadResult rr) throws IOException
    {
    	String		jnlpFileContent=(null == rr) ? null : rr.getJnlpFileData();
    	final int	contentLength=(null == jnlpFileContent) ? 0 : jnlpFileContent.length();
    	if (contentLength <= 0)
    		throw new StreamCorruptedException("transformJnlpFileEx(" + path + ") no initial data");

    	Long						tsv=(null == rr) ? null : rr.getTimestamp();
        final byte[]				cb=jnlpFileContent.getBytes("UTF-8");
        final ByteArrayInputStream	bis=new ByteArrayInputStream(cb);
        try
        {
        	final DocumentBuilderFactory	factory=DocumentBuilderFactory.newInstance();
            final DocumentBuilder			builder=factory.newDocumentBuilder();
            final Document					document=builder.parse(bis);
            if ((null == document) || (document.getNodeType() != Node.DOCUMENT_NODE))
            	throw new DOMException(DOMException.SYNTAX_ERR, "Parsing did not yield a " + Document.class.getSimpleName());

            boolean modified = false;
            Element root = document.getDocumentElement();
            if (root.hasAttribute("href"))
            {
            	String href = root.getAttribute("href");
            	root.setAttribute("href", href + "?" + query);
            	modified = true;
            }
            // Update version value for j2se tag
            if ((testJRE != null) && (testJRE.length() > 0))
            {
            	NodeList j2seNL=root.getElementsByTagName("j2se");
            	if ((j2seNL != null) && (j2seNL.getLength() > 0))
            	{
            		final Element j2se=(Element) j2seNL.item(0);
            		String ver = j2se.getAttribute("version");
            		if ((ver != null) && (ver.length() > 0))
            		{
            			j2se.setAttribute("version", testJRE);
            			modified = true;
            		}
            	}
            }

            final TransformerFactory	tFactory=TransformerFactory.newInstance();
            final Transformer			transformer=tFactory.newTransformer();
            final DOMSource				source=new DOMSource(document);
            final StringWriter			sw=new StringWriter(contentLength);
            final StreamResult			result=new StreamResult(sw);
            transformer.transform(source, result);

            jnlpFileContent=sw.toString();
            if (_log.isDebugLevel())
            	_log.debug("transformJnlpFileEx(" + path + ") converted jnlpFileContent: " + jnlpFileContent);
            // Since we modified the file on the fly, we always update the timestamp value with current time
            if (modified)
            {
            	tsv = Long.valueOf(System.currentTimeMillis());
            	if (_log.isDebugLevel())
            		_log.debug("transformJnlpFileEx(" + path + ") Last modified on the fly:  " + tsv);
            }
        }
        catch(Exception e)
        {
        	final String	msg="transformJnlpFileEx(" + path + ") " + e.getClass().getName() + ": " + e.getMessage();
        	_log.warn(msg, e);
        	if (e instanceof IOException)
        		throw (IOException) e;
        	else
        		throw new StreamCorruptedException(msg);
		}
        finally
        {
        	bis.close();
        }

        return new ReadResult(jnlpFileContent, tsv);
    }

    	/* Main method to lookup an entry (NEW for JavaWebStart 1.5+) */
    public DownloadResponse getJnlpFileEx (JnlpResource jnlpres, DownloadRequest dreq) throws IOException
    {
    	if ((null == jnlpres) || (null == dreq))
    		throw new StreamCorruptedException("getJnlpFileEx(" + jnlpres + ")[" + dreq + "] missing parameters");

    	final String	path=jnlpres.getPath();
        final URL		resource=jnlpres.getResource();
        final long		lastModified=jnlpres.getLastModified();

        if (_log.isDebugLevel())
        	_log.debug("getJnlpFileEx(" + path + ") lastModified: " + lastModified + " " + new Date(lastModified));
        if (lastModified == 0L)
            _log.warn("servlet.log.warning.nolastmodified", path);
        
        // fix for 4474854:  use the request URL as key to look up jnlp file
        // in hash map
        @SuppressWarnings("deprecation")
        String reqUrl = javax.servlet.http.HttpUtils.getRequestURL(dreq.getHttpRequest()).toString();
        // SQE: To support query string, we changed the hash key from Request URL to (Request URL + query string)
        final String	qryString=dreq.getQuery();
        if ((qryString != null) && (qryString.length() > 0))
            reqUrl += qryString;
        
        // Check if entry already exist in Map
        JnlpFileEntry jnlpFile=findEntry(reqUrl);
    	/*
    	 * NOTE !!! we run the risk of same request being re-updated, but better
    	 * 		to lock the instance as shortly as possible
    	 */
        if ((jnlpFile != null) && (jnlpFile.getLastModified() == lastModified))
            return jnlpFile.getResponse();
        
        // Read information from WAR file
        final ServletContext	ctx=getServletContext();
        String mimeType=(null == ctx) ? null : ctx.getMimeType(path);
        if ((mimeType == null) || (mimeType.length() <= 0))
        	mimeType = JNLP_MIME_TYPE;

		ReadResult	rr=readJnlpFileEx(resource, lastModified);
		if (null == rr)
			throw new StreamCorruptedException("getJnlpFileEx(" + path + ") no data read");

		final String	tmplContent=xlateJnlpTemplate(dreq, path, rr.getJnlpFileData());
        /* SQE: We need to add query string back to href in jnlp file. We also need to handle JRE requirement for
         * the test. We reconstruct the xml DOM object, modify the value, then regenerate the jnlpFileContent.
         */
        final String query=dreq.getQuery(), testJRE=dreq.getTestJRE();
        // For backward compatibility: Always check if the href value exists.
        // Bug 4939273: We will retain the jnlp template structure and will NOT add href value. Above old
        // approach to always check href value caused some test case not run.
        if ((query != null) && (query.length() > 0))
        {
            if (_log.isDebugLevel())
            	_log.debug("getJnlpFileEx(" + path + ") double check query string: " + query);

            rr.setJnlpFileData(tmplContent);
            if (null == (rr=transformJnlpFileEx(path, query, testJRE, rr)))
            	throw new StreamCorruptedException("getJnlpFileEx(" + path + ") no translation result returned");
        }

    	final Long				tsv=rr.getTimestamp();
    	final long				timeStamp=(null == tsv) ? 0L : tsv.longValue();
    	final String			jnlpFileContent=rr.getJnlpFileData(),
    							retVerId=jnlpres.getReturnVersionId();
        final DownloadResponse	resp;
    	if (jnlpFileContent != tmplContent)	// check if had to translate
    	{
    		// Convert to bytes as a UTF-8 encoding
    		final byte[] byteContent=jnlpFileContent.getBytes("UTF-8");
    		resp = DownloadResponse.getFileDownloadResponse(byteContent, mimeType, timeStamp, retVerId);
    	}
    	else	// no translation was necessary
    	{
   			resp = DownloadResponse.getFileDownloadResponse(resource, mimeType, timeStamp, retVerId);
    	}

    	jnlpFile = new JnlpFileEntry(resp, timeStamp);

    	final JnlpFileEntry	prev=updateEntry(reqUrl, jnlpFile);
    	if (prev != null)
    	{
    		if (_log.isInformationalLevel())
    			_log.info("getJnlpFileEx(" + path + ") re-mapped " + reqUrl);
    	}
    	else if (_log.isDebugLevel())
			_log.debug("getJnlpFileEx(" + path + ") mapped " + reqUrl);
        
        return resp;
    }
    
    public static final String	PLACEHOLDER_PREFIX="$$",
    							NAME_PLACEHOLDER=PLACEHOLDER_PREFIX + "name",
    							HOST_PLACEHOLDER=PLACEHOLDER_PREFIX + "hostname",
    							CODEBASE_PLACEHOLDER=PLACEHOLDER_PREFIX + "codebase",
    							CONTEXT_PLACEHOLDER=PLACEHOLDER_PREFIX + "context",
    							SITE_PLACEHOLDER=PLACEHOLDER_PREFIX + "site";
    /* This method performs the following substitutions
     *  $$name
     *  $$hostname
     *  $$codebase
     *  $$context
     *  $$site
     */
    protected static String specializeJnlpTemplate (final HttpServletRequest request, final String respath, final String orgTemplate)
    {
    	String			jnlpTemplate=orgTemplate;
        final String 	urlprefix=getUrlPrefix(request), ctxPath=request.getContextPath();
        final int		idx=respath.lastIndexOf('/'); //
        final String	name=respath.substring(idx + 1),    // Exclude /
        				codebase=respath.substring(0, idx + 1), // Include /
        				hostName=request.getServerName();
        jnlpTemplate = substitute(jnlpTemplate, NAME_PLACEHOLDER,  name);
        // fix for 5039951: Add $$hostname macro
        jnlpTemplate = substitute(jnlpTemplate, HOST_PLACEHOLDER, hostName);
        jnlpTemplate = substitute(jnlpTemplate, CODEBASE_PLACEHOLDER,  urlprefix + ctxPath + codebase);
        jnlpTemplate = substitute(jnlpTemplate, CONTEXT_PLACEHOLDER, urlprefix + ctxPath);
        // fix for 6256326: add $$site macro to sample jnlp servlet
        jnlpTemplate = substitute(jnlpTemplate, SITE_PLACEHOLDER, urlprefix);
        return jnlpTemplate;
    }
    
    // This code is heavily inspired by the stuff in HttpUtils.getRequestURL
    public static String getUrlPrefix (HttpServletRequest req)
    {
        final String 		scheme=req.getScheme();
        final int 			port=req.getServerPort();
        final StringBuilder url=new StringBuilder(128)
        								.append(scheme)
        								.append("://")
        								.append(req.getServerName())
        								;

        if (("http".equalsIgnoreCase(scheme) && (port != 80))
	     || ("https".equalsIgnoreCase(scheme) && (port != 443)))
            url.append(':')
            	.append(req.getServerPort())
            	;

        return url.toString();
    }
    
    public static String substitute (final String org, String key, String value)
    {
    	String	target=org;
        int start = 0;
        do
        {
            int idx=target.indexOf(key, start);
            if (idx < 0)
            	return target;
            target = target.substring(0, idx) + value + target.substring(idx + key.length());
            start = idx + value.length();
        } while(true);
    }
    /* Parses a ISO 8601 Timestamp. The format of the timestamp is:
     *
     *   YYYY-MM-DD hh:mm:ss  or   YYYYMMDDhhmmss
     *
     * Hours (hh) is in 24h format. ss are optional. Time are by default relative
     * to the current timezone. Timezone information can be specified
     * by:
     *
     *    - Appending a 'Z', e.g., 2001-12-19 12:00Z
     *    - Appending +hh:mm, +hhmm, +hh, -hh:mm -hhmm, -hh to
     *      indicate that the locale timezone used is either the specified
     *      amount before or after GMT. For example,
     *
     *           12:00Z = 13:00+1:00 = 0700-0500
     *
     *  The method returns 0 if it cannot pass the string. Otherwise, it is
     *  the number of milliseconds size sometime in 1969.
     */
    protected static long parseTimeStamp (final String s)
    {
        int		YYYY = 0, MM = 0, DD = 0, hh = 0, mm = 0,  ss = 0;
        String	timestamp=(null == s) ? null : s.trim();
        if ((null == timestamp) || (timestamp.length() <= 0))
        	return 0L;

        try
        {
            // Check what format is used
            if (matchPattern("####-##-## ##:##", timestamp))
            {
                YYYY = getIntValue(timestamp, 0, 4);
                MM = getIntValue(timestamp, 5, 7);
                DD = getIntValue(timestamp, 8, 10);
                hh = getIntValue(timestamp, 11, 13);
                mm = getIntValue(timestamp, 14, 16);
                timestamp = timestamp.substring(16);
                if (matchPattern(":##", timestamp))
                {
                    ss = getIntValue(timestamp, 1, 3);
                    timestamp = timestamp.substring(3);
                }
            }
            else if (matchPattern("############", timestamp))
            {
                YYYY = getIntValue(timestamp, 0, 4);
                MM = getIntValue(timestamp, 4, 6);
                DD = getIntValue(timestamp, 6, 8);
                hh = getIntValue(timestamp, 8, 10);
                mm = getIntValue(timestamp, 10, 12);
                timestamp = timestamp.substring(12);
                if (matchPattern("##", timestamp)) {
                    ss = getIntValue(timestamp, 0, 2);
                    timestamp = timestamp.substring(2);
                }
            }
            else
            {
                // Unknown format
                return 0L;
            }
        }
        catch(NumberFormatException e)
        {
            // Bad number
            return 0L;
        }
        
        // Remove timezone information
        timestamp = timestamp.trim();
        final String timezone;
        if (timestamp.equalsIgnoreCase("Z"))
            timezone ="GMT";
        else if (timestamp.startsWith("+") || timestamp.startsWith("-"))
            timezone = "GMT" + timestamp;
        else
        	timezone = null;
        
        if ((timezone == null) || (timezone.length() <= 0))
        {
            // Date is relative to current locale
            final Calendar cal=Calendar.getInstance();
            cal.set(YYYY, MM - 1, DD, hh, mm, ss);
            return cal.getTimeInMillis();
        }
        else
        {
            // Date is relative to a timezone
            final Calendar cal=Calendar.getInstance(TimeZone.getTimeZone(timezone));
            cal.set(YYYY, MM - 1, DD, hh, mm, ss);
            return cal.getTimeInMillis();
        }
    }
    
    protected static int getIntValue (String key, int start, int end)
    {
        return Integer.parseInt(key.substring(start, end));
    }
    
    protected static boolean matchPattern (CharSequence pattern, CharSequence key)
    {
        // Key must be longer than pattern
        if ((null == key) || (null == pattern) || (key.length() < pattern.length()))
        	return false;

        for (int i = 0; i < pattern.length(); i++)
        {
            final char format = pattern.charAt(i), ch = key.charAt(i);
            if (!(((format == '#') && Character.isDigit(ch)) || (format == ch)))
                return false;
        }

        return true;
    }
}


