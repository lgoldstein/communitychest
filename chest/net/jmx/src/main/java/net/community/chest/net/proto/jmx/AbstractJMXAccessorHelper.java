package net.community.chest.net.proto.jmx;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 7, 2008 12:48:38 PM
 */
public abstract class AbstractJMXAccessorHelper extends AbstractJMXAccessor {
	protected AbstractJMXAccessorHelper ()
	{
		super();
	}

	private URI	_url	/* =null */;
	/*
	 * @see net.community.chest.apache.httpclient.jmx.JMXAccessor#connect(java.net.URI)
	 */
	@Override
	public void connect (URI u) throws IOException
	{
		if (null == u)
			throw new ConnectException("No URL instance supplied");

		if (_url != null)
			throw new IOException("connect(" + u + ") already connected to " + _url);

		_url = u;
	}
	/*
	 * @see net.community.chest.apache.httpclient.jmx.JMXAccessor#getAccessURL()
	 */
	@Override
	public URI getAccessURL ()
	{
		return _url;
	}

	private String	_path	/* =null */;
	public synchronized String getAccessPath ()
	{
		if (null == _path)
		{
			final URI	u=getAccessURL();
			_path = (null == u) ? null : u.getPath();
		}

		return _path;
	}
	// CAVEAT EMPTOR !!!
	public synchronized void setAccessPath (String path)
	{
		_path = path;
	}
	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (_url != null)
			_url = null;
	}
}
