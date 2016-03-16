/*
 *
 */
package net.community.chest.spring.test.entities;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Lyor G.
 * @since Aug 1, 2010 8:23:10 AM
 */
public class DateTimeValueAdapter extends XmlAdapter<Long,Date> {
    public DateTimeValueAdapter ()
    {
        super();
    }
    /*
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Date unmarshal (Long v) throws Exception
    {
        return (null == v) ? null : new Date(v.longValue());
    }
    /*
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public Long marshal (Date v) throws Exception
    {
        return (null == v) ? null : Long.valueOf(v.getTime());
    }
}
