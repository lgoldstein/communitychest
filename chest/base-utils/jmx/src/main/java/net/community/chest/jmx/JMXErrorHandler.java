/*
 *
 */
package net.community.chest.jmx;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>An interface used to report about errors in some JMX methods</P>
 *
 * @author Lyor G.
 * @since Feb 25, 2009 9:52:39 AM
 */
public interface JMXErrorHandler {
    /**
     * @param mbName The MBean name for which this error is reported (may be
     * <code>null/empty</code> if a generic error is reported)
     * @param msg A descriptive text of the problems
     * @param t A {@link Throwable} instance that caused the problem - may
     * be <code>null</code> if the error is not related to an exception
     */
    void mbeanError (String mbName, String msg, Throwable t);
    void mbeanWarning (String mbName, String msg, Throwable t);
    /**
     * @param <T> The {@link Throwable} instance type
     * @param t associated {@link Throwable} - if non-null, then its class,
     * message and stack trace will be output to the log
     * @return input {@link Throwable} instance
     */
    <T extends Throwable> T errorThrowable (T t);
    <T extends Throwable> T warnThrowable (T t);
}
