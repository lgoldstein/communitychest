/*
 * 
 */
package net.community.chest.apache.log4j.factory;

import java.io.IOException;

import org.apache.log4j.Logger;

import net.community.chest.util.logging.AbstractLoggingPrintStream;
import net.community.chest.util.logging.LogLevelWrapper;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 1, 2008 2:45:46 PM
 */
public class Log4jLoggingPrintStream extends AbstractLoggingPrintStream {
	public Log4jLoggingPrintStream ()
	{
		super();
	}

	protected void log (Logger logger, org.apache.log4j.Level lvl, @SuppressWarnings("unused") StackTraceElement ce, String s)
	{
		if ((logger != null) && (lvl != null) && (s != null) && (s.length() > 0))
			logger.log(lvl, s);
	}
	/*
	 * @see net.community.chest.util.logging.AbstractLoggingPrintStream#log(net.community.chest.util.logging.LogLevelWrapper, java.lang.StackTraceElement, java.lang.String)
	 */
	@Override
	public void log (LogLevelWrapper l, StackTraceElement ce, String s) throws IOException
	{
		if ((null == s) || (s.length() <= 0))
			return;

		org.apache.log4j.Level	lvl=Log4jLoggerWrapper.fromJDKLevel(l);
		if (null == lvl)
			lvl = org.apache.log4j.Level.DEBUG;

		final String	logClass=(null == ce) ? null : ce.getClassName();
		final Logger	logger=
			((null == logClass) || (logClass.length() <= 0)) ? Logger.getRootLogger() : Logger.getLogger(logClass);
		log(logger, lvl, ce, s);
	}
}
