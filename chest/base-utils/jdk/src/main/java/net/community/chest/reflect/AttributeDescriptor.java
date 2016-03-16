/*
 *
 */
package net.community.chest.reflect;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 30, 2009 9:30:43 AM
 */
public class AttributeDescriptor implements PubliclyCloneable<AttributeDescriptor> {
    public AttributeDescriptor ()
    {
        super();
    }

    private String    _name;
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }

    public AttributeDescriptor (String name)
    {
        _name = name;
    }

    private Class<?>    _type;
    public Class<?> getType ()
    {
        return _type;
    }

    public void setType (Class<?> t)
    {
        _type = t;
    }

    public AttributeDescriptor (String name, Class<?> type)
    {
        this(name);
        _type = type;
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public AttributeDescriptor clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final Class<?>    t=getType();
        return getName()
            + "[" + ((null == t) ? null : t.getName()) + "]"
            ;
    }

    public boolean isSameDescriptor (final AttributeDescriptor d)
    {
        if (d == null)
            return false;
        if (d == this)
            return true;

        return (0 == StringUtil.compareDataStrings(getName(), d.getName(), false))
            && AbstractComparator.compareObjects(getType(), d.getType())
            ;
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;

        return isSameDescriptor((AttributeDescriptor) obj);
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getName(), false)
             + ClassUtil.getObjectHashCode(getClass())
             ;
    }
}
