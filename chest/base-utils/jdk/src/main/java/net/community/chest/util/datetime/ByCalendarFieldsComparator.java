/*
 *
 */
package net.community.chest.util.datetime;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Compares 2 {@link Calendar} values according to specified {@link CalendarFieldType}-s
 * according to their specified <U>order</U>. If no order specified then normal comparison is done</P>
 * @author Lyor G.
 * @since Nov 18, 2010 8:29:40 AM
 */
public class ByCalendarFieldsComparator extends AbstractComparator<Calendar> {

    private static final long serialVersionUID = -5338575707422882333L;
    private List<CalendarFieldType>    _fieldsOrder;
    public List<CalendarFieldType> getFieldsOrder ()
    {
        return _fieldsOrder;
    }
    // CAVEAT EMPTOR if used on shared instances
    public void setFieldsOrder (List<CalendarFieldType> fieldsOrder)
    {
        _fieldsOrder = fieldsOrder;
    }

    public ByCalendarFieldsComparator (List<CalendarFieldType> fieldsOrder, boolean ascending)
    {
        super(Calendar.class, !ascending);
        _fieldsOrder = fieldsOrder;
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (Calendar c1, Calendar c2)
    {
        // push null(s) to end
        if (c1 == c2)
            return 0;
        else if (c1 == null)
            return (+1);
        else if (c2 == null)
            return (-1);

        final Collection<CalendarFieldType>    fields=getFieldsOrder();
        if ((fields == null) || fields.isEmpty())
            return compareComparables(c1, c2);

        for (final CalendarFieldType f : fields)
        {
            if (f == null)
                continue;

            final int    v1=f.getFieldValue(c1),
                        v2=f.getFieldValue(c2),
                        d=v1 - v2;
            if (d != 0)
                return d;
        }

        return 0;
    }

    public static final ByCalendarFieldsComparator    BY_DATE_ASCENDING=
        new ByCalendarFieldsComparator(Arrays.asList(CalendarFieldType.YEAR, CalendarFieldType.MONTH, CalendarFieldType.DAY), true),
                                                    BY_DATE_DESCENDING=
        new ByCalendarFieldsComparator(Arrays.asList(CalendarFieldType.YEAR, CalendarFieldType.MONTH, CalendarFieldType.DAY), false),
                                                    BY_TIME_ASCENDING=
        new ByCalendarFieldsComparator(Arrays.asList(CalendarFieldType.HOUR, CalendarFieldType.MINUTE, CalendarFieldType.SECOND, CalendarFieldType.MSEC), true),
                                                    BY_TIME_DESCENDING=
        new ByCalendarFieldsComparator(Arrays.asList(CalendarFieldType.HOUR, CalendarFieldType.MINUTE, CalendarFieldType.SECOND, CalendarFieldType.MSEC), false);
}
