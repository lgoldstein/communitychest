/*
 * 
 */
package net.community.chest.apache.log4j.factory;

import java.io.IOException;

import net.community.chest.io.output.LineWriter;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * <P>Copyright as per GPLv2</P>
 * 
 * <P>A {@link java.io.Writer} implementation that logs every LF terminated
 * line as a log using the provided {@link Logger} and level</P>
 * 
 * @author Lyor G.
 * @since Aug 3, 2010 1:53:51 PM
 *
 */
public class Log4jLoggingWriter extends LineWriter {
	/**
	 * The {@link Logger} instance used for logging
	 */
	private Logger	_logger;
	public Logger getLogger ()
	{
		return _logger;
	}

	public void setLogger (Logger logger)
	{
		_logger = logger;
	}
	/**
	 * The {@link Priority} at which the output is generated. 
	 */
	private Priority	_level;
	public Priority getOutputLevel ()
	{
		return _level;
	}

	public void setOutputLevel (Priority p)
	{
		_level = p;
	}

	public Log4jLoggingWriter (Logger l, Priority p)
	{
		_logger = l;
		_level = p;
	}

	public Log4jLoggingWriter ()
	{
		this(null, null);
	}
	/*
	 * @see net.community.chest.io.output.LineLevelAppender#isWriteEnabled()
	 */
	@Override
	public boolean isWriteEnabled ()
	{
		final Logger	l=getLogger();
		final Priority	p=getOutputLevel();
		if ((null == l) || (null == p))
			return false;
		if (l.isEnabledFor(p))
			return true;

		return false;	// debug breakpoint
	}
	/*
	 * @see net.community.chest.io.output.LineLevelAppender#writeLineData(java.lang.StringBuilder, int)
	 */
	@Override
	public void writeLineData (StringBuilder sb, int dLen) throws IOException
	{
		// this point is reached if data buffer contains line separator pattern
		final Logger	logger=getLogger();
		final Priority	lvl=getOutputLevel();
		if ((dLen > 0) /* ignore empty lines */ && (logger != null) && (lvl != null))
		{
			final String	msg=sb.substring(0, dLen /* without the line separator */);
			logger.log(lvl, msg);
		}

		if (sb.length() > 0)
			sb.setLength(0);
	}

}
