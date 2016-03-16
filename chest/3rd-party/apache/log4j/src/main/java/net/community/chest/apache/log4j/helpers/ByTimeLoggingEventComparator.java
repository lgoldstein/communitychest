/*
 *
 */
package net.community.chest.apache.log4j.helpers;

import net.community.chest.lang.math.LongsComparator;
import net.community.chest.util.compare.AbstractComparator;

import org.apache.log4j.spi.LoggingEvent;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <E> The compared {@link LoggingEvent} type
 * @author Lyor G.
 * @since Sep 22, 2008 10:17:42 AM
 */
public class ByTimeLoggingEventComparator<E extends LoggingEvent> extends AbstractComparator<E> {
    /**
     *
     */
    private static final long serialVersionUID = -7056438078007933545L;

    public ByTimeLoggingEventComparator (Class<E> evClass, boolean ascending)
    {
        super(evClass, !ascending);
    }
    /*
     * @see com.emc.common.util.logging.log4j.AbstractLoggingEventsComparator#compareEvents(org.apache.log4j.spi.LoggingEvent, org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public int compareValues (E e1, E e2)
    {
        final long    t1=(null == e1) ? Long.MAX_VALUE : e1.getTimeStamp(),
                    t2=(null == e2) ? Long.MAX_VALUE : e2.getTimeStamp();
        return LongsComparator.compare(t1, t2);
    }

    public static final ByTimeLoggingEventComparator<LoggingEvent>    ASCENDING=
                                new ByTimeLoggingEventComparator<LoggingEvent>(LoggingEvent.class, true),
                                                                    DESCENDING=
                                new ByTimeLoggingEventComparator<LoggingEvent>(LoggingEvent.class, false);
}
