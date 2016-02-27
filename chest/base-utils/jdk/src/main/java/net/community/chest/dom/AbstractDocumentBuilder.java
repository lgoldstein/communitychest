/*
 * 
 */
package net.community.chest.dom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Provides some default implementations for some inherited methods</P>
 * 
 * @author Lyor G.
 * @since Aug 26, 2009 9:06:40 AM
 */
public abstract class AbstractDocumentBuilder extends DocumentBuilder {
	protected AbstractDocumentBuilder ()
	{
		super();
	}

	private DOMImplementation	_domImpl;
	/*
	 * @see javax.xml.parsers.DocumentBuilder#getDOMImplementation()
	 */
	@Override
	public DOMImplementation getDOMImplementation ()
	{
		return _domImpl;
	}

	public void setDOMImplementation (DOMImplementation impl)
	{
		_domImpl = impl;
	}

	private boolean	_nsAware;
	/*
	 * @see javax.xml.parsers.DocumentBuilder#isNamespaceAware()
	 */
	@Override
	public boolean isNamespaceAware ()
	{
		return _nsAware;
	}

	public void setNamespaceAware (boolean f)
	{
		_nsAware = f;
	}

	private boolean	_validating;
	/*
	 * @see javax.xml.parsers.DocumentBuilder#isValidating()
	 */
	@Override
	public boolean isValidating ()
	{
		return _validating;
	}

	public void setValidating (boolean f)
	{
		_validating = f;
	}

	private EntityResolver	_er;
	public EntityResolver getEntityResolver ()
	{
		return _er;
	}
	/*
	 * @see javax.xml.parsers.DocumentBuilder#setEntityResolver(org.xml.sax.EntityResolver)
	 */
	@Override
	public void setEntityResolver (EntityResolver er)
	{
		_er = er;
	}

	private ErrorHandler	_eh;
	public ErrorHandler getErrorHandler ()
	{
		return _eh;
	}
	/*
	 * @see javax.xml.parsers.DocumentBuilder#setErrorHandler(org.xml.sax.ErrorHandler)
	 */
	@Override
	public void setErrorHandler (ErrorHandler eh)
	{
		_eh = eh;
	}
	/*
	 * @see javax.xml.parsers.DocumentBuilder#parse(java.io.InputStream)
	 */
	@Override
	public Document parse (InputStream is) throws SAXException, IOException
	{
		return parse(is, "");
	}
	/*
	 * @see javax.xml.parsers.DocumentBuilder#parse(java.io.File)
	 */
	@Override
	public Document parse (File f) throws SAXException, IOException
	{
		if (null == f)
			throw new IOException("parse() no " + File.class.getSimpleName() + " specified");

		InputStream	in=null;
		try
		{
			in = new BufferedInputStream(new FileInputStream(f), IOCopier.DEFAULT_COPY_SIZE);
			return parse(in);
		}
		finally
		{
			FileUtil.closeAll(in);
		}
	}
	/*
	 * @see javax.xml.parsers.DocumentBuilder#parse(java.lang.String)
	 */
	@Override
	public Document parse (String uri) throws SAXException, IOException
	{
		if ((null == uri) || (uri.length() <= 0))
			throw new IOException("parse() no " + URI.class.getSimpleName() + " string provided");

		InputStream	in=null;
		try
		{
			final URL	url=new URI(uri).toURL();
			in = url.openStream();
			return parse(in);
		}
		catch(URISyntaxException e)
		{
			throw new StreamCorruptedException("parse(" + uri + ") " + e.getClass().getName() + ": " + e.getMessage());
		}
		finally
		{
			FileUtil.closeAll(in);
		}
	}
}
