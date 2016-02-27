package net.community.chest.apache.log4j.factory;

import java.util.Arrays;
import java.util.List;

import net.community.chest.apache.log4j.ExtendedLevel;
import net.community.chest.util.logging.AbstractLoggerWrapper;
import net.community.chest.util.logging.LogLevelWrapper;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.Priority;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Implements {@link net.community.chest.util.logging.LoggerWrapper} interface using log4j {@link Logger}</P>
 * @author Lyor G.
 * @since Sep 30, 2007 3:15:11 PM
 */
public final class Log4jLoggerWrapper extends AbstractLoggerWrapper {
	public Log4jLoggerWrapper (Class<?> logClass, String logName, String clsIndex)
		throws IllegalArgumentException
	{
		super(logClass, logName, clsIndex);
	}

	public Log4jLoggerWrapper (Class<?> logClass, String clsIndex)
		throws IllegalArgumentException
	{
		this(logClass, null, clsIndex);
	}

	public Log4jLoggerWrapper (Class<?> logClass)
	{
		this(logClass, null);
	}

	private Logger	_logger	/* =null */;
	private synchronized Logger getLogger ()
	{
		if (null == _logger)
		{
			final Class<?>	logClass=getLoggingClass();
			if (logClass != null)	// should not be otherwise
			{
				final String	clsIndex=getLoggingClassIndex();
				if ((null == clsIndex) || (clsIndex.length() <= 0))
					_logger = Logger.getLogger(logClass);
				else
					_logger = Logger.getLogger(logClass.getName() + "[" + clsIndex + "]");
			}
		}

		return _logger;
	}

	public static final org.apache.log4j.Level fromJDKLevel (final LogLevelWrapper lvl)
	{
		if (null == lvl)
			return null;

		switch(lvl)
		{
			case ERROR		: return org.apache.log4j.Level.ERROR;
			case WARNING	: return org.apache.log4j.Level.WARN;
			case INFO		: return org.apache.log4j.Level.INFO;
			case DEBUG		: return org.apache.log4j.Level.DEBUG;
			case VERBOSE	: return ExtendedLevel.VERBOSE;
			case TRACE		: return org.apache.log4j.Level.TRACE;
			default			: return null;
		}
	}
	public static final LogLevelWrapper fromLog4jLevel (final org.apache.log4j.Level lvl)
	{
		if (null == lvl)
			return null;

		final int	numLevel=lvl.toInt();
		switch(numLevel)
		{
			case Priority.FATAL_INT : return LogLevelWrapper.FATAL;
			case Priority.ERROR_INT	: return LogLevelWrapper.ERROR;
			case Priority.WARN_INT	: return LogLevelWrapper.WARNING;
			case Priority.INFO_INT	: return LogLevelWrapper.INFO;
			case Priority.DEBUG_INT	: return LogLevelWrapper.DEBUG;
				// special values
			case org.apache.log4j.Level.TRACE_INT	: return LogLevelWrapper.TRACE;
			default									:
				if (numLevel == ExtendedLevel.VERBOSE.toInt())
					return LogLevelWrapper.VERBOSE;
		}

		return null;	// no match found
	}

	private static final List<? extends org.apache.log4j.Level>	_levels=
		Arrays.asList(org.apache.log4j.Level.ERROR,
					  org.apache.log4j.Level.WARN,
					  org.apache.log4j.Level.INFO,
					  org.apache.log4j.Level.DEBUG,
					  ExtendedLevel.VERBOSE,
					  org.apache.log4j.Level.TRACE);
	public static final org.apache.log4j.Level fromStrLevel (final String s)
	{
		if ((null == s) || (s.length() <= 0))
			return null;

		for (final org.apache.log4j.Level lvl : _levels)
		{
			final String	ls=(null == lvl) ? null : lvl.toString();
			if (s.equalsIgnoreCase(ls))
				return lvl;
		}

		// no match found
		return null;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#isEnabledFor(net.community.chest.util.logging.LogLevelWrapper)
	 */
	@Override
	public boolean isEnabledFor (LogLevelWrapper l)
	{
		final org.apache.log4j.Level	lvl=fromJDKLevel(l);
		if (null == lvl)	// should not happen
			return false;

		final Logger	log=getLogger();
		if (null == log)	// should not happen
			return false;

		if (log.isEnabledFor(lvl))
			return true;

		return false;	// just so we have debug breakpoiny
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#setEnabledFor(net.community.chest.util.logging.LogLevelWrapper)
	 */
	@Override
	public boolean setEnabledFor (LogLevelWrapper l)
	{
		final org.apache.log4j.Level	lvl=fromJDKLevel(l);
		if (null == lvl)	// should not happen
			return false;

		final Logger	log=getLogger();
		if (null == log)	// should not happen
			return false;

		log.setLevel(lvl);
		return true;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#log(net.community.chest.util.logging.LogLevelWrapper, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String log (LogLevelWrapper l, String msg, Throwable t)
	{
		final org.apache.log4j.Level	lvl=fromJDKLevel(l);
		final Logger					log=(null == lvl) /* should not happen */ ? null : getLogger();
		if (log != null)	// should not be otherwise
			log.log(lvl, msg, t);
		return msg;
	}

	/*
	 * @see net.community.chest.util.logging.AbstractLoggerWrapper#getThreadContext()
	 */
	@Override
	public String getThreadContext ()
	{
		return NDC.peek();
	}
	/*
	 * @see net.community.chest.util.logging.AbstractLoggerWrapper#popThreadContext()
	 */
	@Override
	public String popThreadContext ()
	{
		return NDC.pop();
	}
	/*
	 * @see net.community.chest.util.logging.AbstractLoggerWrapper#pushThreadContext(java.lang.String)
	 */
	@Override
	public String pushThreadContext (final String ctx)
			throws IllegalArgumentException
	{
		if ((null == ctx) || (ctx.length() <= 0))
			throw new IllegalArgumentException("pushThreadContext() no context");

		final String	cur=getThreadContext();
		NDC.push(ctx);
		return cur;
	}

	/*
	 * @see net.community.chest.util.logging.AbstractLoggerWrapper#clearThreadContext()
	 */
	@Override
	public void clearThreadContext ()
	{
		NDC.clear();
	}
}
