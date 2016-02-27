package net.community.chest.jfree.jcommon.util;
import net.community.chest.util.logging.AbstractLoggerWrapper;
import net.community.chest.util.logging.LogLevelWrapper;

import org.jfree.util.LogTarget;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 26, 2009 3:20:37 PM
 */
public abstract class AbstractLogTargetWrapper extends AbstractLoggerWrapper implements LogTarget {
	protected AbstractLogTargetWrapper (Class<?> logClass, String logName, String clsIndex)
		throws IllegalArgumentException
	{
		super(logClass, logName, clsIndex);
	}

	protected AbstractLogTargetWrapper (Class<?> logClass, String clsIndex)
		throws IllegalArgumentException
	{
		this(logClass, null, clsIndex);
	}

	protected AbstractLogTargetWrapper (Class<?> logClass)
		throws IllegalArgumentException
	{
		this(logClass, null);
	}

	public static final LogLevelWrapper fromTargetLevel (int level)
	{
		switch(level)
		{
			case ERROR	: return LogLevelWrapper.ERROR;
			case WARN	: return LogLevelWrapper.WARNING;
			case INFO	: return LogLevelWrapper.INFO;
			case DEBUG	: return LogLevelWrapper.DEBUG;
			default		: return null;
		}
	}
	/*
	 * @see org.jfree.util.LogTarget#log(int, java.lang.Object, java.lang.Exception)
	 */
	@Override
	public void log (int level, Object message, Exception e)
	{
		final LogLevelWrapper	lvl=fromTargetLevel(level);
		if ((lvl != null) && isEnabledFor(lvl))
			// TODO special management for Log.SimpleMessage and LogContext
			log(lvl, String.valueOf(message), e);
	}
	/*
	 * @see org.jfree.util.LogTarget#log(int, java.lang.Object)
	 */
	@Override
	public void log (int level, Object message)
	{
		log(level, message, null);
	}
}
