/*
 *
 */
package net.community.chest.util.datetime;

import java.util.Calendar;

/**
 * Represents a {@link Calendar} field value that has a fixed/finite set of
 * possible values
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Oct 9, 2011 7:34:55 AM
 */
public interface CalendarEnumFieldAccessor extends CalendarFieldIndicator {
    int getFieldValue ();
    void setFieldValue (Calendar c);
}
