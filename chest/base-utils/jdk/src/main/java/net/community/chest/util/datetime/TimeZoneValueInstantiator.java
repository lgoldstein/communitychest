/*
 *
 */
package net.community.chest.util.datetime;

import java.util.NoSuchElementException;
import java.util.TimeZone;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <TZ> The instantiated {@link TimeZone}
 * @author Lyor G.
 * @since Jan 12, 2009 3:44:35 PM
 */
public abstract class TimeZoneValueInstantiator<TZ extends TimeZone> extends AbstractXmlValueStringInstantiator<TZ> {
    protected TimeZoneValueInstantiator (Class<TZ> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (TZ inst) throws Exception
    {
        return (null == inst) ? null : inst.getID();
    }

    public static final TimeZoneValueInstantiator<TimeZone>    DEFAULT=
        new TimeZoneValueInstantiator<TimeZone>(TimeZone.class) {
            /*
             * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
             */
            @Override
            public TimeZone newInstance (String v) throws Exception
            {
                final String    s=StringUtil.getCleanStringValue(v);
                if ((null == s) || (s.length() <= 0))
                    return null;

                final String[]    ids=TimeZone.getAvailableIDs();
                if ((null == ids) || (ids.length <= 0))
                    throw new IllegalStateException("newInstance(" +s + ") no TZ ID(s)");

                for (final String tzid : ids)
                {
                    if (0 == StringUtil.compareDataStrings(tzid, s, false))
                        return TimeZone.getTimeZone(tzid);
                }

                throw new NoSuchElementException("newInstance(" + s + ") unknown value");
            }
        };
}
