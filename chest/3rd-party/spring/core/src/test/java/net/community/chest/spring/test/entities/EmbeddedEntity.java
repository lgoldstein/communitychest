/*
 *
 */
package net.community.chest.spring.test.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 20, 2011 10:11:21 AM
 */
@Embeddable
public class EmbeddedEntity implements Serializable, Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = -460441490363498494L;
    public EmbeddedEntity ()
    {
        super();
    }

    public EmbeddedEntity (String name, String address)
    {
        _name = name;
        _address = address;
    }

    private String    _name;
    @Column(name="NAME", nullable=true, length=128, unique=false)
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }

    private String    _address;
    @Column(name="ADDRESS", nullable=true, length=80, unique=false)
    public String getAddress ()
    {
        return _address;
    }

    public void setAddress (String address)
    {
        _address = address;
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getName(), false)
             + StringUtil.getDataStringHashCode(getAddress(), true)
             ;
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
            return true;

        final Class<?>    c=(obj == null) ? null : obj.getClass();
        if (c != getClass())
            return false;

        final EmbeddedEntity    oe=(EmbeddedEntity) obj;
        if ((StringUtil.compareDataStrings(getName(), oe.getName(), false) == 0)
         && (StringUtil.compareDataStrings(getAddress(), oe.getAddress(), true) == 0))
            return true;

        return false;
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public EmbeddedEntity clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getName() + "=" + getAddress();
    }
}
