/*
 * @(#)JnlpResource.java	1.8 05/11/17
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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import javax.servlet.ServletContext;

import jnlp.sample.servlet.download.DownloadResponse;
import jnlp.sample.util.ObjectUtil;

/** 
 *  A JnlpResource encapsulate the information about a resource that is
 *  needed to process a JNLP Download Request.
 *
 *  The pattern matching arguments are: name, version-id, os, arch, and locale.
 *
 *  The outgoing arguments are:
 *     - path to resource in (WAR File)
 *     - product version-id (Version-id to return or null. Typically same as version-id above)
 *     - mime-type for content
 *     - lastModified date of WAR file resource
 * 
 */
public class JnlpResource implements Serializable {       
	/**
	 * 
	 */
	private static final long serialVersionUID = 1951440875518157297L;
	public static final String JNLP_MIME_TYPE      = "application/x-java-jnlp-file";
	public static final String JAR_MIME_TYPE       = "application/x-java-archive";

	public static final String JAR_MIME_TYPE_NEW   = "application/java-archive";
    
    // Default extension for the JNLP file
	public static final String JNLP_EXTENSION      = ".jnlp";
	public static final String JAR_EXTENSION       = ".jar";
    
    private static String _jnlpExtension = JNLP_EXTENSION;
    private static String _jarExtension = JAR_EXTENSION;
    
    public static void setDefaultExtensions (String jnlpExtension, String jarExtension)
    {
    	if ((jnlpExtension != null) && (jnlpExtension.length() > 0))
    	{
    		if (jnlpExtension.charAt(0) != '.')
    			_jnlpExtension = "." + jnlpExtension;
    		else
    			_jnlpExtension = jnlpExtension;	
    	}

    	if ((jarExtension != null) && (jarExtension.length() > 0))
    	{
    		if (jarExtension.charAt(0) != '.')
    			_jarExtension  = "." + jarExtension ;
    		_jarExtension = jarExtension;	
    	}
    }   

    /* Pattern matching arguments */
    private String _name;	  // Name of resource with path (this is the same as path for non-version based)
    private String _versionId;    // Version-id for resource, or null if none
    private String[] _osList;     // List of OSes for which resource should be returned
    private String[] _archList;   // List of architectures for which the resource should be returned
    private String[] _localeList; // List of locales for which the resource should be returned
    /* Information used for reply */
    private String _path;            // Path to resource in WAR file (unique)
    private URL    _resource;        // URL to resource in WAR file (unique - same as above really)
    private long   _lastModified;    // Last modified in WAR file
    private String _mimeType;        // MIME-type for resource
    private String _returnVersionId; // Version Id to return
    private String _encoding;        // Accept encoding

    public static final String	GZ_SUFFIX=".gz", PACK_GZ_SUFFIX=".pack" + GZ_SUFFIX;

    public JnlpResource (ServletContext context, 
						 String 		name, 
						 String 		versionId, 
						 String[] 		osList,
						 String[] 		archList, 
						 String[] 		localeList,
						 String 		path,
						 String 		returnVersionId,
						 String 		encoding)
    {
		// Matching arguments
		_encoding = encoding;
		_name = name;
		_versionId = (null == versionId) ? null : versionId.trim();	
		_osList = osList;
		_archList = archList;
		_localeList = localeList;

		_returnVersionId = (null == returnVersionId) ? null : returnVersionId.trim();

		/* Check for existence and get last modified timestamp */
		try
		{
			final String orig_path=path.trim();
			_mimeType = getMimeType(context, orig_path);

			// check if pack200 compression
			if ((encoding != null) && (encoding.length() > 0)
			 && (JAR_MIME_TYPE.equalsIgnoreCase(_mimeType) || JAR_MIME_TYPE_NEW.equalsIgnoreCase(_mimeType))
			 && (encoding.toLowerCase().indexOf(DownloadResponse.PACK200_GZIP_ENCODING) >= 0))
			{
				final String	search_path=orig_path + PACK_GZ_SUFFIX;
				if ((_resource=context.getResource(search_path)) != null)
				{
					// Get last modified time
					if ((_lastModified=getLastModified(context, _resource, search_path)) != 0L)
					{
						_path = search_path;
						return;
					}

					_resource = null;	// keep looking
				}
			}

			// gzip compression
			if ((encoding != null) && (encoding.length() > 0)
			 && (encoding.toLowerCase().indexOf(DownloadResponse.GZIP_ENCODING) >= 0))
			{
				final String	search_path=orig_path + GZ_SUFFIX;
				if ((_resource=context.getResource(search_path)) != null)
				{
					// Get last modified time
					if ((_lastModified=getLastModified(context, _resource, search_path)) != 0L)
					{
						_path = search_path;
						return;
					}

					_resource = null;	// keep looking
				}
			}

			// no compression
			final String	search_path=orig_path;
			if ((_resource=context.getResource(search_path)) != null)
			{
				if ((_lastModified=getLastModified(context, _resource, search_path)) != 0L)
				{
					_path = search_path;
					return;
				}

				_resource = null;
			}
		}
		catch(IOException ioe)
		{
			_resource = null;
		}				    			    
    }

    public JnlpResource (ServletContext context, 
						 String name, 
						 String versionId, 
						 String[] osList, 
						 String[] archList, 
						 String[] localeList,
						 String path,
						 String returnVersionId)
    {
    	this(context, name, versionId, osList, archList, localeList, path, returnVersionId, null);
    }
    
    public JnlpResource (ServletContext context, String path)
    { 
    	this(context, null, null, null, null, null, path, null); 
    }

    public static long getLastModified (ServletContext context, URL resource, String path)
    {
    	long 	lastModified=0L;
    	if (!ObjectUtil.isFileResource(resource))
    	{
    		URLConnection	conn=null;
    		try
    		{
    			// Get last modified time
    			if ((conn=resource.openConnection()) != null)
    				lastModified = conn.getLastModified();
    		}
    		catch (Exception e)
    		{
    			// do nothing
    		}
    	}
	
    	if (lastModified == 0L)
    	{
    		// Arguably a bug in the JRE will not set the lastModified for file URLs, and
    		// always return 0. This is a workaround for that problem.
    		final String filepath=context.getRealPath(path); 
    		if ((filepath != null) && (filepath.length() > 0))
    		{
    			final File f=new File(filepath);	    
    			if (f.exists())
    				lastModified = f.lastModified();
    		}
	    }

    	return lastModified;
    }

    /* Get resource specific attributes */
    public String getPath () { return _path; }
    public void setPath (String p)
    {
    	_path = p;
    }

    public URL getResource () { return _resource; } 
    public void setResource (URL r)
    {
    	_resource = r;
    }
    public boolean exists ()
    { 
    	return getResource() != null;
    }    

    public String getMimeType () { return _mimeType; }
    public void setMimeType (String t)
    {
    	_mimeType = t;
    }

    public long getLastModified () { return _lastModified; }
    public void setLastModified (long m)
    {
    	_lastModified = m;
    }

    public boolean isJnlpFile ()
    { 
    	final String	p=getPath();
    	return (p != null) && p.endsWith(_jnlpExtension);
    }

    public boolean isJarFile ()
    { 
    	final String	p=getPath();
    	return (p != null) && p.endsWith(_jarExtension);
    }
    
    /* Get JNLP version specific attributes */
    public String getName () { return _name; }            
    public void setName (String n)
    {
    	_name = n;
    }

    public String getEncoding () { return _encoding; }
    public void setEncoding (String e)
    {
    	_encoding = e;
    }

    public String[] getOSList ()  { return _osList; }
    public void setOSList (String ... l)
    {
    	_osList = l;
    }

    public String[] getArchList ()  { return _archList; }
    public void setArchList (String ... l)
    {
    	_archList = l;
    }

    public String[] getLocaleList ()  { return _localeList; }
    public void setLocaleList (String ... l)
    {
    	_localeList = l;
    }

    public String   getReturnVersionId () { return _returnVersionId; }
    public void setReturnVersionId (String v)
    {
    	_returnVersionId = (null == v) ? null : v.trim();
    }

    public String getVersionId () { return _versionId; }
    public void setVersionId (String v)
    {
    	_versionId = (null == v) ? null : v.trim();
    }

    public static String getMimeType (ServletContext context, String path)
    {	
    	final String mimeType=(null == context) ? null : context.getMimeType(path);
    	if ((mimeType != null) && (mimeType.length() > 0))
    		return mimeType;

    	final int	pLen=(null == path) ? 0 : path.length();
    	if (pLen > 0)
    	{
	    	if (path.endsWith(_jnlpExtension))
	    		return JNLP_MIME_TYPE;
	    	if (path.endsWith(_jarExtension))
	    		return JAR_MIME_TYPE;
    	}

    	return "application/unknown";
    }        
    /*
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString ()
    { 
    	return "JnlpResource[WAR Path: " + getPath()
    		+ ObjectUtil.showEntry(" versionId=", getVersionId())
    		+ ObjectUtil.showEntry(" name=", getName())
    		+ " lastModified=" + new Date(getLastModified())
    		+ ObjectUtil.showEntry(" osList=", getOSList())
    		+ ObjectUtil.showEntry(" archList=", getArchList())
    		+ ObjectUtil.showEntry(" localeList=", getLocaleList()) + "]"
    		+ ObjectUtil.showEntry(" returnVersionId=", getReturnVersionId()) + "]"
    		;
    }
}
