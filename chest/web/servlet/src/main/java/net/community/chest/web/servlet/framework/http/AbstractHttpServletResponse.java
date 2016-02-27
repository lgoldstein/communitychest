/*
 * 
 */
package net.community.chest.web.servlet.framework.http;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.Cookie;

import net.community.chest.web.servlet.framework.AbstractServletResponse;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2010 12:00:34 PM
 */
public abstract class AbstractHttpServletResponse extends AbstractServletResponse
		implements XHttpServletResponse {
	
	private final Map<String,String>	_hdrsMap;
	/*
	 * @see net.community.chest.web.servlet.framework.http.XHttpServletResponse#getHeadersMap()
	 */
	@Override
	public final Map<String,String> getHeadersMap ()
	{
		return _hdrsMap;
	}

	private final Collection<Cookie>	_cookies;
	/*
	 * @see net.community.chest.web.servlet.framework.http.XHttpServletResponse#getCookiesList()
	 */
	@Override
	public final Collection<Cookie> getCookiesList ()
	{
		return _cookies;
	}

	protected AbstractHttpServletResponse ()
	{
		_cookies = new LinkedList<Cookie>();
		_hdrsMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	@Override
	public void addCookie (Cookie c)
	{
		final Collection<Cookie>	cl=getCookiesList();
		if (null == c)
			return;	// debug breakpoint

		if (!cl.add(c))
			return;	// debug breakpoint
	}
	/*
	 * @see net.community.chest.web.servlet.framework.http.XHttpServletResponse#getHeader(java.lang.String)
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
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void setHeader (String name, String value)
	{
		if ((null == name) || (name.length() <= 0))
			return;

		final Map<String,String>	hm=getHeadersMap();
		final String				prev;
		if ((null == value) || (value.length() <= 0))
			prev = hm.remove(name);
		else
			prev = hm.put(name, value);
		
		if ((prev != null) && (prev.length() > 0))
			return;	// debug breakpoint
	}
	/*
	 * @see net.community.chest.web.servlet.framework.http.XHttpServletResponse#removeHeader(java.lang.String)
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
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	@Override
	public boolean containsHeader (String name)
	{
		if ((null == name) || (name.length() <= 0))
			return false;

		final Map<String,?>	hm=getHeadersMap();
		if ((null == hm) || (hm.size() <= 0))
			return false;

		return hm.containsKey(name);
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader (String name, String value)
	{
		setHeader(name, value);
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	@Override
	public void addDateHeader (String name, long date)
	{
		if (date != (-1L))
			addHeader(name, String.valueOf(date));
		else
			removeHeader(name);
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	@Override
	public void addIntHeader (String name, int value)
	{
		addHeader(name, String.valueOf(value));
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	@Override
	public void setDateHeader (String name, long date)
	{
		if (date != (-1L))
			setHeader(name, String.valueOf(date));
		else
			removeHeader(name);
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	@Override
	public void setIntHeader (String name, int value)
	{
		setHeader(name, String.valueOf(value));
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public String encodeRedirectUrl (String url)
	{
		return url;
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	@Override
	public String encodeRedirectURL (String url)
	{
		return url;
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public String encodeUrl (String url)
	{
		return url;
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	@Override
	public String encodeURL (String url)
	{
		return url;
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	@Override
	public void sendRedirect (String location) throws IOException
	{
		if ((null == location) || (location.length() <= 0))
			throw new IOException("No redirect location specified");
	}

	private int	_statusCode;
	/*
	 * @see net.community.chest.web.servlet.framework.http.XHttpServletResponse#getStatusCode()
	 */
	@Override
	public int getStatusCode ()
	{
		return _statusCode;
	}

	private String	_statusMsg;
	/*
	 * @see net.community.chest.web.servlet.framework.http.XHttpServletResponse#getStatusMessage()
	 */
	@Override
	public String getStatusMessage ()
	{
		return _statusMsg;
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void setStatus (int sc, String sm)
	{
		if (_statusCode != sc)
			_statusCode = sc;
		_statusMsg = sm;
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	@Override
	public void setStatus (int sc)
	{
		setStatus(sc, String.valueOf(sc));
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	@Override
	public void sendError (int sc, String msg) throws IOException
	{
		setStatus(sc, msg);
	}
	/*
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	@Override
	public void sendError (int sc) throws IOException
	{
		sendError(sc, String.valueOf(sc));
	}
}
