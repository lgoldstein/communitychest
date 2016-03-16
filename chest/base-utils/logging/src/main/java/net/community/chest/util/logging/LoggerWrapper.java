package net.community.chest.util.logging;

/**
 * Copyright 2007 as per GPLv2
 *
 * <P>Class used to hide as much as possible the JDK logging and enable
 * replacing it with another logger (e.g., log4j). Furthermore, this wrapper
 * is intended to be used as a <U>class level</U> wrapper - i.e., its "name"
 * is the fully-qualified class name of the {@link Class} which generates the
 * log messages.</P>
 *
 * <P><B>Note:</B> this class is not intended to expose the full functionality
 * of the underlying logger - only the most likely usages (IMHO). Also, this
 * wrapper makes no assumptions as to how these messages are handled, by whom,
 * how they are formatted, etc.</P>
 *
 * @author Lyor G.
 * @since Jun 26, 2007 11:59:41 AM
 */
public interface LoggerWrapper {
    /**
     * @return the {@link Class} which uses this wrapper.
     */
    Class<?> getLoggingClass ();
    /**
     * @return A class "index" discriminator that can be added to the
     * logging {@link Class} ID - may be null/empty
     */
    String getLoggingClassIndex ();
    /**
     * @param ctx unique thread-local context - may NOT be null
     * @return previous context that was at the top (if any)
     * @throws IllegalArgumentException if null context provided
     */
    String pushThreadContext (String ctx) throws IllegalArgumentException;
    /**
     * @return currently set thread context - null if none set
     */
    String getThreadContext ();
    /**
     * @return unique thread context - pops it from the stack, so that call to
     * {@link #getThreadContext()} will yield the next context in stack
     */
    String popThreadContext ();
    /**
     * Removes all thread contexts - should be used when thread {@link Runnable#run()}
     * method exits
     */
    void clearThreadContext ();
    /**
     * @param l {@link LogLevelWrapper} for which we want to know if this logger is
     * enabled to log messages. This should be done whenever building the
     * string message might take some time (which is almost always). Usually,
     * projects define a "threshold" level below which any logging should
     * ask if level is enabled (e.g., setting the level at INFO, means that
     * anyone wishing to log at fine/finer/finest should call this method
     * first before issuing the log message)
     * @return TRUE if requested level is enabled for logging
     */
    boolean isEnabledFor (LogLevelWrapper l);
    /**
     * @param l The new level to set to
     * @return TRUE if successfully enabled this level
     */
    boolean setEnabledFor (LogLevelWrapper l);
    /**
     * @param l {@link LogLevelWrapper} at which to log the message
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return original message
     */
    String log (LogLevelWrapper l, String msg, Throwable t);
    /**
     * @param l {@link LogLevelWrapper} at which to log the message
     * @param msg string message to be logged
     * @return original message
     */
    String log (LogLevelWrapper l, String msg);
    /**
     * @param <V> The returned object generic type
     * @param l {@link LogLevelWrapper} at which to log the message
     * @param msg string message to be logged
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V logObject (LogLevelWrapper l, String msg, V obj);
    /**
     * @param <V> The returned object generic type
     * @param l {@link LogLevelWrapper} at which to log the message
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V logObject (LogLevelWrapper l, String msg, Throwable t, V obj);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param l {@link LogLevelWrapper} at which to log the message
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T logThrowable (LogLevelWrapper l, String msg, T t);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param l {@link LogLevelWrapper} at which to log the message
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T logThrowable (LogLevelWrapper l, T t);
    /**
     * @return TRUE if this level is enabled for logging
     */
    boolean isTraceEnabled ();
    /**
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return original message
     */
    String trace (String msg, Throwable t);
    /**
     * @param msg string message to be logged
     * @return original message
     */
    String trace (String msg);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V traceObject (String msg, V obj);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V traceObject (String msg, Throwable t, V obj);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T traceThrowable (String msg, T t);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T traceThrowable (T t);
    /**
     * @return TRUE if this level is enabled for logging
     */
    boolean isVerboseEnabled ();
    /**
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return original message
     */
    String verbose (String msg, Throwable t);
    /**
     * @param msg string message to be logged
     * @return original message
     */
    String verbose (String msg);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V verboseObject (String msg, V obj);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V verboseObject (String msg, Throwable t, V obj);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T verboseThrowable (String msg, T t);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T verboseThrowable (T t);
    /**
     * @return TRUE if this level is enabled for logging
     */
    boolean isDebugEnabled ();
    /**
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return original message
     */
    String debug (String msg, Throwable t);
    /**
     * @param msg string message to be logged
     * @return original message
     */
    String debug (String msg);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V debugObject (String msg, V obj);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V debugObject (String msg, Throwable t, V obj);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T debugThrowable (String msg, T t);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T debugThrowable (T t);
    /**
     * @return TRUE if this level is enabled for logging
     */
    boolean isInfoEnabled ();
    /**
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return original message
     */
    String info (String msg, Throwable t);
    /**
     * @param msg string message to be logged
     * @return original message
     */
    String info (String msg);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V infoObject (String msg, V obj);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V infoObject (String msg, Throwable t, V obj);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T infoThrowable (String msg, T t);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T infoThrowable (T t);
    /**
     * @return TRUE if this level is enabled for logging
     */
    boolean isWarnEnabled ();
    /**
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return original message
     */
    String warn (String msg, Throwable t);
    /**
     * @param msg string message to be logged
     * @return original message
     */
    String warn (String msg);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V warnObject (String msg, V obj);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V warnObject (String msg, Throwable t, V obj);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T warnThrowable (String msg, T t);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T warnThrowable (T t);
    /**
     * @return TRUE if this level is enabled for logging
     */
    boolean isErrorEnabled ();
    /**
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return original message
     */
    String error (String msg, Throwable t);
    /**
     * @param msg string message to be logged
     * @return original message
     */
    String error (String msg);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V errorObject (String msg, V obj);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V errorObject (String msg, Throwable t, V obj);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T errorThrowable (String msg, T t);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T errorThrowable (T t);
    /**
     * @return TRUE if this level is enabled for logging
     */
    boolean isFatalEnabled ();
    /**
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return original message
     */
    String fatal (String msg, Throwable t);
    /**
     * @param msg string message to be logged
     * @return original message
     */
    String fatal (String msg);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V fatalObject (String msg, V obj);
    /**
     * @param <V> The returned object generic type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @param obj object value to be returned after logging, <B>Note:</B> the
     * passed object value is <U>not</U> included in the generated log message
     * @return <I>obj</I> parameter
     */
    <V> V fatalObject (String msg, Throwable t, V obj);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param msg string message to be logged
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T fatalThrowable (String msg, T t);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T fatalThrowable (T t);
}
