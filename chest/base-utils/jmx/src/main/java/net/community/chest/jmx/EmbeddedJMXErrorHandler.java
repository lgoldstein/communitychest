/*
 *
 */
package net.community.chest.jmx;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 25, 2009 1:22:52 PM
 */
public class EmbeddedJMXErrorHandler implements JMXErrorHandler {
    private JMXErrorHandler    _eh;
    public JMXErrorHandler getJMXErrorHandler ()
    {
        return _eh;
    }

    public void setJMXErrorHandler (JMXErrorHandler eh)
    {
        _eh = eh;
    }

    public EmbeddedJMXErrorHandler (JMXErrorHandler eh)
    {
        _eh = eh;
    }

    public EmbeddedJMXErrorHandler ()
    {
        this(null);
    }
    /*
     * @see net.community.chest.jmx.JMXErrorHandler#errorThrowable(java.lang.Throwable)
     */
    @Override
    public <T extends Throwable> T errorThrowable (T t)
    {
        final JMXErrorHandler    eh=getJMXErrorHandler();
        if ((eh != null) && (eh != this))
            return eh.errorThrowable(t);
        else
            return t;
    }
    /*
     * @see net.community.chest.jmx.JMXErrorHandler#warnThrowable(java.lang.Throwable)
     */
    @Override
    public <T extends Throwable> T warnThrowable (T t)
    {
        final JMXErrorHandler    eh=getJMXErrorHandler();
        if ((eh != null) && (eh != this))
            return eh.warnThrowable(t);
        else
            return t;
    }
    /*
     * @see net.community.chest.jmx.JMXErrorHandler#mbeanError(java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void mbeanError (String mbName, String msg, Throwable t)
    {
        final JMXErrorHandler    eh=getJMXErrorHandler();
        if ((eh != null) && (eh != this))
            eh.mbeanError(mbName, msg, t);
    }
    /*
     * @see net.community.chest.jmx.JMXErrorHandler#mbeanWarning(java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void mbeanWarning (String mbName, String msg, Throwable t)
    {
        final JMXErrorHandler    eh=getJMXErrorHandler();
        if ((eh != null) && (eh != this))
            eh.mbeanWarning(mbName, msg, t);
    }
}
