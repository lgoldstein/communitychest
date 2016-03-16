package net.community.chest.db.sql.convert;

import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <T> The {@link Timestamp} generic type
 * @author Lyor G.
 * @since May 21, 2008 3:58:11 PM
 */
public class TimestampValueInstantiator<T extends Timestamp> extends AbstractXmlValueStringInstantiator<T> {
    public TimestampValueInstantiator (Class<T> tcs)
    {
        super(tcs);
    }

    public static final String        DEFAULT_TIMESTAMP_FORMAT="yyyy-MM-dd HH:mm:ss";
    public static final DateFormat    DEFAULT_TIMESTAMP_FORMATTER=new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT);
    public static final String toString (final Date t)
    {
        if (null == t)
            return null;

        synchronized(DEFAULT_TIMESTAMP_FORMATTER)
        {
            return DEFAULT_TIMESTAMP_FORMATTER.format(t);
        }
    }

    public static final String toString (final long t)
    {
        return toString(new Date(t));
    }

    public static final String toString (final Calendar c)
    {
        return (null == c) ? null : toString(c.getTime());
    }

    public static final Timestamp fromString (final String v) throws ParseException
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final Date    d;
        synchronized(DEFAULT_TIMESTAMP_FORMATTER)
        {
            d = DEFAULT_TIMESTAMP_FORMATTER.parse(s);
        }

        return new Timestamp(d.getTime());
    }

    public DateFormat getInstanceFormatter () throws Exception
    {
        return DEFAULT_TIMESTAMP_FORMATTER;
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (final T inst) throws Exception
    {
        if (null == inst)
            return null;

        final DateFormat    dtf=getInstanceFormatter();
        synchronized(dtf)
        {
            return dtf.format(inst);
        }
    }

    private Constructor<T>    _ctor    /* =null */;
    public synchronized Constructor<T> getConstructor () throws Exception
    {
        if (null == _ctor)
        {
            final Class<T>    vClass=getValuesClass();
            if (null == (_ctor=vClass.getConstructor(Long.TYPE)))
                throw new NoSuchMethodException("No long value constructor");
        }

        return _ctor;
    }

    public synchronized void setConstructor (Constructor<T> ctor)
    {
        _ctor = ctor;
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public T newInstance (final String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final DateFormat    dtf=getInstanceFormatter();
        final Date            d;
        synchronized(dtf)
        {
            d = dtf.parse(s);
        }

        final Constructor<T>    ctor=getConstructor();
        final Long                tValue=Long.valueOf(d.getTime());
        return ctor.newInstance(tValue);
    }

    public static final TimestampValueInstantiator<Timestamp>    TIMESTAMP=
                    new TimestampValueInstantiator<Timestamp>(Timestamp.class) {
            /*
             * @see net.community.chest.db.sql.convert.TimestampValueInstantiator#convertInstance(java.sql.Timestamp)
             */
            @Override
            public String convertInstance (final Timestamp t) throws Exception
            {
                return toString(t);
            }
            /*
             * @see net.community.chest.db.sql.convert.TimestampValueInstantiator#newInstance(java.lang.String)
             */
            @Override
            public Timestamp newInstance (final String s) throws Exception
            {
                return fromString(s);
            }
        };
}
