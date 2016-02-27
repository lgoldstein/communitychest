package net.community.chest.util.logging;

import java.util.Stack;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Provides some default implementations for the {@link LoggerWrapper} interface
 * 
 * @author Lyor G.
 * @since Jun 26, 2007 12:18:28 PM
 */
public abstract class AbstractLoggerWrapper implements LoggerWrapper {
	private final Class<?>	_logClass;
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#getLoggingClass()
	 */
	@Override
	public final /* no cheating */ Class<?> getLoggingClass ()
	{
		return _logClass;
	}

	private final String	_clsIndex;
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#getLoggingClassIndex()
	 */
	@Override
	public final /* no cheating */ String getLoggingClassIndex ()
	{
		return _clsIndex;
	}

	private final String	_logName;
	public final /* no cheating */ String getLoggerName ()
	{
		return _logName;
	}

	protected AbstractLoggerWrapper (
			final Class<?> logClass, final String logName, final String clsIndex)
		throws IllegalArgumentException
	{
		if (null == (_logClass=logClass))
			throw new IllegalArgumentException("No logging " + Class.class.getSimpleName() + " instance provided to " + getClass().getSimpleName() + " constructor");

		if ((null == logName) || (logName.length() <= 0))
			_logName = logClass.getName();
		else
			_logName = logName;

		_clsIndex = clsIndex;	// OK if null/empty
	}

	protected AbstractLoggerWrapper (
			final Class<?> logClass, final String clsIndex)
	 	throws IllegalArgumentException
	{
		this(logClass, null, clsIndex);
	}

	protected AbstractLoggerWrapper (final Class<?> logClass) throws IllegalArgumentException
	{
		this(logClass, null);
	}

	private static ThreadLocal<Stack<String>>	_ctxStack=new ThreadLocal<Stack<String>>(){
		/*
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		@Override
		protected synchronized Stack<String> initialValue() {
            return new Stack<String>();
        }
	};
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#getThreadContext()
	 */
	@Override
	public String getThreadContext ()
	{
		final Stack<String>	ctxStack=_ctxStack.get();
		return ((null == ctxStack) || ctxStack.isEmpty()) ? null : ctxStack.peek();
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#popThreadContext()
	 */
	@Override
	public String popThreadContext ()
	{
		final Stack<String>	ctxStack=_ctxStack.get();
		return ((null == ctxStack) || ctxStack.isEmpty()) ? null : ctxStack.pop();
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#pushThreadContext(java.lang.String)
	 */
	@Override
	public String pushThreadContext (final String ctx) throws IllegalArgumentException
	{
		if ((null == ctx) || (ctx.length() <= 0))
			throw new IllegalArgumentException("pushThreadContext() no context");

		final String		prev=getThreadContext();
		final Stack<String>	ctxStack=_ctxStack.get();
		if (ctxStack != null)
			ctxStack.push(ctx);
		return prev;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#clearThreadContext()
	 */
	@Override
	public void clearThreadContext ()
	{
		final Stack<String>	ctxStack=_ctxStack.get();
		if (ctxStack != null)
		{
			ctxStack.clear();
			_ctxStack.set(null);
		}
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#log(net.community.chest.util.logging.LogLevelWrapper, java.lang.String)
	 */
	@Override
	public String log (LogLevelWrapper l, String msg)
	{
		return log(l, msg, null);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#logObject(net.community.chest.util.logging.LogLevelWrapper, java.lang.String, java.lang.Throwable, java.lang.Object)
	 */
	@Override
	public <V> V logObject (LogLevelWrapper l, String msg, Throwable t, V obj)
	{
		log(l, msg, t);
		return obj;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#logObject(net.community.chest.util.logging.LogLevelWrapper, java.lang.String, java.lang.Object)
	 */
	@Override
	public <V> V logObject (LogLevelWrapper l, String msg, V obj)
	{
		return logObject(l, msg, null, obj);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#logThrowable(net.community.chest.util.logging.LogLevelWrapper, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T logThrowable (LogLevelWrapper l, String msg, T t)
	{
		return logObject(l, msg, t, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#logThrowable(net.community.chest.util.logging.LogLevelWrapper, java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T logThrowable (LogLevelWrapper l, T t)
	{
		return logThrowable(l, (null == t) ? null : t.getClass().getName() + ": " + t.getMessage(), t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#isDebugEnabled()
	 */
	@Override
	public boolean isDebugEnabled ()
	{
		return isEnabledFor(LogLevelWrapper.DEBUG);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#isVerboseEnabled()
	 */
	@Override
	public boolean isVerboseEnabled ()
	{
		return isEnabledFor(LogLevelWrapper.VERBOSE);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#isTraceEnabled()
	 */
	@Override
	public boolean isTraceEnabled ()
	{
		return isEnabledFor(LogLevelWrapper.TRACE);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#isInfoEnabled()
	 */
	@Override
	public boolean isInfoEnabled ()
	{
		return isEnabledFor(LogLevelWrapper.INFO);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#isWarnEnabled()
	 */
	@Override
	public boolean isWarnEnabled ()
	{
		return isEnabledFor(LogLevelWrapper.WARNING);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#isErrorEnabled()
	 */
	@Override
	public boolean isErrorEnabled ()
	{
		return isEnabledFor(LogLevelWrapper.ERROR);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#isFatalEnabled()
	 */
	@Override
	public boolean isFatalEnabled ()
	{
		return isEnabledFor(LogLevelWrapper.FATAL);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#debug(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String debug (String msg, Throwable t)
	{
		return log(LogLevelWrapper.DEBUG, msg, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#debug(java.lang.String)
	 */
	@Override
	public String debug (String msg)
	{
		return debug(msg, null);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#debugObject(java.lang.String, java.lang.Throwable, java.lang.Object)
	 */
	@Override
	public <V> V debugObject (String msg, Throwable t, V obj)
	{
		debug(msg, t);
		return obj;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#debugObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public <V> V debugObject (String msg, V obj)
	{
		return debugObject(msg, null, obj);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#debugThrowable(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T debugThrowable (String msg, T t)
	{
		return debugObject(msg, t, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#debugThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T debugThrowable (T t)
	{
		return debugThrowable((null == t) ? null : t.getClass().getName() + ": " + t.getMessage(), t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#verbose(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String verbose (String msg, Throwable t)
	{
		return log(LogLevelWrapper.VERBOSE, msg, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#verbose(java.lang.String)
	 */
	@Override
	public String verbose (String msg)
	{
		return verbose(msg, null);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#verboseObject(java.lang.String, java.lang.Throwable, java.lang.Object)
	 */
	@Override
	public <V> V verboseObject (String msg, Throwable t, V obj)
	{
		verbose(msg, t);
		return obj;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#verboseObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public <V> V verboseObject (String msg, V obj)
	{
		return verboseObject(msg, null, obj);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#verboseThrowable(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T verboseThrowable (String msg, T t)
	{
		return verboseObject(msg, t, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#verboseThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T verboseThrowable (T t)
	{
		return verboseThrowable((null == t) ? null : t.getClass().getName() + ": " + t.getMessage(), t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#trace(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String trace (String msg, Throwable t)
	{
		return log(LogLevelWrapper.TRACE, msg, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#trace(java.lang.String)
	 */
	@Override
	public String trace (String msg)
	{
		return trace(msg, null);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#traceObject(java.lang.String, java.lang.Throwable, java.lang.Object)
	 */
	@Override
	public <V> V traceObject (String msg, Throwable t, V obj)
	{
		trace(msg, t);
		return obj;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#traceObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public <V> V traceObject (String msg, V obj)
	{
		return traceObject(msg, null, obj);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#traceThrowable(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T traceThrowable (String msg, T t)
	{
		return traceObject(msg, t, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#traceThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T traceThrowable (T t)
	{
		return traceThrowable((null == t) ? null : t.getClass().getName() + ": " + t.getMessage(), t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#info(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String info (String msg, Throwable t)
	{
		return log(LogLevelWrapper.INFO, msg, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#info(java.lang.String)
	 */
	@Override
	public String info (String msg)
	{
		return info(msg, null);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#infoObject(java.lang.String, java.lang.Throwable, java.lang.Object)
	 */
	@Override
	public <V> V infoObject (String msg, Throwable t, V obj)
	{
		info(msg, t);
		return obj;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#infoObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public <V> V infoObject (String msg, V obj)
	{
		return infoObject(msg, null, obj);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#infoThrowable(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T infoThrowable (String msg, T t)
	{
		return infoObject(msg, t, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#infoThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T infoThrowable (T t)
	{
		return infoThrowable((null == t) ? null : t.getClass().getName() + ": " + t.getMessage(), t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#warning(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String warn (String msg, Throwable t)
	{
		return log(LogLevelWrapper.WARNING, msg, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#warning(java.lang.String)
	 */
	@Override
	public String warn (String msg)
	{
		return warn(msg, null);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#warningObject(java.lang.String, java.lang.Throwable, java.lang.Object)
	 */
	@Override
	public <V> V warnObject (String msg, Throwable t, V obj)
	{
		warn(msg, t);
		return obj;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#warningObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public <V> V warnObject (String msg, V obj)
	{
		return warnObject(msg, null, obj);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#warningThrowable(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T warnThrowable (String msg, T t)
	{
		return warnObject(msg, t, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#warningThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T warnThrowable (T t)
	{
		return warnThrowable((null == t) ? null : t.getClass().getName() + ": " + t.getMessage(), t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#severe(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String error (String msg, Throwable t)
	{
		return log(LogLevelWrapper.ERROR, msg, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#severe(java.lang.String)
	 */
	@Override
	public String error (String msg)
	{
		return error(msg, null);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#severeObject(java.lang.String, java.lang.Throwable, java.lang.Object)
	 */
	@Override
	public <V> V errorObject (String msg, Throwable t, V obj)
	{
		error(msg, t);
		return obj;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#severeObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public <V> V errorObject (String msg, V obj)
	{
		return errorObject(msg, null, obj);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#severeThrowable(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T errorThrowable (String msg, T t)
	{
		return errorObject(msg, t, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#severeThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T errorThrowable (T t)
	{
		return errorThrowable((null == t) ? null : t.getClass().getName() + ": " + t.getMessage(), t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#fatal(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String fatal (String msg, Throwable t)
	{
		return log(LogLevelWrapper.FATAL, msg, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#fatal(java.lang.String)
	 */
	@Override
	public String fatal (String msg)
	{
		return fatal(msg, null);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#fatalObject(java.lang.String, java.lang.Throwable, java.lang.Object)
	 */
	@Override
	public <V> V fatalObject (String msg, Throwable t, V obj)
	{
		fatal(msg, t);
		return obj;
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#fatalObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public <V> V fatalObject (String msg, V obj)
	{
		return fatalObject(msg, null, obj);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#fatalThrowable(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T fatalThrowable (String msg, T t)
	{
		return fatalObject(msg, t, t);
	}
	/*
	 * @see net.community.chest.util.logging.LoggerWrapper#fatalThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T fatalThrowable (T t)
	{
		return fatalThrowable((null == t) ? null : t.getClass().getName() + ": " + t.getMessage(), t);
	}
}
