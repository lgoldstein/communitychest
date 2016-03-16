/*
 *
 */
package net.community.chest.util.datetime;

import java.util.Calendar;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since May 18, 2011 10:05:42 AM
 *
 */
public interface CalendarFieldIndicator {
    /**
     * @return The identifier of the calendar field represented by the indicator
     */
    int getCalendarFieldId ();
    /**
     * @param c The {@link Calendar} instance to process
     * @return The value of the field represented by the indicator (negative if <code>null</code>)
     */
    int getFieldValue (Calendar c);
}
