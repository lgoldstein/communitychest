/*
 *
 */
package net.community.chest.ui.components.logging;

import javax.swing.text.BadLocationException;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.util.logging.AbstractLoggerWrapper;
import net.community.chest.util.logging.LogLevelWrapper;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <V> Type of {@link LoggingTextPane} being assigned
 * @author Lyor G.
 * @since Aug 2, 2009 11:07:21 AM
 */
public class LoggingTextPaneLoggerWrapper<V extends LoggingTextPane>
            extends AbstractLoggerWrapper
            implements TypedComponentAssignment<V> {
    private V    _value;
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public V getAssignedValue ()
    {
        return _value;
    }
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (V value)
    {
        _value = value;
    }

    public LoggingTextPaneLoggerWrapper (V pane, Class<?> logClass, String logName, String clsIndex)
        throws IllegalArgumentException
    {
        super(logClass, logName, clsIndex);
        _value = pane;

    }
    public LoggingTextPaneLoggerWrapper (V pane, Class<?> logClass, String clsIndex)
        throws IllegalArgumentException
    {
        this(pane, logClass, null, clsIndex);
    }

    public LoggingTextPaneLoggerWrapper (V pane, Class<?> logClass)
            throws IllegalArgumentException
    {
        this(pane, logClass, null);
    }
    /*
     * @see net.community.chest.util.logging.LoggerWrapper#isEnabledFor(net.community.chest.util.logging.LogLevelWrapper)
     */
    @Override
    public boolean isEnabledFor (LogLevelWrapper l)
    {
        final LoggingTextPane    p=getAssignedValue();
        return (p != null) && p.isEnabledFor(l);
    }

    protected void handleLocationException (LogLevelWrapper l, String msg, Throwable t, BadLocationException e)
    {
        if ((null == l) || (null == msg) || (null == t) || (null == e))
            return;    // just so compiler does not complain about unused parameters
    }
    /*
     * @see net.community.chest.util.logging.LoggerWrapper#log(net.community.chest.util.logging.LogLevelWrapper, java.lang.String, java.lang.Throwable)
     */
    @Override
    public String log (LogLevelWrapper l, String msg, Throwable t)
    {
        final LoggingTextPane    p=getAssignedValue();
        if (null == p)    // debug breakpoint
            return msg;

        try
        {
            p.log(l, msg);
        }
        catch (BadLocationException e)
        {
            handleLocationException(l, msg, t, e);
        }

        return msg;
    }
    /*
     * @see net.community.chest.util.logging.LoggerWrapper#setEnabledFor(net.community.chest.util.logging.LogLevelWrapper)
     */
    @Override
    public boolean setEnabledFor (LogLevelWrapper l)
    {
        return false;
    }
}
