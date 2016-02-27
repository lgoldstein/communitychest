/*
 * 
 */
package net.community.chest.web.servlet.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2010 9:45:09 AM
 */
public abstract class AbstractServletRequest implements XServletRequest {
	private final Map<String,Object>	_attrsMap;
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#getAttributesMap()
	 */
	@Override
	public final Map<String,Object> getAttributesMap ()
	{
		return _attrsMap;
	}

	private final Map<String,String[]>	_paramsMap;
	protected AbstractServletRequest ()
	{
		_attrsMap = new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
		_paramsMap = new TreeMap<String,String[]>(String.CASE_INSENSITIVE_ORDER);
	}
	/*
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute (String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;

		final Map<String,?>	aMap=getAttributesMap();
		if ((null == aMap) || (aMap.size() <= 0))
			return null;

		return aMap.get(name);
	}
	/*
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute (String name, Object o)
	{
		if ((null == name) || (name.length() <= 0))
			return;
		
		final Map<String,Object>	aMap=getAttributesMap();
		final Object				prev=
			(null == o) ? aMap.remove(name) : aMap.put(name, o);
		if (prev != null)
			return;	// debug breakpoint
	}
	/*
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	@Override
	public void removeAttribute (String name)
	{
		if ((null == name) || (name.length() <= 0))
			return;
		
		final Map<String,?>	aMap=getAttributesMap();
		if ((null == aMap) || (aMap.size() <= 0))
			return;

		final Object	prev=aMap.remove(name);
		if (null == prev)
			return;	// debug breakpoint
	}
	/*
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	@Override
	public Enumeration<?> getAttributeNames ()
	{
		// TODO use an internal implementation for Enumeration
		final Properties	props=new Properties();
		final Map<?,?>		aMap=getAttributesMap();
		if ((aMap != null) && (aMap.size() > 0))
			props.putAll(aMap);

		return props.keys();
	}

	private String	_charenc=Charset.defaultCharset().name();
	/*
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	@Override
	public String getCharacterEncoding ()
	{
		return _charenc;
	}
	/*
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	@Override
	public void setCharacterEncoding (String enc)
			throws UnsupportedEncodingException
	{
		_charenc = enc;
	}

	private int	_ctLength;
	/*
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	@Override
	public int getContentLength ()
	{
		return _ctLength;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setContentLength(int)
	 */
	@Override
	public void setContentLength (int value)
	{
		_ctLength = value;
	}

	private String	_ctType;
	/*
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	@Override
	public String getContentType ()
	{
		return _ctType;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType (String value)
	{
		_ctType = value;
	}

	private String	_localAddr;
	/*
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	@Override
	public String getLocalAddr ()
	{
		return _localAddr;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setLocalAddr(java.lang.String)
	 */
	@Override
	public void setLocalAddr (String value)
	{
		_localAddr = value;
	}

	private String	_localName;
	/*
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	@Override
	public String getLocalName ()
	{
		return _localName;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setLocalName(java.lang.String)
	 */
	@Override
	public void setLocalName (String value)
	{
		_localName = value;
	}

	private int	_localPort;
	/*
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	@Override
	public int getLocalPort ()
	{
		return _localPort;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setLocalPort(int)
	 */
	@Override
	public void setLocalPort (int value)
	{
		if (_localPort != value)
			_localPort = value;	// debug breakpoint
	}

	private int	_remotePort;
	/*
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	@Override
	public int getRemotePort ()
	{
		return _remotePort;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setRemotePort(int)
	 */
	@Override
	public void setRemotePort (int value)
	{
		if (_remotePort != value)
			_remotePort = value;	// debug breakpoint
	}

	private ServletInputStream	_inStream;
	/*
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	@Override
	public ServletInputStream getInputStream () throws IOException
	{
		return _inStream;
	}

	private BufferedReader	_rdr;
	/*
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	@Override
	public BufferedReader getReader () throws IOException
	{
		if (null == _rdr)
		{
			final InputStream	in=getInputStream();
			if (null == in)
				return null;

			final String	enc=getCharacterEncoding();
			if ((enc != null) && (enc.length() > 0))
				_rdr = new BufferedReader(new InputStreamReader(in, enc));
			else
				_rdr = new BufferedReader(new InputStreamReader(in));
		}

		return _rdr;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setInputStream(javax.servlet.ServletInputStream)
	 */
	@Override
	public void setInputStream (ServletInputStream value) throws IOException
	{
		_inStream = value;
		
		if (_rdr != null)
		{
			try
			{
				_rdr.close();
			}
			finally
			{
				_rdr = null;
			}
		}
	}

	private Locale	_locale=Locale.getDefault();
	/*
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	@Override
	public Locale getLocale ()
	{
		return _locale;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale (Locale value)
	{
		_locale = value;
	}
	/*
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	@Override
	public Enumeration<?> getLocales ()
	{
		// TODO use an internal Enumeration implementation
		final Properties	p=new Properties();
		final Locale[]		la=Locale.getAvailableLocales();
		if ((la != null) && (la.length > 0))
		{
			for (final Locale l : la)
				p.put(l, l);
		}

		return p.keys();
	}
	/*
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	@Override
	public Map<String,String[]> getParameterMap ()
	{
		return _paramsMap;
	}
	/*
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter (String name)
	{
		final String[]	va=getParameterValues(name);
		final int		numValues=(va == null) ? 0 : va.length;
		if (numValues <= 0)
			return null;
		if (numValues != 1)
			throw new IllegalStateException("getParameter(" + name + ") multiple (" + numValues + ") values");

		return va[0];
	}
	/*
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	@Override
	public Enumeration<?> getParameterNames ()
	{
		// TODO use an internal Enumeration implementation
		final Properties	p=new Properties();
		final Map<?,?>		pMap=getParameterMap();
		if ((pMap != null) && (pMap.size() > 0))
			p.putAll(pMap);

		return p.keys();
	}
	/*
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	@Override
	public String[] getParameterValues (String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;
		
		final Map<?,String[]>	pMap=getParameterMap();
		if ((null == pMap) || (pMap.size() <= 0))
			return null;

		return pMap.get(name);
	}

	private String	_proto;
	/*
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	@Override
	public String getProtocol ()
	{
		return _proto;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setProtocol(java.lang.String)
	 */
	@Override
	public void setProtocol (String value)
	{
		_proto = value;
	}
	/*
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public String getRealPath (String path)
	{
		return path;
	}

	private String	_remoteAddr;
	/*
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	@Override
	public String getRemoteAddr ()
	{
		return _remoteAddr;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setRemoteAddr(java.lang.String)
	 */
	@Override
	public void setRemoteAddr (String value)
	{
		_remoteAddr = value;
	}

	private String	_remoteHost;
	/*
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	@Override
	public String getRemoteHost ()
	{
		return _remoteHost;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setRemoteHost(java.lang.String)
	 */
	@Override
	public void setRemoteHost (String value)
	{
		_remoteHost = value;
	}
	/*
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	@Override
	public RequestDispatcher getRequestDispatcher (String path)
	{
		if ((null == path) || (path.length() <= 0))
			return null;

		throw new UnsupportedOperationException("getRequestDispatcher(" + path + ") N/A");
	}

	private String	_scheme;
	/*
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	@Override
	public String getScheme ()
	{
		return _scheme;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setScheme(java.lang.String)
	 */
	@Override
	public void setScheme (String value)
	{
		_scheme = value;
	}

	private String	_serverName;
	/*
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	@Override
	public String getServerName ()
	{
		return _serverName;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setServerName(java.lang.String)
	 */
	@Override
	public void setServerName (String value)
	{
		_serverName = value;
	}

	private int	_serverPort;
	/*
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	@Override
	public int getServerPort ()
	{
		return _serverPort;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setServerPort(int)
	 */
	@Override
	public void setServerPort (int value)
	{
		if (_serverPort != value)
			_serverPort = value;	// debug breakpoint
	}

	private boolean	_secure;
	/*
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	@Override
	public boolean isSecure ()
	{
		return _secure;
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#setSecure(boolean)
	 */
	@Override
	public void setSecure (boolean value)
	{
		if (_secure != value)
			_secure = value;	// debug breakpoint
	}
	/*
	 * @see net.community.chest.web.servlet.framework.XServletRequest#updateContents(java.net.URI)
	 */
	@Override
	public void updateContents (URI uri)
	{
		if (null == uri)
			return;

		setProtocol(uri.getScheme());
		setScheme(uri.getScheme());
		setServerName(uri.getHost());
		setLocalAddr(uri.getHost());
		setLocalName(uri.getHost());
		setLocalPort(uri.getPort());
		setServerPort(uri.getPort());
	}
}
