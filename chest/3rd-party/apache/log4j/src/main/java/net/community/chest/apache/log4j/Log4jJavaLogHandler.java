/*
 * 
 */
package net.community.chest.apache.log4j;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * Implements the {@link Handler} class via log4j logging
 * @author Lyor G.
 * @since Jun 24, 2010 2:55:06 PM
 */
public class Log4jJavaLogHandler extends Handler {
	public Log4jJavaLogHandler ()
	{
		super();
	}

	private boolean	_open=true;
	/*
	 * @see java.util.logging.Handler#close()
	 */
	@Override
	public void close () throws SecurityException
	{
		if (_open)
			_open = false;
	}
	/*
	 * @see java.util.logging.Handler#flush()
	 */
	@Override
	public void flush ()
	{
		if (!_open)
			throw new IllegalStateException("flush() handler is closed");
	}

	public static final org.apache.log4j.Level xlateLevel (final java.util.logging.Level lvl)
	{
		if (null == lvl)
			return null;

		if (java.util.logging.Level.SEVERE.equals(lvl))
			return org.apache.log4j.Level.ERROR;
		else if (java.util.logging.Level.WARNING.equals(lvl))
			return org.apache.log4j.Level.WARN;
		else if (java.util.logging.Level.INFO.equals(lvl))
			return org.apache.log4j.Level.INFO;
		else if (java.util.logging.Level.ALL.equals(lvl))
			return org.apache.log4j.Level.ALL;
		else if (java.util.logging.Level.OFF.equals(lvl))
			return org.apache.log4j.Level.OFF;
		else	// default for all others
			return org.apache.log4j.Level.DEBUG;
	}
	/*
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	@Override
	public void publish (final LogRecord record)
	{
		if ((!_open) || (null == record) || (!isLoggable(record)))
		    return;

		final org.apache.log4j.Level	log4jlvl=xlateLevel(record.getLevel());
		if ((null == log4jlvl) || org.apache.log4j.Level.OFF.equals(log4jlvl))
			return;

		final String					logName=record.getLoggerName();
		final org.apache.log4j.Logger	log4jlog=
			org.apache.log4j.Logger.getLogger(logName);
		if ((null == log4jlog) || (!log4jlog.isEnabledFor(log4jlvl)))
			return;

		final Throwable					t=record.getThrown();
		final ThrowableInformation		ti=(null == t) ? null : new ThrowableInformation(t);
		final Thread					curThread=Thread.currentThread();
		final long						curId=(null == curThread) ? Long.MIN_VALUE : curThread.getId(),
										recId=record.getThreadID();
		final String					curName=(null == curThread) ? "" : curThread.getName(),
										thName=
			((null == curName) || (curName.length() <= 0) || (recId != curId)) ? curName + "[ID=" + recId + "]" : curName;
		final LoggingEvent				le=
			new LoggingEvent(record.getSourceClassName(),	// fqnOfCategoryClass
							 log4jlog,				// logger
							 record.getMillis(),	// timeStamp
							 log4jlvl,				// level
							 record.getMessage(),	// message
							 thName, // threadName
							 ti,					// ThrowableInformation
							 null,					// ndc
							 null,					// LocationInfo
							 null);					// properties
		log4jlog.callAppenders(le);
	}
}
