package net.community.chest.apache.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful base class for {@link org.apache.log4j.Appender} implementors</P>
 *
 * @author Lyor G.
 * @since Sep 30, 2007 11:45:48 AM
 */
public abstract class AbstractAppender extends AppenderSkeleton {
    /**
     * Constructor - sets the default name as the class name
     */
    protected AbstractAppender ()
    {
        super.name = getClass().getName();
    }
    /**
     * Checks the filters list (if any)
     * @param e logging event
     * @return DENY/ACCEPT/NEUTRAL(default if no filters set)
     */
    protected int consultFilters (LoggingEvent e)
    {
        for (Filter    f=getFilter(); f != null; f = f.getNext())
        {
            final int    resFilter=f.decide(e);
            if ((Filter.DENY == resFilter) || (Filter.ACCEPT == resFilter))
                return resFilter;
        }

        return Filter.NEUTRAL;
    }
    /**
     * @return TRUE if {@link #close()} has not been called
     */
    protected boolean isOpen ()
    {
        return (!super.closed);
    }

    protected void setOpen (boolean open)
    {
        super.closed = !open;
    }
    /*
     * @see org.apache.log4j.Appender#close()
     */
    @Override
    public void close ()
    {
        if (isOpen())
            setOpen(false);
    }
    /**
     * Calls the {@link ErrorHandler} - if any set
     * @param msg message to be reported
     * @return TRUE if message reported
     */
    public boolean errorReport (String msg)
    {
        final ErrorHandler    eh=getErrorHandler();
        if (eh != null)
            eh.error(msg);
        return (eh != null);
    }
    /**
     * Calls the {@link ErrorHandler} - if any set
     * @param msg message to be reported
     * @param e exception associated with the message
     * @param errCode error code value
     * @return TRUE if message reported
     */
    public boolean errorReport (String msg, Exception e, int errCode)
    {
        final ErrorHandler    eh=getErrorHandler();
        if (eh != null)
            eh.error(msg, e, errCode);
        return (eh != null);
    }
    /**
     * Calls the {@link ErrorHandler} - if any set
     * @param msg message to be reported
     * @param e exception associated with the message
     * @param errCode error code value
     * @param event logging event that caused the error
     * @return TRUE if message reported
     */
    public boolean errorReport (String msg, Exception e, int errCode, LoggingEvent event)
    {
        final ErrorHandler    eh=getErrorHandler();
        if (eh != null)
            eh.error(msg, e, errCode, event);
        return (eh != null);
    }
    /* DEFAULT=FALSE
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    @Override
    public boolean requiresLayout ()
    {
        return false;
    }

    private long    _numAppended    /* =0 */;
    /**
     * @return number of successfully appended events
     * @see #appendFilteredEvent(LoggingEvent)
     */
    public long getNumAppended ()
    {
        return _numAppended;
    }

    private int    _numDiscarded    /* =0 */;
    /**
     * @return number of events discarded due to FALSE return value from
     * call to {@link #appendFilteredEvent(LoggingEvent)} and/or
     * {@link #appendFormattedEvent(LoggingEvent, String)}
     */
    public int getNumDiscarded ()
    {
        return _numDiscarded;
    }

    private int    _numErrors    /* =0 */;
    /**
     * @return Number of errors that occurred while attempting to append
     * logging events
     */
    public int getNumErrors ()
    {
        return _numErrors;
    }
    /**
     * Replaces CR/LF with space and trims the result
     * @param org original string
     * @return cleaned up string - may be null/empty
     */
    public static final String cleanupLogText (final String org)
    {
        final String    lfClean=
            ((null == org) || (org.length() <= 0)) ? null : org.replace('\n', ' '),
                        crClean=
            ((null == lfClean) || (lfClean.length() <= 0)) ? null : lfClean.replace('\r', ' '),
                        retText=
            ((null == crClean) || (crClean.length() <= 0)) ? null : crClean.trim();
        return retText;
    }
    /**
     * Called by the default {@link #appendFilteredEvent(LoggingEvent)}
     * after formatting the message according to the layout (if any)
     * @param e original logging event
     * @param msg formatted message (with/out layout)
     * @return TRUE if event was successfully logged
     */
    protected abstract boolean appendFormattedEvent (LoggingEvent e, String msg);
    /**
     * Called by the default {@link #doAppend(LoggingEvent)} implementation
     * <U>after</U> consulting the filters (i.e., implementor should not
     * re-consult them)
     * @param e event to log
     * @return TRUE if event was successfully logged
     */
    protected boolean appendFilteredEvent (final LoggingEvent e)
    {
        if (null == e)
            return false;

        final Layout    l=getLayout();
        final String    msg;
        if (null == l)
        {
            if (requiresLayout())
                return false;

            msg = String.valueOf(e.getMessage());
        }
        else
            msg = l.format(e);

        return appendFormattedEvent(e, cleanupLogText(msg));
    }
    /*
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public final void append (final LoggingEvent msg)
    {
        try
        {
            if (appendFilteredEvent(msg))
                _numAppended++;
            else
                _numDiscarded++;
        }
        catch(Exception e)
        {
            errorReport("doAppend()", e, (-1), msg);
        }
    }
}
