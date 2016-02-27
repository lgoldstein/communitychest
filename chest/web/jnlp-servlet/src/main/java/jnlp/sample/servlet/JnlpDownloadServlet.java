/*
 * @(#)JnlpDownloadServlet.java	1.10 07/03/15
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jnlp.sample.servlet.download.DownloadRequest;
import jnlp.sample.servlet.download.DownloadResponse;
import jnlp.sample.util.VersionID;
import jnlp.sample.util.log.Logger;
import jnlp.sample.util.log.LoggerFactory;

/**
 * This Servlet class is an implementation of JNLP Specification's
 * Download Protocols.
 *
 * All requests to this servlet is in the form of HTTP GET commands.
 * The parameters that are needed are:
 * <ul>
 * <li><code>arch</code>,
 * <li><code>os</code>,
 * <li><code>locale</code>,
 * <li><code>version-id</code> or <code>platform-version-id</code>,
 * <li><code>current-version-id</code>,
 * <li><code>known-platforms</code>
 * </ul>
 * <p>
 *
 * @version 1.8 01/23/03
 * @author Lyor G. <P>Most important modifications:</P></BR>
 * 		<UL>
 * 			<LI>
 * 			Using JDK 1.5 code
 * 			</LI>
 * 
 * 			<LI>
 * 			Promoted/added <code>protected/public</code> methods
 * 			in order to allow modification to default behavior
 * 			</LI>
 * 
 * 			<LI>
 * 			Generalized the {@link Logger} class to allow plugging
 * 			in other implementations (e.g., <a href="http://logging.apache.org/log4j/1.2/index.html">log4j</a>).
 * 			</LI>
 * 		</UL>
 */
public class JnlpDownloadServlet extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4044215921501234000L;
	public JnlpDownloadServlet ()
    {
    	super();
    }

    // Localization
    private static ResourceBundle  _defaultResourceBundle=null; 
    public static final synchronized ResourceBundle getDefaultResourceBundle ()
    {
    	if (_defaultResourceBundle == null)
    		_defaultResourceBundle = ResourceBundle.getBundle("jnlp/sample/servlet/resources/strings");
    	return _defaultResourceBundle;
    }
    
    // Servlet configuration
    public static final String PARAM_JNLP_EXTENSION    = "jnlp-extension";    
    public static final String PARAM_JAR_EXTENSION     = "jar-extension";    
    
    // Servlet configuration
    private transient Logger _log;

    protected ResourceCatalog createResourceCatalog (ServletContext ctx)
    {
    	return (null == ctx) ? null : new ResourceCatalog(ctx);
    }

    private transient ResourceCatalog _resourceCatalog;
    protected synchronized ResourceCatalog getResourceCatalog (boolean createIfNotExist)
    {
    	if ((null == _resourceCatalog) && createIfNotExist)
            _resourceCatalog = createResourceCatalog(getServletContext());		

    	return _resourceCatalog;
    }

    public ResourceCatalog getResourceCatalog ()
    {
    	return getResourceCatalog(true);
    }

    public synchronized void setResourceCatalog (ResourceCatalog c)
    {
    	_resourceCatalog = c;
    }

    private ResourceBundle	_resourceBundle;
    protected synchronized ResourceBundle getResourceBundle (boolean createIfNotExist)
    {
    	if ((null == _resourceBundle) && createIfNotExist)
    		_resourceBundle = getDefaultResourceBundle();
    	return _resourceBundle;
    }

    public synchronized ResourceBundle getResourceBundle ()
    {
    	return getResourceBundle(true);
    }

    public synchronized void setResourceBundle (ResourceBundle b)
    {
    	_resourceBundle = b;
    }

    protected JnlpFileHandler createJnlpFileHandler (ServletContext ctx)
    {
    	return (null == ctx) ? null : new JnlpFileHandler(ctx);
    }

    private transient JnlpFileHandler _jnlpFileHandler;
	protected synchronized JnlpFileHandler getJnlpFileHandler (boolean createIfNotExist)
	{
		if ((null == _jnlpFileHandler) && createIfNotExist)
			_jnlpFileHandler = createJnlpFileHandler(getServletContext());
		return _jnlpFileHandler;
	}

	public synchronized JnlpFileHandler getJnlpFileHandler ()
	{
		return getJnlpFileHandler(true);
	}

	public synchronized void setJnlpFileHandler (JnlpFileHandler jnlpFileHandler)
	{
		_jnlpFileHandler = jnlpFileHandler;
	}

	protected JarDiffHandler createJarDiffHandler (ServletContext ctx)
	{
		return (null == ctx) ? null : new JarDiffHandler(ctx);
	}

    private transient JarDiffHandler  _jarDiffHandler;
	protected synchronized JarDiffHandler getJarDiffHandler (boolean createIfNotExist)
	{
		if ((null == _jarDiffHandler) && createIfNotExist)
			_jarDiffHandler = createJarDiffHandler(getServletContext());
		return _jarDiffHandler;
	}

	public synchronized JarDiffHandler getJarDiffHandler ()
	{
		return getJarDiffHandler(true);
	}

	public synchronized void setJarDiffHandler (JarDiffHandler jarDiffHandler)
	{
		_jarDiffHandler = jarDiffHandler;
	}
	/*
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
	public void init (ServletConfig config) throws ServletException
	{
        super.init(config);
        LoggerFactory.initLogger(config, getResourceBundle());

        // Setup logging
        _log = LoggerFactory.getLogger(JnlpDownloadServlet.class);
        if (_log.isInformationalLevel())
        	_log.info("Initializing...");
	
        // Get extension from Servlet configuration, or use default	
        JnlpResource.setDefaultExtensions(
        		config.getInitParameter(PARAM_JNLP_EXTENSION), 
        		config.getInitParameter(PARAM_JAR_EXTENSION));		
	
        if (_log.isInformationalLevel())
        	_log.info("Initialized...");
    }
    /* Make sure that it is a valid request. This is also the place to implement the
     *  reverse IP lookup
     */
    protected void validateRequest (final DownloadRequest dreq) throws ErrorResponseException
    {
    	final String path=(null == dreq) ? null : dreq.getPath();
    	if ((null == path) || (path.length() <= 0)
    	 || path.endsWith(ResourceCatalog.VERSION_XML_FILENAME)
    	 || (path.indexOf(ResourceCatalog.PATH_PREFIX_SEP) >= 0))
    		throw new ErrorResponseException(DownloadResponse.getNoContentResponse());	
    }

    protected JnlpResource getBasicResource (final DownloadRequest dreq, final String reqPath) throws IOException
    {
    	if (null == dreq)
    		throw new StreamCorruptedException("getBasicResource(" + reqPath + ") no " + DownloadRequest.class.getSimpleName() + " instance");
    	if ((null == reqPath) || (reqPath.length() <= 0))
    		throw new StreamCorruptedException("getBasicResource(" + dreq + ") no path");

    	return new JnlpResource(getServletContext(), reqPath);
    }

    protected JnlpResource handleBasicDownload (final DownloadRequest dreq) throws ErrorResponseException, IOException
    {
    	if (null == dreq)
    		throw new StreamCorruptedException("No " + DownloadRequest.class.getSimpleName() + " instance");

    	final String	reqPath=dreq.getPath();
    	if (_log.isDebugLevel())
    		_log.debug("handleBasicDownload(" + reqPath + ")");

    	// do not return directory names for basic protocol
    	if ((reqPath == null) || (reqPath.length() <= 0) || reqPath.endsWith("/"))
    		throw new ErrorResponseException(DownloadResponse.getNoContentResponse());

    	// Lookup resource	
    	final JnlpResource jnlpres=getBasicResource(dreq, reqPath);
    	if (!jnlpres.exists())		    
    		throw new ErrorResponseException(DownloadResponse.getNoContentResponse());
			
    	return jnlpres;
    }

    protected JnlpResource handleVersionRequest (final DownloadRequest dreq, final String reqVersion) throws IOException, ErrorResponseException
    {	
    	final String	reqPath=dreq.getPath();
    	if (_log.isDebugLevel())
    		_log.debug("handleVersionRequest(" + reqPath + ")[" + reqVersion + "]");

    	final ResourceCatalog	c=getResourceCatalog();
    	if (null == c)
    		throw new FileNotFoundException("No " + ResourceCatalog.class.getSimpleName() + " instance available");

    	return c.lookupResource(dreq, reqVersion);	
    }
    /* Interprets the download request and convert it into a resource that is
     *  part of the Web Archive.
     */
    protected JnlpResource locateResource (final DownloadRequest dreq) throws IOException, ErrorResponseException
    {    
    	final String	ver=dreq.getVersion();
    	if ((ver == null) || (ver.length() <= 0))	   
    		return handleBasicDownload(dreq);	
    	else
    		return handleVersionRequest(dreq, ver);
    }

    protected DownloadResponse constructJnlpResponse (final JnlpResource jnlpres, final DownloadRequest dreq) throws IOException
    {
    	if ((null == jnlpres) || (null == dreq))
    		throw new StreamCorruptedException("constructJnlpResponse(" + jnlpres + ")[" + dreq + "] missing parameters");

    	final String	path=jnlpres.getPath();
    	final boolean	supportQuery=JarDiffHandler.isJavawsVersion(dreq, "1.5+");
    	if (_log.isDebugLevel())
    		_log.debug("constructJnlpResponse(" + path + ") SupportQuery in Href=" + supportQuery);

		// only support query string in href for 1.5 and above
    	final JnlpFileHandler	fh=getJnlpFileHandler();
		if (supportQuery)
			return fh.getJnlpFileEx(jnlpres, dreq);
		else
			return fh.getJnlpFile(jnlpres, dreq);
    }

    protected DownloadResponse constructJarDiffResponse (final JnlpResource jnlpres, final DownloadRequest dreq) throws IOException
    {
    	if ((null == jnlpres) || (null == dreq))
    		throw new StreamCorruptedException("constructJarDiffResponse(" + jnlpres + ")[" + dreq + "] missing parameters");

    	final String	path=jnlpres.getPath();
    	if (_log.isDebugLevel())
    		_log.debug("constructJarDiffResponse(" + path + ")");

    	final JarDiffHandler	jdh=getJarDiffHandler();
		final DownloadResponse	response=
			jdh.getJarDiffEntry(getResourceCatalog(), dreq, jnlpres);
		if ((response != null) && _log.isInformationalLevel())
			_log.info("servlet.log.info.jardiff.response", path);

		return response;	    
    }

    protected DownloadResponse constructDefaultResponse (final JnlpResource jnlpres, final DownloadRequest dreq) throws IOException
    {
    	if ((null == jnlpres) || (null == dreq))
    		throw new StreamCorruptedException("constructDefaultResponse(" + jnlpres + ")[" + dreq + "] missing parameters");

    	final String 		path=jnlpres.getPath();
    	// check and see if we can use pack resource
    	final JnlpResource	jr=new JnlpResource(getServletContext(), 
				    						    jnlpres.getName(),
				    						    jnlpres.getVersionId(),
				    						    jnlpres.getOSList(),
				    						    jnlpres.getArchList(),
				    						    jnlpres.getLocaleList(),
				    						    jnlpres.getPath(),
				    						    jnlpres.getReturnVersionId(),
				    						    dreq.getEncoding());
    	if (_log.isDebugLevel())
    		_log.debug("constructResponse(" + path + ") Real resource returned=" + jr);

    	// Return WAR file resource
    	return DownloadResponse.getFileDownloadResponse(jr.getResource(), 
    													jr.getMimeType(), 
    													jr.getLastModified(), 
    													jr.getReturnVersionId());	    	
    }
    /* Given a DownloadPath and a DownloadRequest, it constructs the data stream to return
     *  to the requester
     */
    protected DownloadResponse constructResponse (final JnlpResource jnlpres, final DownloadRequest dreq) throws IOException
    {
    	if ((null == jnlpres) || (null == dreq))
    		throw new StreamCorruptedException("constructResponse(" + jnlpres + ")[" + dreq + "] missing parameters");

    	// If it is a JNLP file then need to be macro-expanded, so it is handled differently
    	if (jnlpres.isJnlpFile())
    		return constructJnlpResponse(jnlpres, dreq);
	
    	// Check if a JARDiff can be returned
    	final String	ver=dreq.getCurrentVersionId();
    	if ((ver != null) && (ver.length() > 0) && jnlpres.isJarFile())
    		return constructJarDiffResponse(jnlpres, dreq);

    	return constructDefaultResponse(jnlpres, dreq);
    }

    public static final int compareVersions (final String versionId, final String resVersion)
    {
    	if (versionId == resVersion)
    		return 0;

    	if ((null == versionId) || (versionId.length() <= 0))
    	{
    		if ((null == resVersion) || (resVersion.length() <= 0))
    			return 0;
    		else
    			return (+1);	// nulls come last
    	}
    	else if ((null == resVersion) || (resVersion.length() <= 0))
    		return (-1);

    	final VersionID	v1=new VersionID(versionId), v2=new VersionID(resVersion);
    	return v1.compareTo(v2);
    }

    // return true if nothing modified
    protected boolean compareVersions (
    		final String versionId, final long ifModifiedSince, final JnlpResource jnlpres)
    {
    	final String	resVersion=(null == jnlpres) ? null : jnlpres.getVersionId();
    	if ((versionId != null) && (versionId.length() > 0)
    	 && (resVersion != null) && (resVersion.length() > 0))
    	{
    		final int	nRes=compareVersions(versionId, resVersion);
    		if (nRes != 0)
    			return false;
    	}

    	// fall through
    	return compareLastModified(null, ifModifiedSince, jnlpres);
    }
    // return true if nothing modified
    protected boolean compareLastModified (
    		final String versionId, final long ifModifiedSince, final JnlpResource jnlpres)
    {
    	if ((versionId != null) && (versionId.length() > 0))
    		return compareVersions(versionId, ifModifiedSince, jnlpres);

    	final long	jnlpModTime=(null == jnlpres) ? 0L : jnlpres.getLastModified();
    	if (_log.isDebugLevel())
    		_log.debug("compareLastModified(" + jnlpres + ") If-modified-since=" + ifModifiedSince + "/JNLP-last-modified=" + jnlpModTime);

    	// We divide the value returned by getLastModified here by 1000
        // because if protocol is HTTP, last 3 digits will always be 
        // zero.  However, if protocol is JNDI, that's not the case.
        // so we divide the value by 1000 to remove the last 3 digits
        // before comparison
    	if ((ifModifiedSince != (-1L))
    	 && (jnlpModTime != 0L)
          && ((ifModifiedSince / 1000L) >= (jnlpModTime / 1000L)))
    		return true;

    	return false;
    }

    protected DownloadResponse constructHeadResponse (final JnlpResource jnlpres, final DownloadRequest dreq) throws IOException
    {
    	if ((null == jnlpres) || (null == dreq))
    		throw new StreamCorruptedException("constructHeadResponse(" + jnlpres + ")[" + dreq + "] missing parameters");

    	final URL			resURL=jnlpres.getResource();
    	final URLConnection	resConn=resURL.openConnection();
        final int			cl=resConn.getContentLength(); 

        // head request response
        return DownloadResponse.getHeadRequestResponse(
                	jnlpres.getMimeType(), jnlpres.getVersionId(), jnlpres.getLastModified(), cl);               
    }

    protected DownloadResponse constructNotModifiedResponse (final JnlpResource jnlpres, final DownloadRequest dreq) throws IOException
    {
    	if ((null == jnlpres) || (null == dreq))
    		throw new StreamCorruptedException("constructNotModifiedResponse(" + jnlpres + ")[" + dreq + "] missing parameters");

    	// return (304) not modified if possible
    	if (_log.isDebugLevel())
    		_log.debug("constructNotModifiedResponse(" + jnlpres + ")[" + dreq + "]");

    	return DownloadResponse.getNotModifiedResponse();
    }

    protected DownloadRequest createDownloadRequest (HttpServletRequest request) throws IOException
    {
    	if (null == request)
    		throw new StreamCorruptedException("createDownloadRequest() no " + HttpServletRequest.class.getSimpleName() + " instance");

    	return new DownloadRequest(getServletContext(), request);
    }

    protected void handleRequest (HttpServletRequest request, HttpServletResponse response, boolean isHead) throws IOException
    {
    	String requestStr=request.getRequestURI(), qryString=request.getQueryString();
    	if ((qryString != null) && (qryString.length() > 0))
    		requestStr += "?" + qryString.trim();
	
    	// Parse HTTP request
    	final DownloadRequest dreq=createDownloadRequest(request);
    	if (_log.isInformationalLevel())
    	{
    		_log.info("servlet.log.info.request",   requestStr);
    		_log.info("servlet.log.info.useragent", request.getHeader("User-Agent"));	    
    	}
    	if (_log.isDebugLevel())
    		_log.debug(dreq.toString());
       
    	final long ifModifiedSince=request.getDateHeader("If-Modified-Since");
    	try
    	{ 
        	// Check if it is a valid request
    		validateRequest(dreq);

    		// Decide what resource to return
    		final JnlpResource jnlpres=locateResource(dreq);
    		if (_log.isDebugLevel())
    			_log.debug("handleRequest(" + requestStr + ") JnlpResource=" + jnlpres);

    		if (_log.isInformationalLevel())
    			_log.info("servlet.log.info.goodrequest", (null == jnlpres) ? null : jnlpres.toString());		
    
            final DownloadResponse dres;
            if (isHead)
            	dres = constructHeadResponse(jnlpres, dreq);
            else if (compareLastModified(dreq.getVersion(), ifModifiedSince, jnlpres))
            	dres = constructNotModifiedResponse(jnlpres, dreq);
            else	// Return selected resource
                dres = constructResponse(jnlpres, dreq);

            if (null == dres)
            	throw new StreamCorruptedException("No " + DownloadResponse.class.getSimpleName() + " generated");

            dres.sendRespond(response);
    	}
    	catch(ErrorResponseException ere)
    	{
    		if (_log.isWarningLevel())	    
    			_log.warn("servlet.log.info.badrequest", requestStr);
    		if (_log.isDebugLevel())
    			_log.debug("Response: "+ ere.toString());

    		// Return response from exception
    		final DownloadResponse	dres=ere.getDownloadResponse();
    		dres.sendRespond(response);
    	}
    	catch(IOException ioe)
    	{
    		_log.warn("handleRequest(" + requestStr + ") " + ioe.getClass().getName() + ": " + ioe.getMessage(), ioe);
    		throw ioe;
    	}
    	catch(Throwable e)
    	{
    		_log.fatal("servlet.log.fatal.internalerror", e);
    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	}
    }
    /*
     * @see javax.servlet.http.HttpServlet#doHead(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
	public void doHead (HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException
    {
        handleRequest(request, response, true);
    }
    /* We handle get requests too - even though the spec. only requires POST requests
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
	public void doGet (HttpServletRequest request, HttpServletResponse response)
    	throws ServletException, IOException
    {
    	handleRequest(request, response, false);
    }
	/*
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost (HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
    	handleRequest(request, response, false);
	}
}
