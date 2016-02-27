/*
 * 
 */
package net.community.chest.web.servlet.framework.http;

import java.net.URI;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import net.community.chest.web.servlet.framework.AbstractServletRequest;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2010 10:32:30 AM
 */
public abstract class AbstractHttpServletRequest extends AbstractServletRequest
		implements XHttpServletRequest {

	private final Collection<Cookie>	_cookies;
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#getCookiesList()
	 */
	@Override
	public final Collection<Cookie> getCookiesList ()
	{
		return _cookies;
	}

	private final Map<String,String>	_hdrsMap;
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#getHeadersMap()
	 */
	@Override
	public final Map<String,String> getHeadersMap ()
	{
		return _hdrsMap;
	}

	protected AbstractHttpServletRequest ()
	{
		_cookies = new LinkedList<Cookie>();
		_hdrsMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
	}

	private String	_authType;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	@Override
	public String getAuthType ()
	{
		return _authType;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setAuthType(java.lang.String)
	 */
	@Override
	public void setAuthType (String value)
	{
		_authType = value;
	}

	private String	_ctxPath;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	@Override
	public String getContextPath ()
	{
		return _ctxPath;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setContextPath(java.lang.String)
	 */
	@Override
	public void setContextPath (String value)
	{
		_ctxPath = value;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#addCookie(javax.servlet.http.Cookie)
	 */
	@Override
	public Collection<Cookie> addCookie (Cookie c)
	{
		final Collection<Cookie>	cl=getCookiesList();
		if (null == c)
			return cl;	// debug breakpoint

		if (!cl.add(c))
			return cl;	// debug breakpoint
		
		return cl;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#removeCookie(javax.servlet.http.Cookie)
	 */
	@Override
	public Collection<Cookie> removeCookie (Cookie c)
	{
		final Collection<Cookie>	cl=getCookiesList();
		if (null == c)
			return cl;	// debug breakpoint

		if (!cl.remove(c))
			return cl;	// debug breakpoint
		
		return cl;
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	@Override
	public Cookie[] getCookies ()
	{
		final Collection<? extends Cookie>	cl=getCookiesList();
		final int							numCookies=(null == cl) ? 0 : cl.size();
		if (numCookies <= 0)
			return null;
		
		return cl.toArray(new Cookie[numCookies]);
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	@Override
	public String getHeader (String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;
		
		final Map<String,String>	hm=getHeadersMap();
		if ((null == hm) || (hm.size() <= 0))
			return null;

		return hm.get(name);
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#removeHeader(java.lang.String)
	 */
	@Override
	public String removeHeader (String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;
		
		final Map<String,String>	hm=getHeadersMap();
		if ((null == hm) || (hm.size() <= 0))
			return null;

		return hm.remove(name);
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public String setHeader (String name, String value)
	{
		if ((null == name) || (name.length() <= 0))
			return null;

		if ((null == value) || (value.length() <= 0))
			return removeHeader(name);

		final Map<String,String>	hm=getHeadersMap();
		return hm.put(name, value);
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	@Override
	public long getDateHeader (String name)
	{
		final String	value=getHeader(name);
		if ((null == value) || (value.length() <= 0))
			return (-1L);

		try
		{
			final Long	ret=Long.decode(value);
			final long	dateVal=(null == ret) ? Long.MAX_VALUE : ret.longValue();
			if (dateVal <= 0L)
				throw new IllegalArgumentException("getDateHeader(" + name + ")[" + value + "] - bad value");
			
			return dateVal;
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("getDateHeader(" + name + ")=" + value, e);
		}
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	@Override
	public Enumeration<?> getHeaderNames ()
	{
		// TODO use an internal implementation for Enumeration
		final Properties	p=new Properties();
		final Map<?,?>		hm=getHeadersMap();
		if ((hm != null) && (hm.size() > 0))
			p.putAll(hm);

		return p.keys();
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	@Override
	public Enumeration<?> getHeaders (String name)
	{
		// TODO use an internal implementation for Enumeration
		final Properties	p=new Properties();
		final String		value=getHeader(name);
		if ((value != null) && (value.length() > 0))
			p.put(value, name);

		return p.keys();
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	@Override
	public int getIntHeader (String name)
	{
		final String	value=getHeader(name);
		if ((null == value) || (value.length() <= 0))
			return (-1);

		return Integer.decode(value).intValue();
	}

	private String	_method;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	@Override
	public String getMethod ()
	{
		return _method;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setMethod(java.lang.String)
	 */
	@Override
	public void setMethod (String value)
	{
		_method = value;
	}

	private String	_pathInfo;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	@Override
	public String getPathInfo ()
	{
		return _pathInfo;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setPathInfo(java.lang.String)
	 */
	@Override
	public void setPathInfo (String value)
	{
		_pathInfo = value;
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	@Override
	public String getPathTranslated ()
	{
		return getPathInfo();
	}

	private String	_qryString;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	@Override
	public String getQueryString ()
	{
		return _qryString;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setQueryString(java.lang.String)
	 */
	@Override
	public void setQueryString (String value)
	{
		_qryString = value;
	}

	private String	_remoteUser;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	@Override
	public String getRemoteUser ()
	{
		return _remoteUser;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setRemoteUser(java.lang.String)
	 */
	@Override
	public void setRemoteUser (String value)
	{
		_remoteUser = value;
	}

	private String	_reqSessId;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	@Override
	public String getRequestedSessionId ()
	{
		return _reqSessId;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setRequestedSessionId(java.lang.String)
	 */
	@Override
	public void setRequestedSessionId (String value)
	{
		_reqSessId = value;
	}

	private String	_reqURI;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	@Override
	public String getRequestURI ()
	{
		return _reqURI;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setRequestURI(java.lang.String)
	 */
	@Override
	public void setRequestURI (String value)
	{
		_reqURI = value;
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	@Override
	public StringBuffer getRequestURL ()
	{
		final String	value=getRequestURI();
		if ((null == value) || (value.length() <= 0))
			return null;

		return new StringBuffer(value);
	}

	private String	_servletPath;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	@Override
	public String getServletPath ()
	{
		return _servletPath;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setServletPath(java.lang.String)
	 */
	@Override
	public void setServletPath (String value)
	{
		_servletPath = value;
	}

	private HttpSession	_session;

	/*
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	@Override
	public HttpSession getSession (boolean create)
	{
		if ((null == _session) && create)
			throw new UnsupportedOperationException("getSession(" + create + ") N/A");

		return _session;
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	@Override
	public HttpSession getSession ()
	{
		return getSession(false);
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setSession(javax.servlet.http.HttpSession)
	 */
	@Override
	public void setSession (HttpSession value)
	{
		_session = value;
	}

	private Principal	_userPrincipal;
	/*
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	@Override
	public Principal getUserPrincipal ()
	{
		return _userPrincipal;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XHttpServletRequest#setUserPrincipal(java.security.Principal)
	 */
	@Override
	public void setUserPrincipal (Principal value)
	{
		_userPrincipal = value;
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	@Override
	public boolean isRequestedSessionIdFromCookie ()
	{
		return false;
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	@Override
	@SuppressWarnings("deprecation")
	public boolean isRequestedSessionIdFromUrl ()
	{
		return false;
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	@Override
	public boolean isRequestedSessionIdFromURL ()
	{
		return false;
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	@Override
	public boolean isRequestedSessionIdValid ()
	{
		final String	sessId=getRequestedSessionId();
		if ((null == sessId) || (sessId.length() <= 0))
			return false;
		
		return true;
	}
	/*
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	@Override
	public boolean isUserInRole (String role)
	{
		if ((null == role) || (role.length() <= 0))
			return false;
		
		return true;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.AbstractServletRequest#updateContents(java.net.URI)
	 */
	@Override
	public void updateContents (URI uri)
	{
		super.updateContents(uri);

		if (null == uri)
			return;

		setAuthType(uri.getAuthority());
		setPathInfo(uri.getPath());
		setQueryString(uri.getQuery());
		setRequestURI(uri.toString());
	}
}
