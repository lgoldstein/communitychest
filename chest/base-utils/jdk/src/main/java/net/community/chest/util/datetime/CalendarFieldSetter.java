/*
 *
 */
package net.community.chest.util.datetime;

import java.util.Calendar;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Oct 9, 2011 7:34:13 AM
 *
 */
public interface CalendarFieldSetter extends CalendarFieldIndicator {
    /**
     * @param c The {@link Calendar} instance to process - ignored if <code>null</code>
     * @param value The value to set for the field represented by the indicator
     */
    void setFieldValue (Calendar c, int value);
}
