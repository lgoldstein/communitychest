package net.community.chest.util.logging.factory.console;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Map;

import net.community.chest.util.logging.AbstractLoggerWrapper;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.format.LogMsgComponentFormatter;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 26, 2007 1:32:47 PM
 */
public final class ConsoleLoggerWrapper extends AbstractLoggerWrapper {
	/**
	 * Prefix of all properties names used for controlling the wrapper's
	 * behavior (e.g., level, output stream, etc.)
	 */
	private static final String WRAPPER_BASE_PROPPREFIX=ConsoleLoggerWrapper.class.getName().toLowerCase();
	private static final String getWrapperPropName (final String name)
	{
		return WRAPPER_BASE_PROPPREFIX + "." + name;
	}

	private static final String getLevelStreamPropName (final LogLevelWrapper l)
	{
		final String	lName=(null == l) ? null : l.name();
		return getWrapperPropName(String.valueOf(lName).toLowerCase());
	}

	private static Map<LogLevelWrapper,PrintStream>	_streamsMap;
	private static final synchronized PrintStream getPrintStream (final LogLevelWrapper l)
	{
		if (null == l)
			return System.err;

		PrintStream	out=(null == _streamsMap) ? null : _streamsMap.get(l);
		if (out != null)
			return out;

		// use the level name as the property last component
		final String	pName=getLevelStreamPropName(l),
						pValue=System.getProperty(pName);
		final Boolean	bVal;
		// if no special value set then display all warning and below in STDERR
		if ((null == pValue) || (pValue.length() <= 0))
			bVal = Boolean.valueOf(l.compareTo(LogLevelWrapper.WARNING) <= 0);
		else
			bVal = Boolean.valueOf(pValue);

		out = ((null == bVal) || bVal.booleanValue()) ? System.err : System.out;

		if (null == _streamsMap)
			_streamsMap = new EnumMap<LogLevelWrapper, PrintStream>(LogLevelWrapper.class);
		_streamsMap.put(l, out);

		return out;
	}
	/**
	 * Default format of message(s) to be used unless overridden via the
	 * {@link #WRAPPER_FORMAT_PROPNAME} 
	 */
	public static final String WRAPPER_DEFAULT_FORMAT=
		new StringBuilder(128).append(LogMsgComponentFormatter.MODIFIER_CHAR)
							  .append(LogMsgComponentFormatter.TIMESTAMP)
							  		.append(LogMsgComponentFormatter.MODOPT_START_DELIM)
							  		.append("HH:mm:ss.SSS")
							  		.append(LogMsgComponentFormatter.MODOPT_END_DELIM)
						.append(' ')
							  .append(LogMsgComponentFormatter.MODIFIER_CHAR)
							  .append(LogMsgComponentFormatter.THREAD_NAME)
						.append('\t')
						  .append(LogMsgComponentFormatter.MODIFIER_CHAR)
						  .append(LogMsgComponentFormatter.SIMPLE_CLASS_NAME)
						.append('\t')
						  .append(LogMsgComponentFormatter.MODIFIER_CHAR)
						  .append(LogMsgComponentFormatter.MESSAGE)
			.toString();
	/**
	 * Property used to specify how to format the message(s) - if missing
	 * then {@link #WRAPPER_DEFAULT_FORMAT} is used 
	 */
	public static final String WRAPPER_FORMAT_PROPNAME=getWrapperPropName("format");

	private static LogMsgComponentFormatter<?>[]	_fmts	/* =null */;
	private static final synchronized LogMsgComponentFormatter<?>[] getFormatters ()
	{
		if (null == _fmts)
			_fmts = LogMsgComponentFormatter.parseFormat(System.getProperty(WRAPPER_FORMAT_PROPNAME, WRAPPER_DEFAULT_FORMAT));

		return _fmts;
	}

	private static final void writeMessage (Thread th, long logTime, Class<?> logClass, LogLevelWrapper l, Object ctx, String msg, Throwable t)
	{
		final PrintStream					out=getPrintStream(l);
		final LogMsgComponentFormatter<?>[]	fmts=getFormatters();
		if ((out != null) && (fmts != null) && (fmts.length > 0))
		{
			int	numWritten=0;
			for (final LogMsgComponentFormatter<?> f : fmts)
			{
				if (null == f)	// should not happen
					continue;

				final String	v=f.format(th, logTime, logClass, l, ctx, msg, t);
				out.append(String.valueOf(v));
				numWritten++;
			}

			if (numWritten > 0)
				out.println();
		}
	}

	public ConsoleLoggerWrapper (Class<?> logClass, String logName, String clsIndex)
		throws IllegalArgumentException
	{
		super(logClass, logName, clsIndex);
	}
	/**
	 * Property used to control min. level of output messages - default={@link LogLevelWrapper#INFO}
	 * (i.e., anything below this level will not be output)
	 */
	public static final String WRAPPER_LEVEL_PROPNAME=getWrapperPropName("level");
	private static LogLevelWrapper	_outLevel	/* =null */;
	private static synchronized LogLevelWrapper getOutputLevel ()
	{
		if (null == _outLevel)
		{
			final String	lvlName=System.getProperty(WRAPPER_LEVEL_PROPNAME, LogLevelWrapper.INFO.name());
			if (null == (_outLevel=LogLevelWrapper.fromString(lvlName)))
				_outLevel = LogLevelWrapper.INFO;	// should not happen
		}

		return _outLevel;
	}

	static synchronized LogLevelWrapper setOutputLevel (LogLevelWrapper l)
	{
		final LogLevelWrapper	prev=_outLevel;
		if (l != null)
			_outLevel = l;
		return prev;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#isEnabledFor(net.community.chest.util.logging.LogLevelWrapper)
	 */
	@Override
	public boolean isEnabledFor (final LogLevelWrapper l)
	{
		if (null == l)
			return false;

		final LogLevelWrapper	ol=getOutputLevel();
		if (null == ol)	// should not happen
			return true;

		final int	nDiff=l.compareTo(ol);
		return (nDiff <= 0);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#setEnabledFor(net.community.chest.util.logging.LogLevelWrapper)
	 */
	@Override
	public boolean setEnabledFor (LogLevelWrapper l)
	{
		if (l != null)
		{
			setOutputLevel(l);
			return true;
		}

		return false;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#log(net.community.chest.util.logging.LogLevelWrapper, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String log (final LogLevelWrapper l, final String msg, final Throwable t)
	{
		if (isEnabledFor(l))
			writeMessage(Thread.currentThread(), System.currentTimeMillis(), getLoggingClass(), l, getThreadContext(), msg, t);
		return msg;
	}
}
