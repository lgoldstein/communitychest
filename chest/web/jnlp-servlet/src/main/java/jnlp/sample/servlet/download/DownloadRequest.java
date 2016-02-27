/*
 * @(#)DownloadRequest.java	1.7 05/11/17
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
import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import jnlp.sample.util.ObjectUtil;

/** 
 * The DownloadRequest encapsulates all the data in a request
 * SQE: We need to address query string
 */
public class DownloadRequest implements Cloneable {
    // Arguments
	public static final String ARG_ARCH                = "arch";
	public static final String ARG_OS                  = "os";
	public static final String ARG_LOCALE              = "locale";
	public static final String ARG_VERSION_ID          = "version-id";
	public static final String ARG_CURRENT_VERSION_ID  = "current-version-id";
	public static final String ARG_PLATFORM_VERSION_ID = "platform-version-id";
	public static final String ARG_KNOWN_PLATFORMS     = "known-platforms";
	public static final String TEST_JRE = "TestJRE";
    
    private String _path;
    private String _version;
    private String _currentVersionId;
    private String[] _os;
    private String[] _arch;
    private String[] _locale;
    private String[] _knownPlatforms;
    private String _query;
    private String _testJRE;
    private boolean _isPlatformRequest;
    private ServletContext _context;
    private String _encoding;
    
    private HttpServletRequest _httpRequest;

    // HTTP Compression RFC 2616 : Standard headers
    public static final String ACCEPT_ENCODING          = "accept-encoding";
    
    // Construct Request object based on HTTP request
    public DownloadRequest (HttpServletRequest request)
    {
    	this((ServletContext) null, request);
    }

    public static final String	DEFAULT_JNLP_FILE	=	"launch.jnlp";
    public DownloadRequest (ServletContext context, HttpServletRequest request)
    {
    	_context = context;	
    	_httpRequest = request;
    	_path = request.getRequestURI();
    	_encoding = request.getHeader(ACCEPT_ENCODING);

    	final String contextPath = request.getContextPath();	
    	if ((contextPath != null) && (contextPath.length() > 0))
    		_path = _path.substring(contextPath.length());       

    	if ((_path == null) || (_path.length() <= 0))
    		_path = request.getServletPath(); // This works for *.<ext> invocations
    	if ((_path == null) || (_path.length() <= 0))
    		_path = "/"; // No path given
    	_path = _path.trim();
    	if ((_context != null) && (!_path.endsWith("/")))
    	{
    		final String realPath = _context.getRealPath(_path);
    		// fix for 4474021 - getRealPath might returns NULL
    		if ((realPath != null) && (realPath.length() > 0))
    		{
    			final File f=new File(realPath);
    			if (f.exists() && f.isDirectory())
    				_path += "/";
    		}
    	}
        // Append default file for a directory
        if (_path.endsWith("/"))
        	_path += DEFAULT_JNLP_FILE;

        _version = getParameter(request, ARG_VERSION_ID);
        _currentVersionId = getParameter(request, ARG_CURRENT_VERSION_ID);
        _os = getParameterList(request, ARG_OS);
        _arch = getParameterList(request, ARG_ARCH);
        _locale = getParameterList(request, ARG_LOCALE);	    	    
        _knownPlatforms = getParameterList(request, ARG_KNOWN_PLATFORMS);
	
        final String platformVersion = getParameter(request, ARG_PLATFORM_VERSION_ID);	    
        _isPlatformRequest =  (platformVersion != null) && (platformVersion.length() > 0);
        if (_isPlatformRequest)
        	_version = platformVersion;
        _query = request.getQueryString();
        _testJRE = getParameter(request, TEST_JRE);
    }

    /* Returns a DownloadRequest for the currentVersionId, that can be used
     *  to lookup the existing cached version
     */
    public DownloadRequest (DownloadRequest dreq)
    {
		_encoding = dreq._encoding;
		_context = dreq._context;
		_httpRequest = dreq._httpRequest;
		_path = dreq._path;	        
		_version = dreq._currentVersionId;
		_currentVersionId = null;
		_os = dreq._os;
		_arch = dreq._arch;
		_locale = dreq._locale;
		_knownPlatforms = dreq._knownPlatforms;	
		_isPlatformRequest =  dreq._isPlatformRequest;	
		_query = dreq._query;
		_testJRE = dreq._testJRE;
    }
    
    private static String getParameter (HttpServletRequest req, String key)
    {
    	final String res=(null == req) ? null : req.getParameter(key);
    	return (res == null) ? null : res.trim();    
    }

    /* Converts a space delimited string to a list of strings */
    static private String[] getStringList (final String str)
    {
        final int length=(null == str) ? 0 : str.length();
        if (length <= 0)
        	return null;

        Collection<String> list = new LinkedList<String>();
        int i = 0;
        StringBuilder sb = null;
        while(i < length)
        {
            char ch = str.charAt(i);
            if (ch == ' ')
            {
                // A space was hit. Add string to list
                if (sb != null)
                {
                	if (sb.length() > 0)
                		list.add(sb.toString());
                    sb = null;
                }
            }
            else if (ch == '\\')
            {
                // It is a delimiter. Add next character
                if (i + 1 < length)
                {
                    ch = str.charAt(++i);
                    if (sb == null)
                    	sb = new StringBuilder();
                    sb.append(ch);
                }
            }
            else
            {
                if (sb == null)
                	sb = new StringBuilder();
                sb.append(ch);
            }
            i++; // Next character
        }
        // Make sure to add the last part to the list too
        if ((sb != null) && (sb.length() > 0))
            list.add(sb.toString());
	
        if (list.size() <= 0)
        	return null;        

        return list.toArray(new String[list.size()]);
    }
    
    /* Split parameter at spaces. Convert '\ ' into a space */
    private static String[] getParameterList (HttpServletRequest req, String key)
    {
    	final String res=req.getParameter(key);
    	return (res == null) ? null : getStringList(res.trim());
    }
    
    // Query
    public String getPath() { return _path; }
    public String getVersion() { return _version; }
    public String getCurrentVersionId() { return _currentVersionId; }
    public String getQuery() { return _query; }
    public String getTestJRE() { return _testJRE; }
    public String getEncoding() { return _encoding; }
    public String[] getOS() { return _os; }
    public String[] getArch() { return _arch; }
    public String[] getLocale() { return _locale; }
    public String[] getKnownPlatforms() { return _knownPlatforms; }
    public boolean isPlatformRequest() { return _isPlatformRequest; }	
    public HttpServletRequest getHttpRequest() { return _httpRequest; }
    
    /* Returns a DownloadRequest for the currentVersionId, that can be used
     *  to lookup the existing cached version
     */
    public DownloadRequest getFromDownloadRequest ()
    {
    	return new DownloadRequest(this);
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString ()
    { 
    	return "DownloadRequest[path=" + getPath() + 
    		ObjectUtil.showEntry(" encoding=", getEncoding()) +
		    ObjectUtil.showEntry(" query=", getQuery()) + 
		    ObjectUtil.showEntry(" TestJRE=", getTestJRE()) +
		    ObjectUtil.showEntry(" version=", getVersion()) +
		    ObjectUtil.showEntry(" currentVersionId=", getCurrentVersionId()) +
		    ObjectUtil.showEntry(" os=", getOS()) + 
		    ObjectUtil.showEntry(" arch=", getArch()) + 
		    ObjectUtil.showEntry(" locale=", getLocale()) +
		    ObjectUtil.showEntry(" knownPlatforms=", getKnownPlatforms())
		    + " isPlatformRequest=" + isPlatformRequest() + "]"
		    ;
    }
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DownloadRequest /* co-variant return */ clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}		
}


