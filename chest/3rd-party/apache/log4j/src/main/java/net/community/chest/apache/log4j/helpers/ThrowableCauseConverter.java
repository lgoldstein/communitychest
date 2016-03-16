package net.community.chest.apache.log4j.helpers;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2007 11:21:08 AM
 */
public class ThrowableCauseConverter extends ThrowableConverter {
    public ThrowableCauseConverter ()
    {
        super();
    }

    public ThrowableCauseConverter (int maxStackDepth)
    {
        super(maxStackDepth);
    }

    public ThrowableCauseConverter (String maxDepth)
    {
        super(maxDepth);
    }
    /*
     * @see net.community.chest.apache.log4j.helpers.ThrowableConverter#convert(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public String convert (LoggingEvent event)
    {
        final ThrowableInformation    ti=(null == event) /* should not happen */ ? null : event.getThrowableInformation();
        final Throwable                t=(null == ti) ? null : ti.getThrowable(),
                                    c=(null == t) ? null : t.getCause();
        return convert(c);
    }
}
