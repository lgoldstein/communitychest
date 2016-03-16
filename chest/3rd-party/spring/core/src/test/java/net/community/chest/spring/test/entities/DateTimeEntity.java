/*
 *
 */
package net.community.chest.spring.test.entities;

import java.text.DateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Lyor G.
 * @since Jul 21, 2010 8:50:27 AM
 */
@Entity
@Table(name="date_time_entity")
@XmlRootElement(name="DateTimeEntity")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class DateTimeEntity extends AbstractBaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = 1751153913683432780L;

    private Date    _dateValue;

    public static final String DATE_VALUE_COL="date_value";
    @Column(name=DATE_VALUE_COL)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    public Date getDateValue ()
    {
        return _dateValue;
    }

    public void setDateValue (Date dtv)
    {
        _dateValue = dtv;
    }

    public DateTimeEntity (Date dtv, String name, String desc)
    {
        super(null, name, desc);
        _dateValue = dtv;
    }

    private static final DateFormat    DEFAULT_FORMAT=DateFormat.getDateTimeInstance();
    public static final String getDefaultDescription (Date dtv)
    {
        if (null == dtv)
            return null;

        synchronized(DEFAULT_FORMAT)
        {
            return DEFAULT_FORMAT.format(dtv);
        }
    }

    public DateTimeEntity (Date dtv, String name)
    {
        this(dtv, name, getDefaultDescription(dtv));
    }

    public DateTimeEntity (String name)
    {
        this(new Date(System.currentTimeMillis()), name);
    }

    public DateTimeEntity ()
    {
        this(new Date(System.currentTimeMillis()), null);
    }
    /*
     * @see net.community.chest.spring.test.entities.AbstractBaseEntity#hashCode()
     */
    @Override
    public int hashCode ()
    {
        final Date    d=getDateValue();
        return super.hashCode() + ((null == d) ? 0 : d.hashCode());
    }
    /*
     * @see net.community.chest.spring.test.entities.AbstractBaseEntity#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof DateTimeEntity))
            return false;
        if (!super.equals(obj))
            return false;
        if (this == obj)
            return true;

        final Date    td=getDateValue(), od=((DateTimeEntity) obj).getDateValue();
        if (null == td)
            return (od == null);
        else
            return td.equals(od);
    }
    /*
     * @see net.community.chest.spring.test.entities.AbstractBaseEntity#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString()
            + ";timestamp=" + getDefaultDescription(getDateValue())
            ;
    }
}
