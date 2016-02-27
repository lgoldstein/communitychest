package net.community.chest.apache.log4j;

import net.community.chest.apache.log4j.helpers.ExtendedPatternParser;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternParser;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides setHeader/Footer methods and uses the {@link ExtendedPatternParser}
 * by default</P>
 *
 * @author Lyor G.
 * @since Sep 26, 2007 11:57:40 AM
 */
public class BaseLayout extends PatternLayout {
	public BaseLayout ()
	{
		super();
	}

	private String _header /* =null */;
	/*
	 * @see org.apache.log4j.Layout#getHeader()
	 */
	@Override
	public String getHeader ()
	{
		return _header;
	}

	public void setHeader(String header)
	{
		_header = header;
	}

	private String	_footer	/* =null */;
	/*
	 * @see org.apache.log4j.Layout#getFooter()
	 */
	@Override
	public String getFooter ()
	{
		return _footer;
	}

	public void setFooter (String footer)
	{
		_footer = footer;
	}

	private boolean	_ignThrowable	/* =false */;
	public boolean isThrowableIgnored ()
	{
		return _ignThrowable;
	}

	public void setThrowableIgnored (boolean flag)
	{
		_ignThrowable = flag;
	}
	/*
	 * @see org.apache.log4j.Layout#ignoresThrowable()
	 */
	@Override
	public boolean ignoresThrowable ()
	{
		return isThrowableIgnored();
	}
	/*
	 * @see org.apache.log4j.PatternLayout#createPatternParser(java.lang.String)
	 */
	@Override
	protected PatternParser createPatternParser (String pattern)
	{
		return new ExtendedPatternParser(pattern);
	}
}
