/*
 *
 */
package net.community.chest.apache.log4j.helpers;

import org.apache.log4j.spi.LoggingEvent;

import net.community.chest.reflect.FieldsAccessor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <E> The accessed {@link LoggingEvent} type
 * @author Lyor G.
 * @since Oct 26, 2008 1:50:10 PM
 */
public class LoggingEventFieldsAccessor<E extends LoggingEvent> extends FieldsAccessor<E> {
    /**
     *
     */
    private static final long serialVersionUID = -9195542500705736137L;
    public LoggingEventFieldsAccessor (Class<E> eventClass)
    {
        super(eventClass, LoggingEvent.class);
    }

    public static final String    CATEGORY_NAME_FIELD_NAME="categoryName";
    public String getCategoryName (final E event) throws Exception
    {
        return (null == event) ? null : getCastFieldValue(event, CATEGORY_NAME_FIELD_NAME, String.class);
    }

    public void setCategoryName (final E event, final String name) throws Exception
    {
        setFieldValue(event, CATEGORY_NAME_FIELD_NAME, name);
    }

    public static final LoggingEventFieldsAccessor<LoggingEvent>    LOGEVENT=
                new LoggingEventFieldsAccessor<LoggingEvent>(LoggingEvent.class);
}
