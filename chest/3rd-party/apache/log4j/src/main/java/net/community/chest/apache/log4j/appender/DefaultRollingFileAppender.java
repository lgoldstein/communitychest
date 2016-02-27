package net.community.chest.apache.log4j.appender;

import java.io.File;

import org.apache.log4j.spi.LoggingEvent;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 30, 2007 3:30:32 PM
 */
public class DefaultRollingFileAppender extends AbstractRollingFileAppender {
	public DefaultRollingFileAppender ()
	{
		super();
	}

	private long	_openTime	/* =0L */, _fileSize	/* =0L */;	
	/*
	 * @see net.community.chest.apache.log4j.appender.AbstractRollingFileAppender#getCurrentFileOpenTime()
	 */
	@Override
	public long getCurrentFileOpenTime ()
	{
		return _openTime;
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.AbstractRollingFileAppender#getCurrentFileSize()
	 */
	@Override
	public long getCurrentFileSize ()
	{
		return _fileSize;
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.AbstractRollingFileAppender#assignLoggingFile()
	 */
	@Override
	protected File assignLoggingFile ()
	{
		final File	logFile=super.assignLoggingFile();
		// re-start from scratch
		_fileSize = 0L;
		_openTime = (null == logFile) ? 0L : System.currentTimeMillis();

		return logFile;
	}
	/*
	 * @see net.community.chest.apache.log4j.appender.AbstractRollingFileAppender#appendFormattedEvent(org.apache.log4j.spi.LoggingEvent, java.lang.String)
	 */
	@Override
	protected boolean appendFormattedEvent (LoggingEvent e, String msg)
	{
		if (super.appendFormattedEvent(e, msg))
		{
			final long	msgLen=(null == msg) ? 0 : msg.length();
			if (msgLen > 0)	// we do not count CR/LF(s)...
				_fileSize += msgLen;

			return true;
		}

		return false;
	}
}
